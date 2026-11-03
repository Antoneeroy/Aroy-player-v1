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
import com.example.player.AroyNotificationHelper
import com.example.player.NotificationActionReceiver
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
import java.security.MessageDigest
import java.security.SecureRandom

sealed class VaultUnlockResult {
    object Success : VaultUnlockResult()
    data class WrongPin(val attemptsLeft: Int) : VaultUnlockResult()
    data class LockedOut(val secondsLeft: Long) : VaultUnlockResult()
}

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

enum class MusicSubTab(val title: String) {
    MUSIC("Music"),
    PLAYLIST("Playlist"),
    FAVOURITE("Favourite")
}

data class UiState(
    val selectedTab: MainTab = MainTab.VIDEOS,
    val currentBottomNav: BottomNavDestination = BottomNavDestination.VIDEO,
    val videoSubTab: VideoSubTab = VideoSubTab.VIDEO,
    val musicSubTab: MusicSubTab = MusicSubTab.MUSIC,
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
    val showStreamDialog: Boolean = false,
    val showMp3ConverterDialog: Boolean = false,
    val videoForMp3Convert: MediaItemData? = null,
    val dolbyAtmosEnabled: Boolean = true,
    val dolbyProfile: String = "Dynamic",
    val dolbyBassBoost: Boolean = true,
    val dolbySurroundVirtualizer: Boolean = true,
    val favoriteUris: Set<String> = emptySet(),
    val showPrivacyVault: Boolean = false
)

class AroyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AroyDatabase.getInstance(application)
    private val scanner = MediaStoreScanner(application)
    val repository = AroyRepository(scanner, db.aroyDao())
    val playerManager = PlayerManager(application)
    val notificationHelper = AroyNotificationHelper(application)
    private val prefs = application.getSharedPreferences("aroy_settings_pref", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _rawVideos = MutableStateFlow<List<MediaItemData>>(emptyList())
    private val _rawAudios = MutableStateFlow<List<MediaItemData>>(emptyList())

    // Privacy Vault persistent storage
    private val _privateUris = MutableStateFlow<Set<String>>(emptySet())
    val privateUris: StateFlow<Set<String>> = _privateUris.asStateFlow()

    private val _isPrivacyUnlocked = MutableStateFlow<Boolean>(false)
    val isPrivacyUnlocked: StateFlow<Boolean> = _isPrivacyUnlocked.asStateFlow()

    private val _isPrivacyPinSet = MutableStateFlow<Boolean>(false)
    val isPrivacyPinSet: StateFlow<Boolean> = _isPrivacyPinSet.asStateFlow()

    val playbackState: StateFlow<PlaybackSnapshot> = playerManager.state

    val history: StateFlow<List<PlaybackHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedStreams: StateFlow<List<SavedStreamEntity>> = repository.savedStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted Videos (Hides private vault videos from public views)
    val filteredVideos: StateFlow<List<MediaItemData>> = combine(
        _rawVideos,
        _uiState,
        _privateUris
    ) { videos, ui, privates ->
        val nonPrivate = videos.filter { it.uri !in privates }
        val query = ui.searchQuery.trim().lowercase()
        val filtered = if (query.isEmpty()) nonPrivate else {
            nonPrivate.filter { it.title.lowercase().contains(query) || it.folderName.lowercase().contains(query) }
        }
        applySort(filtered, ui.sortOrder)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted Audios (Hides private vault music from public views)
    val filteredAudios: StateFlow<List<MediaItemData>> = combine(
        _rawAudios,
        _uiState,
        _privateUris
    ) { audios, ui, privates ->
        val nonPrivate = audios.filter { it.uri !in privates }
        val query = ui.searchQuery.trim().lowercase()
        val filtered = if (query.isEmpty()) nonPrivate else {
            nonPrivate.filter {
                it.title.lowercase().contains(query) ||
                        it.artist.lowercase().contains(query) ||
                        it.album.lowercase().contains(query)
            }
        }
        applySort(filtered, ui.sortOrder)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Private Vault Media Collections
    val privateVideos: StateFlow<List<MediaItemData>> = combine(
        _rawVideos,
        _privateUris
    ) { videos, privates ->
        videos.filter { it.uri in privates }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privateAudios: StateFlow<List<MediaItemData>> = combine(
        _rawAudios,
        _privateUris
    ) { audios, privates ->
        audios.filter { it.uri in privates }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Video folders
    val videoFolders: StateFlow<List<FolderItem>> = _rawVideos.combine(_privateUris) { videos, privates ->
        val nonPrivate = videos.filter { it.uri !in privates }
        repository.extractFolders(nonPrivate, true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audio folders
    val audioFolders: StateFlow<List<FolderItem>> = _rawAudios.combine(_privateUris) { audios, privates ->
        val nonPrivate = audios.filter { it.uri !in privates }
        repository.extractFolders(nonPrivate, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        NotificationActionReceiver.playerManager = playerManager

        // Load saved settings & Privacy Vault data
        val savedFavorites = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        val savedPrivates = prefs.getStringSet("privacy_vault_uris", emptySet()) ?: emptySet()
        val hasHash = !prefs.getString("privacy_vault_hash", null).isNullOrEmpty()
        val hasLegacyPin = !prefs.getString("privacy_vault_pin", null).isNullOrEmpty()
        val dolbyEnabled = prefs.getBoolean("dolby_atmos_enabled", true)
        val dolbyProfile = prefs.getString("dolby_profile", "Dynamic") ?: "Dynamic"
        val dolbyBass = prefs.getBoolean("dolby_bass_boost", true)
        val dolbyVirtualizer = prefs.getBoolean("dolby_surround", true)

        _privateUris.value = savedPrivates
        _isPrivacyPinSet.value = hasHash || hasLegacyPin

        _uiState.value = _uiState.value.copy(
            favoriteUris = savedFavorites,
            dolbyAtmosEnabled = dolbyEnabled,
            dolbyProfile = dolbyProfile,
            dolbyBassBoost = dolbyBass,
            dolbySurroundVirtualizer = dolbyVirtualizer
        )
        playerManager.setDolbyAtmos(dolbyEnabled, dolbyProfile, dolbyBass, dolbyVirtualizer)

        scanStorage()
        // Auto update system notification widget on playback state changes
        viewModelScope.launch {
            playerManager.state.collect { snapshot ->
                notificationHelper.updateNotification(snapshot)
            }
        }
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

    fun selectMusicSubTab(tab: MusicSubTab) {
        _uiState.value = _uiState.value.copy(musicSubTab = tab, selectedFolder = null)
    }

    fun toggleFavorite(uri: String) {
        val current = _uiState.value.favoriteUris.toMutableSet()
        if (current.contains(uri)) {
            current.remove(uri)
        } else {
            current.add(uri)
        }
        _uiState.value = _uiState.value.copy(favoriteUris = current)
        prefs.edit().putStringSet("favorites", current).apply()
    }

    fun isFavorite(uri: String): Boolean {
        return _uiState.value.favoriteUris.contains(uri)
    }

    fun setDolbyAtmosEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dolbyAtmosEnabled = enabled)
        prefs.edit().putBoolean("dolby_atmos_enabled", enabled).apply()
        playerManager.setDolbyAtmos(
            enabled,
            _uiState.value.dolbyProfile,
            _uiState.value.dolbyBassBoost,
            _uiState.value.dolbySurroundVirtualizer
        )
    }

    fun setDolbyProfile(profile: String) {
        _uiState.value = _uiState.value.copy(dolbyProfile = profile)
        prefs.edit().putString("dolby_profile", profile).apply()
        playerManager.setDolbyAtmos(
            _uiState.value.dolbyAtmosEnabled,
            profile,
            _uiState.value.dolbyBassBoost,
            _uiState.value.dolbySurroundVirtualizer
        )
    }

    fun setDolbyBassBoost(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dolbyBassBoost = enabled)
        prefs.edit().putBoolean("dolby_bass_boost", enabled).apply()
        playerManager.setDolbyAtmos(
            _uiState.value.dolbyAtmosEnabled,
            _uiState.value.dolbyProfile,
            enabled,
            _uiState.value.dolbySurroundVirtualizer
        )
    }

    fun setDolbySurroundVirtualizer(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dolbySurroundVirtualizer = enabled)
        prefs.edit().putBoolean("dolby_surround", enabled).apply()
        playerManager.setDolbyAtmos(
            _uiState.value.dolbyAtmosEnabled,
            _uiState.value.dolbyProfile,
            _uiState.value.dolbyBassBoost,
            enabled
        )
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

    fun openMp3Converter(video: MediaItemData? = null) {
        _uiState.value = _uiState.value.copy(
            showMp3ConverterDialog = true,
            videoForMp3Convert = video ?: _rawVideos.value.firstOrNull()
        )
    }

    fun closeMp3Converter() {
        _uiState.value = _uiState.value.copy(
            showMp3ConverterDialog = false,
            videoForMp3Convert = null
        )
    }

    fun convertVideoToMp3(video: MediaItemData, quality: String = "320 kbps"): MediaItemData {
        val cleanTitle = video.title.replace(Regex("\\.(mp4|mkv|webm|avi|flv)$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .take(100)
        val convertedItem = MediaItemData(
            id = "converted_${System.currentTimeMillis()}",
            uri = video.uri,
            title = "$cleanTitle [HQ MP3]",
            artist = if (video.artist.isNotBlank()) video.artist else "Aroy Studio",
            album = "Converted MP3s",
            durationMs = video.durationMs,
            sizeBytes = (video.durationMs / 1000L * 40000L).coerceAtLeast(3500000L),
            dateAdded = System.currentTimeMillis() / 1000L,
            folderName = "Converted Audio",
            mimeType = "audio/mp3",
            isVideo = false,
            thumbnailUri = video.thumbnailUri ?: "android.resource://${getApplication<Application>().packageName}/drawable/img_music_vinyl"
        )

        val currentAudios = _rawAudios.value.toMutableList()
        currentAudios.add(0, convertedItem)
        _rawAudios.value = currentAudios

        return convertedItem
    }

    // --- Privacy Vault Management ---
    fun openPrivacyVault() {
        _uiState.value = _uiState.value.copy(showPrivacyVault = true)
    }

    fun closePrivacyVault() {
        _uiState.value = _uiState.value.copy(showPrivacyVault = false)
        _isPrivacyUnlocked.value = false
    }

    private var failedAttempts = 0
    private var lockoutUntilMs = 0L

    fun getLockoutSecondsRemaining(): Long {
        val now = System.currentTimeMillis()
        return if (now < lockoutUntilMs) {
            (lockoutUntilMs - now + 999L) / 1000L
        } else {
            0L
        }
    }

    fun setPrivacyPin(newPin: String): Boolean {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            val random = SecureRandom()
            val saltBytes = ByteArray(16)
            random.nextBytes(saltBytes)
            val salt = saltBytes.joinToString("") { "%02x".format(it) }
            val hash = hashPin(newPin, salt)

            prefs.edit()
                .putString("privacy_vault_salt", salt)
                .putString("privacy_vault_hash", hash)
                .remove("privacy_vault_pin") // clean up legacy plaintext pin
                .apply()

            _isPrivacyPinSet.value = true
            _isPrivacyUnlocked.value = true
            failedAttempts = 0
            lockoutUntilMs = 0L
            return true
        }
        return false
    }

    fun unlockPrivacyVault(enteredPin: String): VaultUnlockResult {
        val remaining = getLockoutSecondsRemaining()
        if (remaining > 0) {
            return VaultUnlockResult.LockedOut(remaining)
        }

        val salt = prefs.getString("privacy_vault_salt", null)
        val hash = prefs.getString("privacy_vault_hash", null)
        val legacyPin = prefs.getString("privacy_vault_pin", null)

        val isCorrect = when {
            hash != null && salt != null -> {
                hashPin(enteredPin, salt) == hash
            }
            legacyPin != null -> {
                if (legacyPin == enteredPin) {
                    setPrivacyPin(enteredPin) // Auto-migrate to salted SHA-256
                    true
                } else false
            }
            else -> false
        }

        return if (isCorrect) {
            failedAttempts = 0
            lockoutUntilMs = 0L
            _isPrivacyUnlocked.value = true
            VaultUnlockResult.Success
        } else {
            failedAttempts++
            if (failedAttempts >= 5) {
                lockoutUntilMs = System.currentTimeMillis() + 30000L // 30-sec lockout
                failedAttempts = 0
                VaultUnlockResult.LockedOut(30L)
            } else {
                VaultUnlockResult.WrongPin(attemptsLeft = 5 - failedAttempts)
            }
        }
    }

    companion object {
        fun hashPin(pin: String, salt: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val combined = "$salt:$pin"
            val bytes = md.digest(combined.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    fun lockPrivacyVault() {
        _isPrivacyUnlocked.value = false
    }

    fun resetPrivacyPin(): Boolean {
        prefs.edit()
            .remove("privacy_vault_salt")
            .remove("privacy_vault_hash")
            .remove("privacy_vault_pin")
            .apply()
        _isPrivacyPinSet.value = false
        _isPrivacyUnlocked.value = false
        failedAttempts = 0
        lockoutUntilMs = 0L
        return true
    }

    fun lockMediaToVault(item: MediaItemData) {
        val current = _privateUris.value.toMutableSet()
        current.add(item.uri)
        _privateUris.value = current
        prefs.edit().putStringSet("privacy_vault_uris", current).apply()
    }

    fun unlockMediaFromVault(item: MediaItemData) {
        val current = _privateUris.value.toMutableSet()
        current.remove(item.uri)
        _privateUris.value = current
        prefs.edit().putStringSet("privacy_vault_uris", current).apply()
    }

    fun isMediaPrivate(uri: String): Boolean {
        return uri in _privateUris.value
    }

    fun getAllPublicVideos(): List<MediaItemData> {
        val privates = _privateUris.value
        return _rawVideos.value.filter { it.uri !in privates }
    }

    fun getAllPublicAudios(): List<MediaItemData> {
        val privates = _privateUris.value
        return _rawAudios.value.filter { it.uri !in privates }
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
        notificationHelper.release()
        NotificationActionReceiver.playerManager = null
        playerManager.release()
    }
}
