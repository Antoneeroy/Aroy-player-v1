package com.example.data.model

import java.util.Locale
import java.util.concurrent.TimeUnit

data class MediaItemData(
    val id: String,
    val uri: String,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val folderName: String = "Storage",
    val mimeType: String = "",
    val isVideo: Boolean = true,
    val resolution: String = "",
    val thumbnailUri: String? = null,
    val isOnlineStream: Boolean = false,
    val streamType: String? = null // "HLS", "MP4", "AUDIO"
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "--:--"
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return ""
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.getDefault(), "%.1f GB", gb)
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                else -> String.format(Locale.getDefault(), "%.1f KB", kb)
            }
        }
}

data class FolderItem(
    val name: String,
    val path: String,
    val itemCount: Int,
    val isVideoFolder: Boolean,
    val sampleThumbnailUri: String? = null
)

enum class PlayerResizeMode(val title: String) {
    FIT("Fit Screen"),
    ZOOM("Crop / Zoom"),
    FILL("Stretch"),
    RATIO_16_9("16 : 9"),
    RATIO_4_3("4 : 3")
}

enum class PlayerRepeatMode(val title: String) {
    OFF("Repeat Off"),
    ALL("Repeat All"),
    ONE("Repeat One")
}

enum class PlaybackMode(val title: String, val shortDesc: String) {
    ORDER("Sequential Play", "Play in Order (≡↓)"),
    SHUFFLE("Shuffle Play", "Random (🔀)"),
    REPEAT_ALL("Repeat All", "Loop Playlist (🔁)"),
    REPEAT_ONE("Repeat One", "Loop Song (🔂)"),
    STOP_ON_FINISH("Stop on Finish", "Single Song (⏹️)")
}

enum class SortOrder(val title: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    DURATION_DESC("Duration (Longest)"),
    SIZE_DESC("Size (Largest)")
}
