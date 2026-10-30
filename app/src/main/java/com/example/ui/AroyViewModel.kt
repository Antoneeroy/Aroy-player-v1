package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AroyDatabase
import com.example.data.local.PlaybackHistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.SavedStreamEntity
import com.example.data.model.FolderItem
import com.example.data.model.MediaItemData
import com.example.data.model.PlayerRepeatMode
import com.example.data.model.PlayerResizeMode
import com.example.data.model.SortOrder
import com.example.data.repository.AroyRepository
import com.example.data.repository.MediaStoreScanner
import com.example.player.PlaybackSnapshot
import com.example.player.PlayerManager
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    VIDEOS("Videos"),
    MUSIC("Music"),
    FOLDERS("Folders"),
    ONLINE("Streams"),
    PLAYLISTS("Playlists")
}

enum class BottomNavDestination(val label: String) {
    VIDEO("VIDEO"),
    MUSIC("MUSIC"),
    STREAM("STREAM"),
    ME("ME")
}

enum class VideoSubTab(val title: String) {
    VIDEO("Video"),
    FOLDER("Folder"),
    PLAYLIST("Playlist"),
    STREAM("Streams")
}

data class UiState(
    val selectedTab: MainTab = MainTab.VIDEOS,
    val currentBottomNav: BottomNavDestination = BottomNavDestination.VIDEO,
    val videoSubTab: VideoSubTab = VideoSubTab.VIDEO,
    val videoLayoutColumns: Int = 2, // 1, 2, or 3 ("3 line" layout)
    val isScanning: Boolean = false,
    val hasPermission: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val selectedFolder: FolderItem? = null,
    val isVideoPlayerOpen: Boolean = false,
    val isAudioSheetExpanded: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val resizeMode: PlayerResizeMode = PlayerResizeMode.FIT,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val showSleepTimerDialog: Boolean = false,
    val mediaForPlaylist: MediaItemData? = null,
    val showCreatePlaylistDialog: Boolean = false,
    val showStreamDialog: Boolean = false
)

class AroyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AroyDatabase.getInstance(application)
    private val scanner = MediaStoreScanner(application)
    val repository = AroyRepository(scanner, db.aroyDao())
    val playerManager = PlayerManager(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _rawVideos = MutableStateFlow<List<MediaItemData>>(emptyList())
    private val _rawAudios = MutableStateFlow<List<MediaItemData>>(emptyList())

    val playbackState: StateFlow<PlaybackSnapshot> = playerManager.state

    val history: StateFlow<List<PlaybackHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedStreams: StateFlow<List<SavedStreamEntity>> = repository.savedStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted Videos
    val filteredVideos: StateFlow<List<MediaItemData>> = combine(
        _rawVideos,
        _uiState
    ) { videos, ui ->
        val query = ui.searchQuery.trim().lowercase()
        val filtered = if (query.isEmpty()) videos else {
            videos.filter { it.title.lowercase().contains(query) || it.folderName.lowercase().contains(query) }
        }
        applySort(filtered, ui.sortOrder)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted Audios
    val filteredAudios: StateFlow<List<MediaItemData>> = combine(
        _rawAudios,
        _uiState
    ) { audios, ui ->
        val query = ui.searchQuery.trim().lowercase()
        val filtered = if (query.isEmpty()) audios else {
            audios.filter {
                it.title.lowercase().contains(query) ||
                        it.artist.lowercase().contains(query) ||
                        it.album.lowercase().contains(query)
            }
        }
        applySort(filtered, ui.sortOrder)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Video folders
    val videoFolders: StateFlow<List<FolderItem>> = _rawVideos.combine(_uiState) { videos, _ ->
        repository.extractFolders(videos, true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audio folders
    val audioFolders: StateFlow<List<FolderItem>> = _rawAudios.combine(_uiState) { audios, _ ->
        repository.extractFolders(audios, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        scanStorage()
    }

    fun scanStorage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val vids = repository.scanVideos()
            val auds = repository.scanAudios()
            _rawVideos.value = vids
            _rawAudios.value = auds
            _uiState.value = _uiState.value.copy(isScanning = false)
        }
    }

    fun setHasPermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasPermission = granted)
        if (granted) {
            scanStorage()
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, selectedFolder = null)
    }

    fun selectBottomNav(dest: BottomNavDestination) {
        _uiState.value = _uiState.value.copy(currentBottomNav = dest, selectedFolder = null)
    }

    fun selectVideoSubTab(tab: VideoSubTab) {
        _uiState.value = _uiState.value.copy(videoSubTab = tab, selectedFolder = null)
    }

    fun cycleVideoLayout() {
        val next = when (_uiState.value.videoLayoutColumns) {
            2 -> 3 // 3-line mode requested by user!
            3 -> 1 // 1-line list mode
            else -> 2 // standard 2-line mode
        }
        _uiState.value = _uiState.value.copy(videoLayoutColumns = next)
    }

    fun setVideoLayoutColumns(cols: Int) {
        _uiState.value = _uiState.value.copy(videoLayoutColumns = cols)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
    }

    fun openFolder(folder: FolderItem) {
        _uiState.value = _uiState.value.copy(selectedFolder = folder)
    }

    fun closeFolder() {
        _uiState.value = _uiState.value.copy(selectedFolder = null)
    }

    fun playMediaItem(item: MediaItemData, playlist: List<MediaItemData> = emptyList()) {
        viewModelScope.launch {
            val resumePos = repository.getResumePosition(item.uri)
            playerManager.playMedia(item, playlist, startPositionMs = resumePos)

            if (item.isVideo) {
                _uiState.value = _uiState.value.copy(isVideoPlayerOpen = true)
            } else {
                _uiState.value = _uiState.value.copy(isAudioSheetExpanded = true)
            }
            repository.recordHistory(item, resumePos)
        }
    }

    fun playOnlineStream(url: String, title: String, type: String) {
        val isVid = type.uppercase() != "AUDIO"
        val streamItem = MediaItemData(
            id = "stream_${System.currentTimeMillis()}",
            uri = url,
            title = title,
            artist = "Online Stream ($type)",
            album = "Online",
            durationMs = 0L,
            isVideo = isVid,
            isOnlineStream = true,
            streamType = type
        )
        viewModelScope.launch {
            repository.saveStream(title, url, type, "Online")
            playerManager.playMedia(streamItem)
            if (isVid) {
                _uiState.value = _uiState.value.copy(isVideoPlayerOpen = true)
            } else {
                _uiState.value = _uiState.value.copy(isAudioSheetExpanded = true)
            }
            repository.recordHistory(streamItem, 0L)
        }
    }

    fun resumeFromBeginning() {
        playerManager.seekTo(0)
        playerManager.resume()
    }

    fun closeVideoPlayer() {
        // Save current progress before closing
        val current = playbackState.value.currentMedia
        val pos = playbackState.value.currentPositionMs
        if (current != null) {
            viewModelScope.launch {
                repository.recordHistory(current, pos)
            }
        }
        _uiState.value = _uiState.value.copy(isVideoPlayerOpen = false)
    }

    fun setVideoPlayerOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isVideoPlayerOpen = open)
    }

    fun setAudioSheetExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(isAudioSheetExpanded = expanded)
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsOpen = open)
    }

    fun setResizeMode(mode: PlayerResizeMode) {
        _uiState.value = _uiState.value.copy(resizeMode = mode)
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun toggleDayNightMode() {
        val nextMode = when (_uiState.value.themeMode) {
            AppThemeMode.DARK, AppThemeMode.AMOLED, AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
        }
        _uiState.value = _uiState.value.copy(themeMode = nextMode)
    }

    fun setShowSleepTimerDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSleepTimerDialog = show)
    }

    fun setShowAddToPlaylist(item: MediaItemData?) {
        _uiState.value = _uiState.value.copy(mediaForPlaylist = item)
    }

    fun setShowCreatePlaylist(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreatePlaylistDialog = show)
    }

    fun setShowStreamDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStreamDialog = show)
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
            setShowCreatePlaylist(false)
        }
    }

    fun addToPlaylist(playlistId: Long, item: MediaItemData) {
        viewModelScope.launch {
            repository.addItemToPlaylist(playlistId, item)
            setShowAddToPlaylist(null)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun deleteStream(streamId: Long) {
        viewModelScope.launch {
            repository.deleteStream(streamId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun attachSubtitle(uri: Uri) {
        playerManager.attachSubtitle(uri)
    }

    private fun applySort(items: List<MediaItemData>, sortOrder: SortOrder): List<MediaItemData> {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> items.sortedBy { it.title.lowercase() }
            SortOrder.NAME_DESC -> items.sortedByDescending { it.title.lowercase() }
            SortOrder.DATE_DESC -> items.sortedByDescending { it.dateAdded }
            SortOrder.DATE_ASC -> items.sortedBy { it.dateAdded }
            SortOrder.DURATION_DESC -> items.sortedByDescending { it.durationMs }
            SortOrder.SIZE_DESC -> items.sortedByDescending { it.sizeBytes }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
