package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AroyDao {

    // --- Playback History ---
    @Query("SELECT * FROM playback_history ORDER BY lastPlayedTimestamp DESC")
    fun getAllHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE isVideo = 1 ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getVideoHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE isVideo = 0 ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getAudioHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE mediaUri = :uri LIMIT 1")
    suspend fun getHistoryForUri(uri: String): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE mediaUri = :uri")
    suspend fun deleteHistoryByUri(uri: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearAllHistory()

    // --- Playlists ---
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getItemsForPlaylist(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE id = :itemId")
    suspend fun deletePlaylistItem(itemId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun deleteAllItemsInPlaylist(playlistId: Long)

    // --- Saved Streams ---
    @Query("SELECT * FROM saved_streams ORDER BY createdAt DESC")
    fun getAllSavedStreams(): Flow<List<SavedStreamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedStream(stream: SavedStreamEntity): Long

    @Query("DELETE FROM saved_streams WHERE id = :streamId")
    suspend fun deleteSavedStream(streamId: Long)
}
