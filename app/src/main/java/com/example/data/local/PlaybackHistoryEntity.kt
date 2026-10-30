package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey
    val mediaUri: String,
    val mediaId: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val lastPositionMs: Long,
    val lastPlayedTimestamp: Long,
    val isVideo: Boolean,
    val folderName: String,
    val thumbnailUri: String?,
    val isOnlineStream: Boolean
)
