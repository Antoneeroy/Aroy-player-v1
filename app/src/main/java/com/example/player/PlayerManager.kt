package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.MediaItemData
import com.example.data.model.PlayerRepeatMode
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

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
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

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val duration = if (player.duration > 0) player.duration else _state.value.currentMedia?.durationMs ?: 0L
                _state.value = _state.value.copy(
                    isBuffering = isBuffering,
                    durationMs = duration
                )

                if (playbackState == Player.STATE_ENDED) {
                    onTrackEnded()
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
                _state.value = _state.value.copy(currentPositionMs = newPosition.positionMs)
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

        player.setMediaItem(mediaItemBuilder.build())
        player.prepare()
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.play()
        applyDolbyEffects()

        _state.value = _state.value.copy(
            currentMedia = item,
            queue = fullQueue,
            queueIndex = index,
            currentPositionMs = startPositionMs,
            durationMs = item.durationMs,
            isAudioSessionActive = !item.isVideo,
            hasSubtitle = subtitleUri != null,
            errorMessage = null
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun retry() {
        val current = _state.value.currentMedia ?: return
        val pos = _state.value.currentPositionMs
        playMedia(current, _state.value.queue, pos)
    }

    fun playPauseToggle() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
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

    fun toggleRepeatMode() {
        val nextMode = when (_state.value.repeatMode) {
            PlayerRepeatMode.OFF -> PlayerRepeatMode.ALL
            PlayerRepeatMode.ALL -> PlayerRepeatMode.ONE
            PlayerRepeatMode.ONE -> PlayerRepeatMode.OFF
        }
        setRepeatMode(nextMode)
    }

    fun setRepeatMode(mode: PlayerRepeatMode) {
        val player = exoPlayer ?: return
        when (mode) {
            PlayerRepeatMode.OFF -> player.repeatMode = Player.REPEAT_MODE_OFF
            PlayerRepeatMode.ALL -> player.repeatMode = Player.REPEAT_MODE_ALL
            PlayerRepeatMode.ONE -> player.repeatMode = Player.REPEAT_MODE_ONE
        }
        _state.value = _state.value.copy(repeatMode = mode)
    }

    fun playNext() {
        val q = _state.value.queue
        val currIdx = _state.value.queueIndex
        if (q.isNotEmpty()) {
            val nextIdx = (currIdx + 1) % q.size
            playMedia(q[nextIdx], q)
        }
    }

    fun playPrevious() {
        val q = _state.value.queue
        val currIdx = _state.value.queueIndex
        if (q.isNotEmpty()) {
            val prevIdx = if (currIdx - 1 < 0) q.size - 1 else currIdx - 1
            playMedia(q[prevIdx], q)
        }
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
        if (_state.value.repeatMode == PlayerRepeatMode.ONE) {
            seekTo(0)
            resume()
        } else if (_state.value.queue.size > 1 && _state.value.repeatMode == PlayerRepeatMode.ALL) {
            playNext()
        } else {
            _state.value = _state.value.copy(isPlaying = false)
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val dur = if (player.duration > 0) player.duration else _state.value.durationMs
                    _state.value = _state.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur
                    )
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
        applyDolbyEffects()
    }

    private fun applyDolbyEffects() {
        val player = exoPlayer ?: return
        val sessionId = player.audioSessionId
        if (sessionId <= 0) return

        try {
            if (isDolbyEnabled) {
                if (virtualizer == null) {
                    virtualizer = android.media.audiofx.Virtualizer(0, sessionId)
                }
                virtualizer?.enabled = currentDolbyVirtualizer
                if (currentDolbyVirtualizer) {
                    virtualizer?.setStrength(1000.toShort())
                }

                if (bassBoost == null) {
                    bassBoost = android.media.audiofx.BassBoost(0, sessionId)
                }
                bassBoost?.enabled = currentDolbyBass
                if (currentDolbyBass) {
                    val strength = when (currentDolbyProfile) {
                        "Movie" -> 850.toShort()
                        "Music" -> 900.toShort()
                        "Voice" -> 250.toShort()
                        else -> 800.toShort()
                    }
                    bassBoost?.setStrength(strength)
                }
            } else {
                virtualizer?.enabled = false
                bassBoost?.enabled = false
            }
        } catch (e: Exception) {
            // Emulators or unsupported devices
        }
    }

    fun stopPlayback() {
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
        try {
            virtualizer?.release()
            bassBoost?.release()
            virtualizer = null
            bassBoost = null
        } catch (e: Exception) {
            // ignore
        }
        exoPlayer?.release()
        exoPlayer = null
    }
}
