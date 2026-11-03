package com.example.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        var playerManager: PlayerManager? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val manager = playerManager ?: return
        when (intent?.action) {
            AroyNotificationHelper.ACTION_PLAY -> {
                manager.resume()
            }
            AroyNotificationHelper.ACTION_PAUSE -> {
                manager.pause()
            }
            AroyNotificationHelper.ACTION_PLAY_PAUSE -> {
                manager.playPauseToggle()
            }
            AroyNotificationHelper.ACTION_NEXT -> {
                manager.playNext()
            }
            AroyNotificationHelper.ACTION_PREV -> {
                manager.playPrevious()
            }
            AroyNotificationHelper.ACTION_STOP -> {
                manager.pause()
            }
        }
    }
}
