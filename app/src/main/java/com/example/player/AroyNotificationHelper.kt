package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.MainActivity
import com.example.R

class AroyNotificationHelper(context: Context) {

    private val appContext = context.applicationContext

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    var mediaSession: MediaSessionCompat? = null
        private set

    private var lastNotifiedUri: String? = null
    private var lastNotifiedPlaying: Boolean? = null
    private var lastNotifiedDuration: Long = -1L
    private var lastNotifiedSpeed: Float = -1f
    private var lastNotifiedPositionMs: Long = -1L
    private var lastNotifiedTimeMs: Long = 0L

    private val contentPendingIntent: PendingIntent by lazy {
        val openAppIntent = Intent(appContext, MainActivity::class.java).apply {
            setPackage(appContext.packageName)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        PendingIntent.getActivity(
            appContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val prevPendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            appContext,
            1,
            Intent(appContext, NotificationActionReceiver::class.java).apply {
                setPackage(appContext.packageName)
                action = ACTION_PREV
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val playPendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            appContext,
            2,
            Intent(appContext, NotificationActionReceiver::class.java).apply {
                setPackage(appContext.packageName)
                action = ACTION_PLAY
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val pausePendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            appContext,
            5,
            Intent(appContext, NotificationActionReceiver::class.java).apply {
                setPackage(appContext.packageName)
                action = ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val nextPendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            appContext,
            3,
            Intent(appContext, NotificationActionReceiver::class.java).apply {
                setPackage(appContext.packageName)
                action = ACTION_NEXT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val stopPendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            appContext,
            4,
            Intent(appContext, NotificationActionReceiver::class.java).apply {
                setPackage(appContext.packageName)
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "aroy_media_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREV = "com.example.ACTION_PREV"
        const val ACTION_STOP = "com.example.ACTION_STOP"

        @Volatile
        var latestNotification: Notification? = null

        @Volatile
        private var instance: AroyNotificationHelper? = null

        fun getInstance(context: Context): AroyNotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: AroyNotificationHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        try {
            mediaSession = MediaSessionCompat(appContext, "AroyMediaSession").apply {
                isActive = true
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        NotificationActionReceiver.playerManager?.resume()
                    }

                    override fun onPause() {
                        NotificationActionReceiver.playerManager?.pause()
                    }

                    override fun onSkipToNext() {
                        NotificationActionReceiver.playerManager?.playNext()
                    }

                    override fun onSkipToPrevious() {
                        NotificationActionReceiver.playerManager?.playPrevious()
                    }

                    override fun onSeekTo(pos: Long) {
                        NotificationActionReceiver.playerManager?.seekTo(pos)
                    }

                    override fun onFastForward() {
                        NotificationActionReceiver.playerManager?.forward10s()
                    }

                    override fun onRewind() {
                        NotificationActionReceiver.playerManager?.rewind10s()
                    }

                    override fun onStop() {
                        NotificationActionReceiver.playerManager?.pause()
                        dismissNotification()
                    }
                })
            }
        } catch (_: Throwable) {
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Aroy Media Playback Notification",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Native System Media Controls for Notification Shade & Lock Screen"
                    setShowBadge(false)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            } catch (_: Throwable) {
            }
        }
    }

    fun updateNotification(snapshot: PlaybackSnapshot, isPrivateMedia: Boolean = false) {
        val media = snapshot.currentMedia
        if (media == null) {
            dismissNotification()
            return
        }

        val now = System.currentTimeMillis()
        val effectiveDuration = if (snapshot.durationMs > 0) snapshot.durationMs else media.durationMs
        val songChanged = lastNotifiedUri != media.uri
        val stateChanged = lastNotifiedPlaying != snapshot.isPlaying
        val durationChanged = lastNotifiedDuration != effectiveDuration && effectiveDuration > 0
        val speedChanged = lastNotifiedSpeed != snapshot.playbackSpeed

        // Detect if user manually scrubbed/seeked (position jumped more than 3 seconds from expected linear progress)
        val elapsedMs = (now - lastNotifiedTimeMs).coerceAtLeast(0L)
        val expectedPos = if (lastNotifiedPlaying == true) {
            lastNotifiedPositionMs + (elapsedMs * snapshot.playbackSpeed).toLong()
        } else {
            lastNotifiedPositionMs
        }
        val seekJumped = lastNotifiedPositionMs >= 0L && kotlin.math.abs(snapshot.currentPositionMs - expectedPos) > 3000L

        // Return early on regular 250ms progress ticks to prevent Binder IPC & PendingIntent exhaustion
        if (!songChanged && !stateChanged && !durationChanged && !speedChanged && !seekJumped) {
            return
        }

        lastNotifiedUri = media.uri
        lastNotifiedPlaying = snapshot.isPlaying
        lastNotifiedDuration = effectiveDuration
        lastNotifiedSpeed = snapshot.playbackSpeed
        lastNotifiedPositionMs = snapshot.currentPositionMs
        lastNotifiedTimeMs = now

        val safeTitle = if (isPrivateMedia) "Privacy Vault Media" else media.title
        val safeSub = if (isPrivateMedia) {
            "Protected Playback"
        } else if (media.isVideo) {
            media.folderName
        } else {
            media.artist
        }

        try {
            if (mediaSession?.isActive != true) {
                mediaSession?.isActive = true
            }

            // Update MediaSession Playback State so system notification displays live seekbar & controls
            val stateActions = PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_FAST_FORWARD or
                    PlaybackStateCompat.ACTION_REWIND or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_STOP

            val playbackState = if (snapshot.isPlaying) {
                PlaybackStateCompat.STATE_PLAYING
            } else {
                PlaybackStateCompat.STATE_PAUSED
            }

            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(stateActions)
                .setState(playbackState, snapshot.currentPositionMs.coerceAtLeast(0L), snapshot.playbackSpeed)
            mediaSession?.setPlaybackState(stateBuilder.build())

            // Update Media Metadata only when song or duration changes
            if (songChanged || durationChanged) {
                val metadataBuilder = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, safeTitle)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, safeSub)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, if (isPrivateMedia) "Vault" else media.album)
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        effectiveDuration.coerceAtLeast(0L)
                    )
                mediaSession?.setMetadata(metadataBuilder.build())
            }

            // Rebuild & post System Notification only when song or play/pause state changes
            if (songChanged || stateChanged) {
                val playPausePendingIntent = if (snapshot.isPlaying) pausePendingIntent else playPendingIntent
                val playPauseIcon = if (snapshot.isPlaying) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                }

                val mediaStyle = MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2) // Prev (0), Play/Pause (1), Next (2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(stopPendingIntent)

