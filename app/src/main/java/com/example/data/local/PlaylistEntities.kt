package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val mediaUri: String,
    val mediaId: String,
    val title: String,
    val artist: String = "",
    val durationMs: Long = 0L,
    val isVideo: Boolean = false,
    val thumbnailUri: String? = null,
    val orderIndex: Int = 0
)

@Entity(tableName = "saved_streams")
data class SavedStreamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val streamUrl: String,
    val streamType: String, // "HLS", "MP4", "AUDIO"
    val category: String = "Custom",
    val createdAt: Long = System.currentTimeMillis()
)
