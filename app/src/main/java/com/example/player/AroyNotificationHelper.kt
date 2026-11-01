package com.example.player

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
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.MainActivity
import com.example.R

class AroyNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    var mediaSession: MediaSessionCompat? = null
        private set

    companion object {
        const val CHANNEL_ID = "aroy_media_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREV = "com.example.ACTION_PREV"
        const val ACTION_STOP = "com.example.ACTION_STOP"
    }

    init {
        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        try {
            mediaSession = MediaSessionCompat(context, "AroyMediaSession").apply {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        }
    }

    fun updateNotification(snapshot: PlaybackSnapshot) {
        val media = snapshot.currentMedia
        if (media == null) {
            dismissNotification()
            return
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
            .setState(playbackState, snapshot.currentPositionMs, snapshot.playbackSpeed)
        mediaSession?.setPlaybackState(stateBuilder.build())

        // Update Media Metadata (Duration, Title, Artist, Album)
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, media.title)
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                if (media.isVideo) media.folderName else media.artist
            )
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, media.album)
            .putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                if (snapshot.durationMs > 0) snapshot.durationMs else media.durationMs
            )
        mediaSession?.setMetadata(metadataBuilder.build())

        // Intent to re-open MainActivity on tapping notification
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action intents for Previous, Play/Pause, Next, Stop
        val prevPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, NotificationActionReceiver::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPausePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, NotificationActionReceiver::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            Intent(context, NotificationActionReceiver::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            Intent(context, NotificationActionReceiver::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (snapshot.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        // Native Android MediaStyle Notification
        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 1, 2) // Prev (0), Play/Pause (1), Next (2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopPendingIntent)

        mediaSession?.sessionToken?.let { token ->
            mediaStyle.setMediaSession(token)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(media.title)
            .setContentText(if (media.isVideo) "${media.folderName} • Video" else "${media.artist} • Audio")
            .setSubText(if (snapshot.isPlaying) "Playing" else "Paused")
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setOngoing(snapshot.isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(mediaStyle)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, if (snapshot.isPlaying) "Pause" else "Play", playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

        try {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet
        }
    }

    fun dismissNotification() {
        try {
            val stateBuilder = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, 0L, 0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun release() {
        dismissNotification()
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }
}