                mediaSession?.sessionToken?.let { token ->
                    mediaStyle.setMediaSession(token)
                }

                val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(safeTitle)
                    .setContentText(if (isPrivateMedia) "Privacy Vault • Protected" else if (media.isVideo) "${media.folderName} • Video" else "${media.artist} • Audio")
                    .setSubText(if (snapshot.isPlaying) "Playing" else "Paused")
                    .setContentIntent(contentPendingIntent)
                    .setDeleteIntent(stopPendingIntent)
                    .setOngoing(snapshot.isPlaying)
                    .setOnlyAlertOnce(true)
                    .setVisibility(
                        if (isPrivateMedia) NotificationCompat.VISIBILITY_SECRET else NotificationCompat.VISIBILITY_PUBLIC
                    )
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setStyle(mediaStyle)
                    .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                    .addAction(playPauseIcon, if (snapshot.isPlaying) "Pause" else "Play", playPausePendingIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                    .build()

                latestNotification = notification
                notificationManager.notify(NOTIFICATION_ID, notification)

                // Ensure Foreground Service is started and keeping process alive in background
                if (snapshot.isPlaying || AroyPlaybackService.isServiceRunning) {
                    try {
                        val serviceIntent = Intent(appContext, AroyPlaybackService::class.java).apply {
                            action = AroyPlaybackService.ACTION_START_FOREGROUND
                        }
                        if (snapshot.isPlaying && !AroyPlaybackService.isServiceRunning) {
                            ContextCompat.startForegroundService(appContext, serviceIntent)
                        } else if (AroyPlaybackService.isServiceRunning) {
                            appContext.startService(serviceIntent)
                        }
                    } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {
            // Safely ignore SecurityException or Binder IPC transient errors
        }
    }

    fun dismissNotification() {
        try {
            if (lastNotifiedUri == null && lastNotifiedPlaying == null && !AroyPlaybackService.isServiceRunning) return
            lastNotifiedUri = null
            lastNotifiedPlaying = null
            lastNotifiedDuration = -1L
            lastNotifiedSpeed = -1f
            lastNotifiedPositionMs = -1L
            lastNotifiedTimeMs = 0L
            latestNotification = null
            val stateBuilder = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, 0L, 0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
            notificationManager.cancel(NOTIFICATION_ID)
            if (AroyPlaybackService.isServiceRunning) {
                try {
                    val stopIntent = Intent(appContext, AroyPlaybackService::class.java).apply {
                        action = AroyPlaybackService.ACTION_STOP_FOREGROUND
                    }
                    appContext.startService(stopIntent)
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {
            // ignore
        }
    }

    fun release() {
        dismissNotification()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
        } catch (_: Throwable) {}
        mediaSession = null
    }
}
