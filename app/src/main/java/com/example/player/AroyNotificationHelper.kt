package com.example.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class AroyNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "aroy_media_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREV = "com.example.ACTION_PREV"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aroy Media Playback Notification",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media controls and playback status widget"
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

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(media.title)
            .setContentText(if (media.isVideo) "${media.folderName} • Video" else "${media.artist} • Audio")
            .setSubText(if (snapshot.isPlaying) "Playing" else "Paused")
            .setContentIntent(contentPendingIntent)
            .setOngoing(snapshot.isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(if (media.isVideo) "${media.folderName} • ${media.formattedDuration}" else "${media.artist} • ${media.formattedDuration}")
            )

        // Previous Action
        val prevIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.addAction(
            android.R.drawable.ic_media_previous,
            "Previous",
            prevPendingIntent
        )

        // Play / Pause Action
        val playPauseIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIcon = if (snapshot.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        notificationBuilder.addAction(
            playPauseIcon,
            if (snapshot.isPlaying) "Pause" else "Play",
            playPausePendingIntent
        )

        // Next Action
        val nextIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.addAction(
            android.R.drawable.ic_media_next,
            "Next",
            nextPendingIntent
        )

        try {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet
        }
    }

    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
