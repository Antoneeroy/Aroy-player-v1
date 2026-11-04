package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.R

class AroyPlaybackService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    companion object {
        const val ACTION_START_FOREGROUND = "com.example.player.ACTION_START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "com.example.player.ACTION_STOP_FOREGROUND"

        @Volatile
        var isServiceRunning: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        acquireLocks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_FOREGROUND) {
            stopForegroundServiceCleanly()
            return START_NOT_STICKY
        }

        isServiceRunning = true
        acquireLocks()

        val notification = AroyNotificationHelper.latestNotification ?: buildFallbackNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    AroyNotificationHelper.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(AroyNotificationHelper.NOTIFICATION_ID, notification)
            }
        } catch (_: Throwable) {
            // Safely handle ForegroundServiceStartNotAllowedException on restricted OEM states
        }

        // START_STICKY ensures Android OS keeps background music alive across memory pressure
        return START_STICKY
    }

    private fun acquireLocks() {
        try {
            if (wakeLock == null) {
                val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "AroyPlayer::MediaPlaybackWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(6 * 60 * 60 * 1000L) // Up to 6 hours continuous playback guard
            }
        } catch (_: Throwable) {}

        try {
            if (wifiLock == null) {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                @Suppress("DEPRECATION")
                val lockMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    WifiManager.WIFI_MODE_FULL_LOW_LATENCY
                } else {
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF
                }
                wifiLock = wifiManager?.createWifiLock(lockMode, "AroyPlayer::MediaWifiLock")?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wifiLock?.isHeld == false) {
                wifiLock?.acquire()
            }
        } catch (_: Throwable) {}
    }

    private fun releaseLocks() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Throwable) {}
        wakeLock = null

        try {
            if (wifiLock?.isHeld == true) {
                wifiLock?.release()
            }
        } catch (_: Throwable) {}
        wifiLock = null
    }

    private fun buildFallbackNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    AroyNotificationHelper.CHANNEL_ID,
                    "Aroy Media Playback Notification",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    setShowBadge(false)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(channel)
            } catch (_: Throwable) {}
        }

        val currentMedia = NotificationActionReceiver.playerManager?.state?.value?.currentMedia
        return NotificationCompat.Builder(this, AroyNotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(currentMedia?.title ?: "Aroy Player")
            .setContentText(currentMedia?.artist ?: "Background Audio Playback")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun stopForegroundServiceCleanly() {
        isServiceRunning = false
        releaseLocks()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Throwable) {}
        try {
            stopSelf()
        } catch (_: Throwable) {}
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val isPlaying = NotificationActionReceiver.playerManager?.state?.value?.isPlaying == true
        if (!isPlaying) {
            stopForegroundServiceCleanly()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        isServiceRunning = false
        releaseLocks()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
