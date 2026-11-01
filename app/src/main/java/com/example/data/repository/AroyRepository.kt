package com.example.data.repository

import com.example.data.local.AroyDao
import com.example.data.local.PlaybackHistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.SavedStreamEntity
import com.example.data.model.FolderItem
import com.example.data.model.MediaItemData
import kotlinx.coroutines.flow.Flow

class AroyRepository(
    private val scanner: MediaStoreScanner,
    private val dao: AroyDao
) {
    val allHistory: Flow<List<PlaybackHistoryEntity>> = dao.getAllHistory()
    val videoHistory: Flow<List<PlaybackHistoryEntity>> = dao.getVideoHistory()
    val audioHistory: Flow<List<PlaybackHistoryEntity>> = dao.getAudioHistory()
    val allPlaylists: Flow<List<PlaylistEntity>> = dao.getAllPlaylists()
    val savedStreams: Flow<List<SavedStreamEntity>> = dao.getAllSavedStreams()

    suspend fun scanVideos(): List<MediaItemData> = scanner.scanVideos()

    suspend fun scanAudios(): List<MediaItemData> = scanner.scanAudios()

    fun extractFolders(items: List<MediaItemData>, isVideo: Boolean): List<FolderItem> =
        scanner.extractFolders(items, isVideo)

    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> =
        dao.getItemsForPlaylist(playlistId)

    suspend fun recordHistory(item: MediaItemData, currentPositionMs: Long) {
        val entity = PlaybackHistoryEntity(
            mediaUri = item.uri,
            mediaId = item.id,
            title = item.title,
            artist = item.artist,
            durationMs = item.durationMs,
            lastPositionMs = currentPositionMs,
            lastPlayedTimestamp = System.currentTimeMillis(),
            isVideo = item.isVideo,
            folderName = item.folderName,
            thumbnailUri = item.thumbnailUri,
            isOnlineStream = item.isOnlineStream
        )
        dao.upsertHistory(entity)
    }

    suspend fun getResumePosition(uri: String): Long {
        val history = dao.getHistoryForUri(uri)
        return history?.lastPositionMs ?: 0L
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        val safeName = name.trim().take(80)
        val safeDesc = description.trim().take(250)
        return dao.insertPlaylist(PlaylistEntity(name = safeName, description = safeDesc))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deleteAllItemsInPlaylist(playlistId)
        dao.deletePlaylist(playlistId)
    }

    suspend fun addItemToPlaylist(playlistId: Long, item: MediaItemData) {
        val playlistItem = PlaylistItemEntity(
            playlistId = playlistId,
            mediaUri = item.uri,
            mediaId = item.id,
            title = item.title.trim().take(120),
            artist = item.artist.trim().take(120),
            durationMs = item.durationMs.coerceAtLeast(0L),
            isVideo = item.isVideo,
            thumbnailUri = item.thumbnailUri
        )
        dao.insertPlaylistItem(playlistItem)
    }

    suspend fun removePlaylistItem(itemId: Long) {
        dao.deletePlaylistItem(itemId)
    }

    suspend fun saveStream(title: String, url: String, type: String, category: String = "Online"): Long {
        val safeTitle = title.trim().take(100)
        val safeUrl = url.trim().take(2048)
        val safeType = type.trim().take(20)
        val safeCat = category.trim().take(50)
        return dao.insertSavedStream(
            SavedStreamEntity(
                title = safeTitle,
                streamUrl = safeUrl,
                streamType = safeType,
                category = safeCat
            )
        )
    }

    suspend fun deleteStream(streamId: Long) {
        dao.deleteSavedStream(streamId)
    }
}
