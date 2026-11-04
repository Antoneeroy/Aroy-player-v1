package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AroyViewModel
import com.example.ui.BottomNavDestination
import com.example.ui.VideoSubTab
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.AudioPlayerSheet
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.Mp3ConverterDialog
import com.example.security.SecurityGuardDialog
import com.example.ui.components.PrivacyVaultDialog
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SortOrderDialog
import com.example.ui.components.StreamUrlDialog
import com.example.ui.components.glossyRedBlueWhiteBackground
import com.example.ui.screens.tabs.AudioListTab
import com.example.ui.screens.tabs.FolderListTab
import com.example.ui.screens.tabs.OnlineStreamTab
import com.example.ui.screens.tabs.PlaylistsTab
import com.example.ui.screens.tabs.VideoListTab
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGlassBorderDark
import com.example.ui.theme.LiquidGlassBorderLight
import com.example.ui.theme.LiquidGlassSurfaceDark
import com.example.ui.theme.LiquidGlassSurfaceLight
import com.example.ui.theme.WordColorBlack
import com.example.ui.theme.WordColorDarkSlate
import com.example.ui.theme.WordColorLightSilver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AroyViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackSnapshot by viewModel.playbackState.collectAsStateWithLifecycle()
    val filteredVideos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val filteredAudios by viewModel.filteredAudios.collectAsStateWithLifecycle()
    val videoFolders by viewModel.videoFolders.collectAsStateWithLifecycle()
    val audioFolders by viewModel.audioFolders.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val savedStreams by viewModel.savedStreams.collectAsStateWithLifecycle()
    val privateVideos by viewModel.privateVideos.collectAsStateWithLifecycle()
    val privateAudios by viewModel.privateAudios.collectAsStateWithLifecycle()
    val isPrivacyUnlocked by viewModel.isPrivacyUnlocked.collectAsStateWithLifecycle()
    val isPrivacyPinSet by viewModel.isPrivacyPinSet.collectAsStateWithLifecycle()

    val isDark = uiState.themeMode != AppThemeMode.LIGHT
    val primaryTextColor = if (isDark) Color.White else WordColorBlack
    val secondaryTextColor = if (isDark) WordColorLightSilver else WordColorDarkSlate

    var isSearchActive by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var isMiniPlayerDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(playbackSnapshot.currentMedia) {
        if (playbackSnapshot.currentMedia != null) {
            isMiniPlayerDismissed = false
        }
    }

    // Permissions launcher
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        viewModel.setHasPermission(granted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest)
    }

    // Full Screen Video Player
    if (uiState.isVideoPlayerOpen && playbackSnapshot.currentMedia != null) {
        VideoPlayerScreen(
            playerManager = viewModel.playerManager,
            snapshot = playbackSnapshot,
            resizeMode = uiState.resizeMode,
            onResizeModeChange = { viewModel.setResizeMode(it) },
            onClose = { viewModel.closeVideoPlayer() },
            onRestartFromBeginning = { viewModel.resumeFromBeginning() }
        )
        return
    }

    // Folder Detail Screen
    if (uiState.selectedFolder != null) {
        val folder = uiState.selectedFolder!!
        val folderItems = if (folder.isVideoFolder) filteredVideos else filteredAudios
        FolderDetailScreen(
            folder = folder,
            items = folderItems,
            onBack = { viewModel.closeFolder() },
            onItemClick = { item -> viewModel.playMediaItem(item, folderItems) },
            onAddToPlaylist = { item -> viewModel.setShowAddToPlaylist(item) }
        )
        return
    }

    // Settings Screen
    if (uiState.isSettingsOpen) {
        SettingsScreen(
            currentThemeMode = uiState.themeMode,
            onThemeChange = { viewModel.setThemeMode(it) },
            onRescanStorage = { viewModel.scanStorage() },
            dolbyAtmosEnabled = uiState.dolbyAtmosEnabled,
            dolbyProfile = uiState.dolbyProfile,
            dolbyBassBoost = uiState.dolbyBassBoost,
            dolbySurroundVirtualizer = uiState.dolbySurroundVirtualizer,
            onToggleDolbyAtmos = { viewModel.setDolbyAtmosEnabled(it) },
            onSelectDolbyProfile = { viewModel.setDolbyProfile(it) },
            onToggleDolbyBassBoost = { viewModel.setDolbyBassBoost(it) },
            onToggleDolbySurroundVirtualizer = { viewModel.setDolbySurroundVirtualizer(it) },
            isPrivacyPinSet = isPrivacyPinSet,
            onResetPrivacyPin = { viewModel.resetPrivacyPin() },
            onOpenSecurityGuard = { viewModel.openSecurityGuard() },
            onBack = { viewModel.setSettingsOpen(false) }
        )
        return
    }

    // Dialogs
    if (uiState.showSleepTimerDialog) {
        SleepTimerDialog(
            currentMinutesLeft = playbackSnapshot.sleepTimerMinutesLeft,
            onSetTimer = { viewModel.playerManager.setSleepTimer(it) },
            onDismiss = { viewModel.setShowSleepTimerDialog(false) }
        )
    }

    if (uiState.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onConfirm = { name, desc -> viewModel.createPlaylist(name, desc) },
            onDismiss = { viewModel.setShowCreatePlaylist(false) }
        )
    }

    if (uiState.mediaForPlaylist != null) {
        AddToPlaylistDialog(
            mediaItem = uiState.mediaForPlaylist!!,
            playlists = playlists,
            onAddToPlaylist = { playlistId ->
                viewModel.addToPlaylist(playlistId, uiState.mediaForPlaylist!!)
            },
            onCreateNewPlaylist = {
                viewModel.setShowCreatePlaylist(true)
            },
            onDismiss = { viewModel.setShowAddToPlaylist(null) }
        )
    }

    if (uiState.showStreamDialog) {
        StreamUrlDialog(
            onPlayStream = { url, title, type ->
                viewModel.playOnlineStream(url, title, type)
                viewModel.setShowStreamDialog(false)
            },
            onDismiss = { viewModel.setShowStreamDialog(false) }
        )
    }

    if (showSortDialog) {
        SortOrderDialog(
            currentOrder = uiState.sortOrder,
            onSelectOrder = { viewModel.setSortOrder(it) },
            onDismiss = { showSortDialog = false }
        )
    }

    if (uiState.showMp3ConverterDialog) {
        Mp3ConverterDialog(
            allVideos = filteredVideos,
            initialVideo = uiState.videoForMp3Convert,
            isDark = isDark,
            onConvert = { video, quality ->
                viewModel.convertVideoToMp3(video, quality)
            },
            onPlayConverted = { convertedAudio ->
                viewModel.closeMp3Converter()
                viewModel.playMediaItem(convertedAudio, filteredAudios)
            },
            onDismiss = { viewModel.closeMp3Converter() }
        )
    }

    // Privacy Vault Screen / Dialog
    if (uiState.showPrivacyVault) {
        PrivacyVaultDialog(
            isPinSet = isPrivacyPinSet,
            isUnlocked = isPrivacyUnlocked,
            privateVideos = privateVideos,
            privateAudios = privateAudios,
            publicVideos = viewModel.getAllPublicVideos(),
            publicAudios = viewModel.getAllPublicAudios(),
            onSetPin = { viewModel.setPrivacyPin(it) },
            onUnlock = { viewModel.unlockPrivacyVault(it) },
            onResetPin = { viewModel.resetPrivacyPin() },
            onLock = { viewModel.lockPrivacyVault() },
            onLockMedia = { viewModel.lockMediaToVault(it) },
            onUnlockMedia = { viewModel.unlockMediaFromVault(it) },
            onPlayMedia = { item ->
                if (item.isVideo) {
                    viewModel.playMediaItem(item, privateVideos)
                } else {
                    viewModel.playMediaItem(item, privateAudios)
                }
            },
            onDismiss = { viewModel.closePrivacyVault() }
        )
    }

    // Security Guard Shield Dialog
    if (uiState.showSecurityGuard) {
        SecurityGuardDialog(
            isVaultPinSet = isPrivacyPinSet,
            privateMediaCount = privateVideos.size + privateAudios.size,
            onOpenPrivacyVault = { viewModel.openPrivacyVault() },
            onDismiss = { viewModel.closeSecurityGuard() }
        )
    }

    // Root Scaffold with Glossy Red-Blue-White (5% mix) Background
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp) // "Ese thoda niche karo" - extra top margin
            ) {
                // Top Header Row with App Logo, Name, and Day/Night option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Logo + Name ("app ka logo name likho")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.selectBottomNav(BottomNavDestination.VIDEO) }
                    ) {
                        // Official Luxury 3D Gold AROY PLAYER Logo
                        Image(
                            painter = painterResource(id = R.drawable.ic_aroy_logo),
                            contentDescription = "Aroy Player Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .border(
                                    1.2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFE2B755),
                                            Color(0xFFB45309)
                                        )
                                    ),
                                    RoundedCornerShape(13.dp)
                                )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Aroy Player",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryTextColor, // "word color black" in day mode!
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = AccentGreen
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Video & MP3 Player",
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryTextColor
                            )
                        }
                    }

                    // Top Action Icons: Search, Day/Night Toggle ("day/ night Option add karo"), Layout Grid
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Search Button
                        IconButton(
                            onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) viewModel.setSearchQuery("")
                            },
                            modifier = Modifier.testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = primaryTextColor
                            )
                        }

                        // Day / Night Option Button ("day/ night Option add karo")
                        IconButton(
                            onClick = { viewModel.toggleDayNightMode() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isDark) Color(0x33FFFFFF) else Color(0x18000000))
                                .testTag("day_night_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDark) "Switch to Day Mode" else "Switch to Night Mode",
                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF4F46E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (uiState.currentBottomNav == BottomNavDestination.VIDEO) {
                            // Layout toggle: 3-line grid / 2-line grid / 1-line list ("3 line add karo")
                            IconButton(
                                onClick = { viewModel.cycleVideoLayout() },
                                modifier = Modifier.testTag("layout_grid_toggle")
                            ) {
                                BadgedBox(
                                    badge = {
                                        Surface(
                                            shape = CircleShape,
                                            color = AccentGreen,
                                            modifier = Modifier.size(14.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${uiState.videoLayoutColumns}",
                                                    color = Color.Black,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = when (uiState.videoLayoutColumns) {
                                            3 -> Icons.Default.ViewCompact // 3 line grid
                                            1 -> Icons.Default.ViewAgenda   // 1 line list
                                            else -> Icons.Default.GridView // 2 line standard
                                        },
                                        contentDescription = "Switch to 3-line / 2-line / list grid",
                                        tint = primaryTextColor
                                    )
                                }
                            }

                            // Sort icon
                            IconButton(
                                onClick = { showSortDialog = true },
                                modifier = Modifier.testTag("sort_button")
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Sort",
                                    tint = primaryTextColor
                                )
                            }
                        }
                    }
                }

                // If search is active, show search field
                if (isSearchActive) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search videos, music, folders...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = primaryTextColor,
                            unfocusedTextColor = primaryTextColor,
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = if (isDark) Color(0x44FFFFFF) else Color(0x33000000)
                        ),
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = primaryTextColor)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("search_text_field")
                    )
                }

                // Sub-tabs under VIDEO section (Video, Folder, Playlist)
                if (uiState.currentBottomNav == BottomNavDestination.VIDEO && !isSearchActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        listOf(VideoSubTab.VIDEO, VideoSubTab.FOLDER, VideoSubTab.PLAYLIST).forEach { subTab ->
                            val isSelected = uiState.videoSubTab == subTab
                            Column(
                                modifier = Modifier
                                    .clickable { viewModel.selectVideoSubTab(subTab) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = subTab.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AccentGreen else secondaryTextColor
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 3.dp)
                                            .width(24.dp)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(AccentGreen)
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub-tabs under MUSIC section (Music, Playlist, Favourite) ("music pe click kare upar music, playlist, favourite, ye bhi aye")
                if (uiState.currentBottomNav == BottomNavDestination.MUSIC && !isSearchActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        listOf(
                            com.example.ui.MusicSubTab.MUSIC,
                            com.example.ui.MusicSubTab.PLAYLIST,
                            com.example.ui.MusicSubTab.FAVOURITE
                        ).forEach { subTab ->
                            val isSelected = uiState.musicSubTab == subTab
                            Column(
                                modifier = Modifier
                                    .clickable { viewModel.selectMusicSubTab(subTab) }
                                    .padding(vertical = 4.dp)
                                    .testTag("music_subtab_${subTab.name}")
                            ) {
                                Text(
                                    text = subTab.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AccentGreen else secondaryTextColor
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 3.dp)
                                            .width(24.dp)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(AccentGreen)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Floating MiniPlayerBar with swipe-to-dismiss & hold gesture
                if (playbackSnapshot.currentMedia != null && !isMiniPlayerDismissed) {
                    MiniPlayerBar(
                        snapshot = playbackSnapshot,
                        onBarClick = {
                            if (playbackSnapshot.currentMedia?.isVideo == true) {
                                viewModel.setVideoPlayerOpen(true)
                            } else {
                                viewModel.setAudioSheetExpanded(true)
                            }
                        },
                        onPlayPause = { viewModel.playerManager.playPauseToggle() },
                        onNext = { viewModel.playerManager.playNext() },
                        onDismiss = {
                            viewModel.playerManager.pause()
                            isMiniPlayerDismissed = true
                        }
                    )
                }

                // Liquid Glass Bottom Navigation Bar with: VIDEO | MUSIC | STREAM (next to music!) | ME
                Surface(
                    color = if (isDark) LiquidGlassSurfaceDark else LiquidGlassSurfaceLight,
                    border = BorderStroke(
                        1.dp,
                        if (isDark) {
                            Brush.linearGradient(
                                listOf(
                                    Color(0x7738BDF8),
                                    Color(0x77A855F7),
                                    Color(0x77EC4899),
                                    Color(0x7738BDF8)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color(0x6694A3B8),
                                    Color(0x44CBD5E1)
                                )
                            )
                        }
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        modifier = Modifier.height(64.dp)
                    ) {
                        // 1. VIDEO
                        NavigationBarItem(
                            selected = uiState.currentBottomNav == BottomNavDestination.VIDEO,
                            onClick = { viewModel.selectBottomNav(BottomNavDestination.VIDEO) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "VIDEO"
                                )
                            },
                            label = { Text("VIDEO", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentGreen,
                                selectedTextColor = AccentGreen,
                                unselectedIconColor = secondaryTextColor,
                                unselectedTextColor = secondaryTextColor,
                                indicatorColor = AccentGreen.copy(alpha = 0.18f)
                            ),
                            modifier = Modifier.testTag("nav_video")
                        )

                        // 2. MUSIC
                        NavigationBarItem(
                            selected = uiState.currentBottomNav == BottomNavDestination.MUSIC,
                            onClick = { viewModel.selectBottomNav(BottomNavDestination.MUSIC) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "MUSIC"
                                )
                            },
                            label = { Text("MUSIC", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentGreen,
                                selectedTextColor = AccentGreen,
                                unselectedIconColor = secondaryTextColor,
                                unselectedTextColor = secondaryTextColor,
                                indicatorColor = AccentGreen.copy(alpha = 0.18f)
                            ),
                            modifier = Modifier.testTag("nav_music")
                        )

                        // 3. STREAM (Right next to MUSIC as requested!)
                        NavigationBarItem(
                            selected = uiState.currentBottomNav == BottomNavDestination.STREAM,
                            onClick = { viewModel.selectBottomNav(BottomNavDestination.STREAM) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = "STREAM"
                                )
                            },
                            label = { Text("STREAM", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentGreen,
                                selectedTextColor = AccentGreen,
                                unselectedIconColor = secondaryTextColor,
                                unselectedTextColor = secondaryTextColor,
                                indicatorColor = AccentGreen.copy(alpha = 0.18f)
                            ),
                            modifier = Modifier.testTag("nav_stream")
                        )

                        // 4. ME (Profile & Tools from reference screenshot)
                        NavigationBarItem(
                            selected = uiState.currentBottomNav == BottomNavDestination.ME,
                            onClick = { viewModel.selectBottomNav(BottomNavDestination.ME) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "ME"
                                )
                            },
                            label = { Text("ME", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentGreen,
                                selectedTextColor = AccentGreen,
                                unselectedIconColor = secondaryTextColor,
                                unselectedTextColor = secondaryTextColor,
                                indicatorColor = AccentGreen.copy(alpha = 0.18f)
                            ),
                            modifier = Modifier.testTag("nav_me")
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .glossyRedBlueWhiteBackground(isDark = isDark)
            .testTag("home_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Storage Permission Warning Banner if not granted
            if (!uiState.hasPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) LiquidGlassSurfaceDark else LiquidGlassSurfaceLight
                    ),
                    border = BorderStroke(1.dp, if (isDark) LiquidGlassBorderDark else LiquidGlassBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Grant storage permission to scan videos & songs from device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryTextColor,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { permissionLauncher.launch(permissionsToRequest) }
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }

            // Main Content Area based on BottomNav Destination
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState.currentBottomNav) {
                    BottomNavDestination.VIDEO -> {
                        when (uiState.videoSubTab) {
                            VideoSubTab.VIDEO -> {
                                VideoListTab(
                                    videos = filteredVideos,
                                    layoutColumns = uiState.videoLayoutColumns, // 3 lines, 2 lines, or 1 line
                                    isScanning = uiState.isScanning,
                                    onVideoClick = { video -> viewModel.playMediaItem(video, filteredVideos) },
                                    onAddToPlaylist = { video -> viewModel.setShowAddToPlaylist(video) },
                                    onConvertMp3 = { video -> viewModel.openMp3Converter(video) },
                                    onLockToVault = { video -> viewModel.lockMediaToVault(video) },
                                    onRefreshScan = { viewModel.scanStorage() }
                                )
                            }
                            VideoSubTab.FOLDER -> {
                                FolderListTab(
                                    videoFolders = videoFolders,
                                    audioFolders = audioFolders,
                                    onFolderClick = { folder -> viewModel.openFolder(folder) }
                                )
                            }
                            VideoSubTab.PLAYLIST -> {
                                PlaylistsTab(
                                    history = history,
                                    playlists = playlists,
                                    onResumeItem = { item -> viewModel.playMediaItem(item) },
                                    onClearHistory = { viewModel.clearHistory() },
                                    onCreatePlaylist = { viewModel.setShowCreatePlaylist(true) },
                                    onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                                )
                            }
                            VideoSubTab.STREAM -> {
                                OnlineStreamTab(
                                    savedStreams = savedStreams,
                                    onPlayStream = { url, title, type ->
                                        viewModel.playOnlineStream(url, title, type)
                                    },
                                    onDeleteSavedStream = { id -> viewModel.deleteStream(id) },
                                    onOpenStreamDialog = { viewModel.setShowStreamDialog(true) }
                                )
                            }
                        }
                    }

                    BottomNavDestination.MUSIC -> {
                        when (uiState.musicSubTab) {
                            com.example.ui.MusicSubTab.MUSIC -> {
                                AudioListTab(
                                    audios = filteredAudios,
                                    isScanning = uiState.isScanning,
                                    onAudioClick = { audio -> viewModel.playMediaItem(audio, filteredAudios) },
                                    onAddToPlaylist = { audio -> viewModel.setShowAddToPlaylist(audio) },
                                    onRefreshScan = { viewModel.scanStorage() },
                                    isFavorite = { uri -> viewModel.isFavorite(uri) },
                                    onToggleFavorite = { uri -> viewModel.toggleFavorite(uri) },
                                    onLockToVault = { audio -> viewModel.lockMediaToVault(audio) }
                                )
                            }
                            com.example.ui.MusicSubTab.PLAYLIST -> {
                                PlaylistsTab(
                                    history = history.filter { !it.isVideo },
                                    playlists = playlists,
                                    onResumeItem = { item -> viewModel.playMediaItem(item) },
                                    onClearHistory = { viewModel.clearHistory() },
                                    onCreatePlaylist = { viewModel.setShowCreatePlaylist(true) },
                                    onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                                )
                            }
                            com.example.ui.MusicSubTab.FAVOURITE -> {
                                val favAudios = filteredAudios.filter { viewModel.isFavorite(it.uri) }
                                AudioListTab(
                                    audios = favAudios,
                                    isScanning = uiState.isScanning,
                                    onAudioClick = { audio -> viewModel.playMediaItem(audio, favAudios) },
                                    onAddToPlaylist = { audio -> viewModel.setShowAddToPlaylist(audio) },
                                    onRefreshScan = { viewModel.scanStorage() },
                                    isFavorite = { uri -> viewModel.isFavorite(uri) },
                                    onToggleFavorite = { uri -> viewModel.toggleFavorite(uri) },
                                    onLockToVault = { audio -> viewModel.lockMediaToVault(audio) },
                                    emptyTitle = "No Favourites Yet",
                                    emptyMessage = "Tap the heart icon on any song in the Music tab to add it to your favourites."
                                )
                            }
                        }
                    }

                    BottomNavDestination.STREAM -> {
                        // Direct online streaming tab next to MUSIC!
                        OnlineStreamTab(
                            savedStreams = savedStreams,
                            onPlayStream = { url, title, type ->
                                viewModel.playOnlineStream(url, title, type)
                            },
                            onDeleteSavedStream = { id -> viewModel.deleteStream(id) },
                            onOpenStreamDialog = { viewModel.setShowStreamDialog(true) }
                        )
                    }

                    BottomNavDestination.ME -> {
                        MeScreen(
                            isDark = isDark,
                            onToggleDayNight = { viewModel.toggleDayNightMode() },
                            onOpenSettings = { viewModel.setSettingsOpen(true) },
                            onOpenHistory = {
                                viewModel.selectBottomNav(BottomNavDestination.VIDEO)
                                viewModel.selectVideoSubTab(VideoSubTab.PLAYLIST)
                            },
                            onOpenThemePicker = { viewModel.setSettingsOpen(true) },
                            onOpenMp3Converter = { viewModel.openMp3Converter(null) },
                            onOpenPrivacyVault = { viewModel.openPrivacyVault() },
                            onOpenSecurityGuard = { viewModel.openSecurityGuard() },
                            onRescanStorage = { viewModel.scanStorage() }
                        )
                    }
                }
            }
        }
    }

    // Expanded Audio Player Sheet
    if (uiState.isAudioSheetExpanded && playbackSnapshot.currentMedia != null) {
        val currentUri = playbackSnapshot.currentMedia?.uri ?: ""
        AudioPlayerSheet(
            snapshot = playbackSnapshot,
            onClose = { viewModel.setAudioSheetExpanded(false) },
            onPlayPause = { viewModel.playerManager.playPauseToggle() },
            onSeek = { viewModel.playerManager.seekTo(it) },
            onForward10s = { viewModel.playerManager.forward10s() },
            onRewind10s = { viewModel.playerManager.rewind10s() },
            onNext = { viewModel.playerManager.playNext() },
            onPrevious = { viewModel.playerManager.playPrevious() },
            onToggleRepeat = { viewModel.playerManager.toggleRepeatMode() },
            onSetSpeed = { viewModel.playerManager.setPlaybackSpeed(it) },
            onToggleMute = { viewModel.playerManager.toggleMute() },
            onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
            onAddToPlaylist = { viewModel.setShowAddToPlaylist(it) },
            isFavorite = viewModel.isFavorite(currentUri),
            onToggleFavorite = { viewModel.toggleFavorite(currentUri) },
            onSelectTrack = { track -> viewModel.playMediaItem(track, playbackSnapshot.queue) }
        )
    }
}
