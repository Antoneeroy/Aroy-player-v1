package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.model.FolderItem
import com.example.data.model.MediaItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreScanner(private val context: Context) {

    suspend fun scanVideos(): List<MediaItemData> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<MediaItemData>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            } else {
                MediaStore.Video.Media.DATA
            }
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleColumn = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val bucketColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                } else {
                    cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                }

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Video $id"
                    val title = if (titleColumn >= 0) cursor.getString(titleColumn) ?: name else name
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateColumn)
                    val mimeType = cursor.getString(mimeColumn) ?: "video/mp4"

                    val width = if (widthColumn >= 0) cursor.getInt(widthColumn) else 0
                    val height = if (heightColumn >= 0) cursor.getInt(heightColumn) else 0
                    val resolution = if (width > 0 && height > 0) "${width}x${height}" else ""

                    val folderName = if (bucketColumn >= 0) {
                        val bucketVal = cursor.getString(bucketColumn)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            bucketVal ?: "Movies"
                        } else {
                            bucketVal?.let { File(it).parentFile?.name } ?: "Movies"
                        }
                    } else "Movies"

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    videoList.add(
                        MediaItemData(
                            id = id.toString(),
                            uri = contentUri.toString(),
                            title = title,
                            artist = folderName,
                            album = "Video",
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            folderName = folderName,
                            mimeType = mimeType,
                            isVideo = true,
                            resolution = resolution,
                            thumbnailUri = contentUri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or query exception
        }

        // If no videos found on device/emulator, add high quality open samples
        if (videoList.isEmpty()) {
            videoList.addAll(getSampleVideos())
        }

        videoList
    }

    suspend fun scanAudios(): List<MediaItemData> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<MediaItemData>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
            } else {
                MediaStore.Audio.Media.DATA
            }
        )

        val selection = "${MediaStore.Audio.Media.DURATION} >= 10000" // at least 10s
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val bucketColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                } else {
                    cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                }

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Audio $id"
                    val title = if (titleColumn >= 0) cursor.getString(titleColumn) ?: name else name
                    val artist = if (artistColumn >= 0) cursor.getString(artistColumn) ?: "Unknown Artist" else "Unknown Artist"
                    val album = if (albumColumn >= 0) cursor.getString(albumColumn) ?: "Unknown Album" else "Unknown Album"
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateColumn)
                    val mimeType = cursor.getString(mimeColumn) ?: "audio/mp3"

                    val folderName = if (bucketColumn >= 0) {
                        val bucketVal = cursor.getString(bucketColumn)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            bucketVal ?: "Music"
                        } else {
                            bucketVal?.let { File(it).parentFile?.name } ?: "Music"
                        }
                    } else "Music"

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    audioList.add(
                        MediaItemData(
                            id = id.toString(),
                            uri = contentUri.toString(),
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = album,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            folderName = folderName,
                            mimeType = mimeType,
                            isVideo = false,
                            thumbnailUri = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or query exception
        }

        // If no audios found, provide sample tracks so user can test MP3 playback immediately
        if (audioList.isEmpty()) {
            audioList.addAll(getSampleAudios())
        }

        audioList
    }

    fun extractFolders(items: List<MediaItemData>, isVideo: Boolean): List<FolderItem> {
        val grouped = items.groupBy { it.folderName }
        return grouped.map { (folderName, folderItems) ->
            FolderItem(
                name = folderName,
                path = folderName,
                itemCount = folderItems.size,
                isVideoFolder = isVideo,
                sampleThumbnailUri = folderItems.firstOrNull { it.thumbnailUri != null }?.thumbnailUri
            )
        }.sortedByDescending { it.itemCount }
    }

    private fun getSampleVideos(): List<MediaItemData> {
        return listOf(
            MediaItemData(
                id = "sample_vid_1",
                uri = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                title = "Big Buck Bunny (Animation)",
                artist = "Blender Foundation",
                album = "Open Movies",
                durationMs = 596000L,
                sizeBytes = 158000000L,
                dateAdded = System.currentTimeMillis() / 1000,
                folderName = "Downloads",
                mimeType = "video/mp4",
                isVideo = true,
                resolution = "1920x1080",
                thumbnailUri = "android.resource://${context.packageName}/drawable/img_bunny_poster"
            ),
            MediaItemData(
                id = "sample_vid_2",
                uri = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                title = "Elephants Dream (Sci-Fi CGI)",
                artist = "Orange Open Movie",
                album = "Open Movies",
                durationMs = 653000L,
                sizeBytes = 175000000L,
                dateAdded = (System.currentTimeMillis() - 86400000) / 1000,
                folderName = "Movies",
                mimeType = "video/mp4",
                isVideo = true,
                resolution = "1920x1080",
                thumbnailUri = "android.resource://${context.packageName}/drawable/img_scifi_poster"
            ),
            MediaItemData(
                id = "sample_vid_3",
                uri = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                title = "Chromecast Demo: For Bigger Blazes",
                artist = "Google Samples",
                album = "Camera",
                durationMs = 15000L,
                sizeBytes = 15000000L,
                dateAdded = (System.currentTimeMillis() - 172800000) / 1000,
                folderName = "Camera",
                mimeType = "video/mp4",
                isVideo = true,
                resolution = "1280x720",
                thumbnailUri = "android.resource://${context.packageName}/drawable/img_flame_poster"
            ),
            MediaItemData(
                id = "sample_vid_4",
                uri = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                title = "Tears of Steel (VFX Short Film)",
                artist = "Mango Open Movie",
                album = "Downloads",
                durationMs = 734000L,
                sizeBytes = 220000000L,
                dateAdded = (System.currentTimeMillis() - 259200000) / 1000,
                folderName = "Downloads",
                mimeType = "video/mp4",
                isVideo = true,
                resolution = "1920x1080",
                thumbnailUri = "android.resource://${context.packageName}/drawable/img_vfx_poster"
            )
        )
    }

    private fun getSampleAudios(): List<MediaItemData> {
        return listOf(
            MediaItemData(
                id = "sample_aud_1",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                title = "Aroy Chill Synth Wave 01",
                artist = "SoundHelix Project",
                album = "Midnight Sessions",
                durationMs = 372000L,
                sizeBytes = 8900000L,
                dateAdded = System.currentTimeMillis() / 1000,
                folderName = "Music",
                mimeType = "audio/mp3",
                isVideo = false,
                thumbnailUri = "android.resource://${context.packageName}/drawable/img_music_vinyl"
            ),
            MediaItemData(
                id = "sample_aud_2",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                title = "Electronic Sunset Groove",
                artist = "Electro Beats Lab",
                album = "Neon Horizon",
                durationMs = 423000L,
                sizeBytes = 10100000L,
                dateAdded = (System.currentTimeMillis() - 86400000) / 1000,
                folderName = "Downloads",
                mimeType = "audio/mp3",
                isVideo = false,
                thumbnailUri = "android.resource://${context.packageName}/drawable/img_music_vinyl"
            ),
            MediaItemData(
                id = "sample_aud_3",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                title = "Acoustic Serenade in D Minor",
                artist = "Aroy Acoustics",
                album = "Acoustic Gems",
                durationMs = 345000L,
                sizeBytes = 8200000L,
                dateAdded = (System.currentTimeMillis() - 172800000) / 1000,
                folderName = "WhatsApp Audio",
                mimeType = "audio/mp3",
                isVideo = false,
                thumbnailUri = "android.resource://${context.packageName}/drawable/img_music_vinyl"
            )
        )
    }
}
