package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.MediaItemData
import com.example.data.model.PlayerRepeatMode
import com.example.data.model.PlaybackMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

data class PlaybackSnapshot(
    val currentMedia: MediaItemData? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: PlayerRepeatMode = PlayerRepeatMode.OFF,
    val playbackMode: PlaybackMode = PlaybackMode.ORDER, // Default to sequential play
    val isMuted: Boolean = false,
    val queue: List<MediaItemData> = emptyList(),
    val queueIndex: Int = -1,
    val isAudioSessionActive: Boolean = false,
    val hasSubtitle: Boolean = false,
    val sleepTimerMinutesLeft: Int = 0,
    val errorMessage: String? = null
)

@OptIn(UnstableApi::class)
class PlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _state = MutableStateFlow(PlaybackSnapshot())
    val state: StateFlow<PlaybackSnapshot> = _state.asStateFlow()

    fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            initPlayer()
        }
        return exoPlayer!!
    }

    private fun initPlayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 AroyPlayer/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20000)
            .setReadTimeoutMs(20000)

        val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(defaultDataSourceFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId > 0 && audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    applyDolbyEffects(audioSessionId)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val rawDur = player.duration
                val duration = if (rawDur > 0 && rawDur != C.TIME_UNSET) {
                    rawDur
                } else {
                    (_state.value.currentMedia?.durationMs ?: 0L).coerceAtLeast(0L)
                }
                _state.value = _state.value.copy(
                    isBuffering = isBuffering,
                    durationMs = duration
                )

                if (playbackState == Player.STATE_ENDED) {
                    // Dispatch asynchronously on Main scope to avoid re-entrant ExoPlayer listener mutation
                    scope.launch {
                        onTrackEnded()
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val detailedMsg = when (val cause = error.cause) {
                    is HttpDataSource.InvalidResponseCodeException -> {
                        if (cause.responseCode == 403) {
                            "Stream server returned 403: Access forbidden or URL expired"
                        } else {
                            "Stream server returned HTTP error code ${cause.responseCode}"
                        }
                    }
                    is HttpDataSource.HttpDataSourceException -> {
                        "Connection failed: unable to fetch media stream"
                    }
                    else -> {
                        error.message ?: "Playback error encountered"
                    }
                }
                stopProgressTracker()
                _state.value = _state.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = detailedMsg
                )
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _state.value = _state.value.copy(currentPositionMs = newPosition.positionMs.coerceAtLeast(0L))
            }
        })

        exoPlayer = player
    }

    companion object {
        fun isSafeMediaUri(uriStr: String): Boolean {
            val trimmed = uriStr.trim()
            if (trimmed.isEmpty() || trimmed.length > 4096) return false

            // Block directory traversal attempts
            if (trimmed.contains("../") || trimmed.contains("..\\")) return false

            // Prevent Local File Inclusion (LFI) & Private Sandbox directory probing
            val lower = trimmed.lowercase()
            if (lower.startsWith("file:///data/") ||
                lower.startsWith("file:///data/data/") ||
                lower.startsWith("file:///data/user/") ||
                lower.startsWith("file:///proc/") ||
                lower.startsWith("file:///sys/") ||
                lower.startsWith("file:///etc/") ||
                lower.startsWith("javascript:") ||
                lower.startsWith("data:") ||
                lower.startsWith("intent:") ||
                lower.startsWith("blob:")
            ) {
                return false
            }

            return true
        }
    }

    fun playMedia(
        item: MediaItemData,
        playlist: List<MediaItemData> = emptyList(),
        startPositionMs: Long = 0L,
        subtitleUri: Uri? = null
    ) {
        if (!isSafeMediaUri(item.uri)) {
            _state.value = _state.value.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = "Security: Access to restricted or unsafe URI blocked."
            )
            return
        }

        try {
            val player = getPlayer()

            val fullQueue = if (playlist.isNotEmpty()) playlist else listOf(item)
            val index = fullQueue.indexOfFirst { it.uri == item.uri }.let { if (it >= 0) it else 0 }

            val mediaItemBuilder = MediaItem.Builder().setUri(item.uri)

            // HLS detection
            if (item.uri.endsWith(".m3u8", ignoreCase = true) || item.streamType == "HLS") {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            }

            // Subtitle support (.srt or .vtt)
            if (subtitleUri != null) {
                val mimeType = if (subtitleUri.toString().endsWith(".vtt", ignoreCase = true)) {
                    MimeTypes.TEXT_VTT
                } else {
                    MimeTypes.APPLICATION_SUBRIP
                }
                val subtitle = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                    .setMimeType(mimeType)
                    .setLanguage("en")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitle))
            }

            // Cleanly stop previous media period & release codecs before loading next track
            stopProgressTracker()
            player.stop()
            player.clearMediaItems()

            val safeStartPos = if (item.durationMs > 0 && startPositionMs >= item.durationMs - 2000L) {
                0L
            } else {
                startPositionMs.coerceAtLeast(0L)
            }

            player.setMediaItem(mediaItemBuilder.build(), safeStartPos)
            player.playWhenReady = true
            player.prepare()
            player.play()
            applyDolbyEffects(player.audioSessionId)

            _state.value = _state.value.copy(
                currentMedia = item,
                queue = fullQueue,
                queueIndex = index,
                currentPositionMs = safeStartPos,
                durationMs = item.durationMs.coerceAtLeast(0L),
                isAudioSessionActive = !item.isVideo,
                hasSubtitle = subtitleUri != null,
                errorMessage = null
            )
        } catch (t: Throwable) {
            _state.value = _state.value.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = t.message ?: "Unable to play selected track"
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun retry() {
        val current = _state.value.currentMedia ?: return
        val pos = _state.value.currentPositionMs
        playMedia(current, _state.value.queue, pos)
    }

    private var lastToggleTimeMs = 0L

    fun playPauseToggle() {
        val now = System.currentTimeMillis()
        if (now - lastToggleTimeMs < 250L) {
            return
        }
        lastToggleTimeMs = now

        val player = exoPlayer ?: return
        val isCurrentlyPlaying = player.playWhenReady && (_state.value.isPlaying || player.isPlaying)
        if (isCurrentlyPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        val player = exoPlayer ?: return
        player.playWhenReady = false
        player.pause()
        _state.value = _state.value.copy(isPlaying = false)
        stopProgressTracker()
    }

    fun resume() {
        val player = exoPlayer ?: return
        player.playWhenReady = true
        player.play()
        _state.value = _state.value.copy(isPlaying = true)
        startProgressTracker()
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        val clamped = positionMs.coerceIn(0L, _state.value.durationMs.coerceAtLeast(1L))
        player.seekTo(clamped)
        _state.value = _state.value.copy(currentPositionMs = clamped)
    }

    fun forward10s() {
        val player = exoPlayer ?: return
        val target = player.currentPosition + 10000
        seekTo(target)
    }

    fun rewind10s() {
        val player = exoPlayer ?: return
        val target = player.currentPosition - 10000
        seekTo(target.coerceAtLeast(0L))
    }

    fun setPlaybackSpeed(speed: Float) {
        val player = exoPlayer ?: return
        player.playbackParameters = PlaybackParameters(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    fun cyclePlaybackMode() {
        val next = when (_state.value.playbackMode) {
            PlaybackMode.ORDER -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.REPEAT_ALL
            PlaybackMode.REPEAT_ALL -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.STOP_ON_FINISH
            PlaybackMode.STOP_ON_FINISH -> PlaybackMode.ORDER
        }
        setPlaybackMode(next)
    }

    fun setPlaybackMode(mode: PlaybackMode) {
        val player = exoPlayer
        if (player != null) {
            // Keep ExoPlayer repeatMode OFF so STATE_ENDED always fires and onTrackEnded() manages the full queue
            player.repeatMode = Player.REPEAT_MODE_OFF
        }
        val legacyRepeat = when (mode) {
            PlaybackMode.REPEAT_ONE -> PlayerRepeatMode.ONE
            PlaybackMode.REPEAT_ALL -> PlayerRepeatMode.ALL
            else -> PlayerRepeatMode.OFF
        }
        _state.value = _state.value.copy(
            playbackMode = mode,
            repeatMode = legacyRepeat
        )
    }

    fun toggleRepeatMode() {
        cyclePlaybackMode()
    }

    fun setRepeatMode(mode: PlayerRepeatMode) {
        val pMode = when (mode) {
            PlayerRepeatMode.OFF -> PlaybackMode.ORDER
            PlayerRepeatMode.ALL -> PlaybackMode.REPEAT_ALL
            PlayerRepeatMode.ONE -> PlaybackMode.REPEAT_ONE
        }
        setPlaybackMode(pMode)
    }

    fun playNext() {
        val q = _state.value.queue
        if (q.isEmpty()) return
        val currIdx = _state.value.queueIndex.coerceIn(0, q.size - 1)
        val nextIdx = if (_state.value.playbackMode == PlaybackMode.SHUFFLE && q.size > 1) {
            val available = q.indices.filter { it != currIdx }
            if (available.isNotEmpty()) available.random() else (currIdx + 1) % q.size
        } else {
            (currIdx + 1) % q.size
        }
        q.getOrNull(nextIdx)?.let { playMedia(it, q) }
    }

    fun playPrevious() {
        val q = _state.value.queue
        if (q.isEmpty()) return
        val currIdx = _state.value.queueIndex.coerceIn(0, q.size - 1)
        val prevIdx = if (currIdx - 1 < 0) q.size - 1 else currIdx - 1
        q.getOrNull(prevIdx)?.let { playMedia(it, q) }
    }

    fun toggleMute() {
        val player = exoPlayer ?: return
        val willMute = !_state.value.isMuted
        player.volume = if (willMute) 0.0f else 1.0f
        _state.value = _state.value.copy(isMuted = willMute)
    }

    fun attachSubtitle(uri: Uri) {
        val current = _state.value.currentMedia ?: return
        val currentPos = exoPlayer?.currentPosition ?: 0L
        playMedia(current, _state.value.queue, currentPos, uri)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.value = _state.value.copy(sleepTimerMinutesLeft = 0)
            return
        }

        _state.value = _state.value.copy(sleepTimerMinutesLeft = minutes)
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0 && isActive) {
                delay(60000L)
                remaining--
                _state.value = _state.value.copy(sleepTimerMinutesLeft = remaining)
            }
            pause()
            _state.value = _state.value.copy(sleepTimerMinutesLeft = 0)
        }
    }

    private fun onTrackEnded() {
        val mode = _state.value.playbackMode
        val q = _state.value.queue
        val currIdx = if (q.isNotEmpty()) _state.value.queueIndex.coerceIn(0, q.size - 1) else 0

        when (mode) {
            PlaybackMode.REPEAT_ONE -> {
                seekTo(0)
                resume()
            }
            PlaybackMode.SHUFFLE -> {
                if (q.isNotEmpty()) {
                    val nextIdx = if (q.size > 1) {
                        val available = q.indices.filter { it != currIdx }
                        if (available.isNotEmpty()) available.random() else 0
                    } else 0
                    q.getOrNull(nextIdx)?.let { playMedia(it, q) }
                } else {
                    _state.value = _state.value.copy(isPlaying = false)
                }
            }
            PlaybackMode.ORDER -> {
                // Sequential play (≡↓): auto play next in list, stop if end of queue reached
                if (q.isNotEmpty()) {
                    if (currIdx + 1 < q.size) {
                        q.getOrNull(currIdx + 1)?.let { playMedia(it, q) }
                    } else {
                        // End of playlist
                        _state.value = _state.value.copy(isPlaying = false)
                    }
                } else {
                    _state.value = _state.value.copy(isPlaying = false)
                }
            }
            PlaybackMode.REPEAT_ALL -> {
                if (q.isNotEmpty()) {
                    val nextIdx = (currIdx + 1) % q.size
                    q.getOrNull(nextIdx)?.let { playMedia(it, q) }
                } else {
                    _state.value = _state.value.copy(isPlaying = false)
                }
            }
            PlaybackMode.STOP_ON_FINISH -> {
                // User explicitly selected single track play: music complete hone ke baad band ho jata hai
                _state.value = _state.value.copy(isPlaying = false)
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val pos = player.currentPosition.coerceAtLeast(0L)
                        val rawDur = player.duration
                        val dur = if (rawDur > 0 && rawDur != C.TIME_UNSET) {
                            rawDur
                        } else {
                            _state.value.durationMs.coerceAtLeast(0L)
                        }
                        _state.value = _state.value.copy(
                            currentPositionMs = pos,
                            durationMs = dur
                        )
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    private var virtualizer: android.media.audiofx.Virtualizer? = null
    private var bassBoost: android.media.audiofx.BassBoost? = null
    private var boundAudioSessionId: Int = C.AUDIO_SESSION_ID_UNSET
    private var isDolbyEnabled: Boolean = true
    private var currentDolbyProfile: String = "Dynamic"
    private var currentDolbyBass: Boolean = true
    private var currentDolbyVirtualizer: Boolean = true

    fun setDolbyAtmos(
        enabled: Boolean,
        profile: String = "Dynamic",
        enableBass: Boolean = true,
        enableVirtualizer: Boolean = true
    ) {
        isDolbyEnabled = enabled
        currentDolbyProfile = profile
        currentDolbyBass = enableBass
        currentDolbyVirtualizer = enableVirtualizer
        applyDolbyEffects(exoPlayer?.audioSessionId ?: C.AUDIO_SESSION_ID_UNSET)
    }

    private fun releaseAudioEffects() {
        try {
            virtualizer?.release()
        } catch (_: Throwable) {}
        virtualizer = null

        try {
            bassBoost?.release()
        } catch (_: Throwable) {}
        bassBoost = null
        boundAudioSessionId = C.AUDIO_SESSION_ID_UNSET
    }

    private fun applyDolbyEffects(sessionId: Int = exoPlayer?.audioSessionId ?: C.AUDIO_SESSION_ID_UNSET) {
        if (sessionId <= 0 || sessionId == C.AUDIO_SESSION_ID_UNSET) return

        try {
            if (!isDolbyEnabled) {
                try { virtualizer?.enabled = false } catch (_: Throwable) {}
                try { bassBoost?.enabled = false } catch (_: Throwable) {}
                return
            }

            // If ExoPlayer switched to a new AudioSessionId across tracks, release old effects first
            if (boundAudioSessionId != sessionId) {
                releaseAudioEffects()
                boundAudioSessionId = sessionId
            }

            try {
                if (virtualizer == null) {
                    virtualizer = android.media.audiofx.Virtualizer(0, sessionId)
                }
                virtualizer?.let { virt ->
                    virt.enabled = currentDolbyVirtualizer
                    if (currentDolbyVirtualizer && virt.strengthSupported) {
                        virt.setStrength(1000.toShort())
                    }
                }
            } catch (_: Throwable) {
                try { virtualizer?.release() } catch (_: Throwable) {}
                virtualizer = null
            }

            try {
                if (bassBoost == null) {
                    bassBoost = android.media.audiofx.BassBoost(0, sessionId)
                }
                bassBoost?.let { bass ->
                    bass.enabled = currentDolbyBass
                    if (currentDolbyBass && bass.strengthSupported) {
                        val strength = when (currentDolbyProfile) {
                            "Movie" -> 850.toShort()
                            "Music" -> 900.toShort()
                            "Voice" -> 250.toShort()
                            else -> 800.toShort()
                        }
                        bass.setStrength(strength)
                    }
                }
            } catch (_: Throwable) {
                try { bassBoost?.release() } catch (_: Throwable) {}
                bassBoost = null
            }
        } catch (_: Throwable) {
            // Safely ignore hardware DSP limitations on emulators or unsupported OEM HALs
        }
    }

    fun stopPlayback() {
        stopProgressTracker()
        exoPlayer?.pause()
        _state.value = _state.value.copy(
            isPlaying = false,
            currentMedia = null,
            currentPositionMs = 0L
        )
    }

    fun release() {
        stopProgressTracker()
        sleepTimerJob?.cancel()
        releaseAudioEffects()
        try {
            exoPlayer?.release()
        } catch (_: Throwable) {}
        exoPlayer = null
    }
}
