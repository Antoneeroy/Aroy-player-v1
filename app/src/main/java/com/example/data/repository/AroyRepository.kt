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
        return dao.insertPlaylist(PlaylistEntity(name = name, description = description))
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
            title = item.title,
            artist = item.artist,
            durationMs = item.durationMs,
            isVideo = item.isVideo,
            thumbnailUri = item.thumbnailUri
        )
        dao.insertPlaylistItem(playlistItem)
    }

    suspend fun removePlaylistItem(itemId: Long) {
        dao.deletePlaylistItem(itemId)
    }

    suspend fun saveStream(title: String, url: String, type: String, category: String = "Online"): Long {
        return dao.insertSavedStream(
            SavedStreamEntity(
                title = title,
                streamUrl = url,
                streamType = type,
                category = category
            )
        )
    }

    suspend fun deleteStream(streamId: Long) {
        dao.deleteSavedStream(streamId)
    }
}
