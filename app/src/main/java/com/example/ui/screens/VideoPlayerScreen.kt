package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.MediaItemData
import com.example.data.model.PlayerResizeMode
import com.example.player.PlaybackSnapshot
import com.example.player.PlayerManager
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    playerManager: PlayerManager,
    snapshot: PlaybackSnapshot,
    resizeMode: PlayerResizeMode,
    onResizeModeChange: (PlayerResizeMode) -> Unit,
    onClose: () -> Unit,
    onRestartFromBeginning: () -> Unit
) {
    val context = LocalContext.current
    val currentMedia = snapshot.currentMedia ?: return

    // Keep screen on during video playback
    val activity = remember(context) { context.findActivity() }
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler {
        onClose()
    }

    var areControlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var autoHideCounter by remember { mutableIntStateOf(0) }

    // Volume & Brightness Gesture states
    var gestureIndicatorText by remember { mutableStateOf<String?>(null) }
    var gestureIndicatorIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
    var gestureIndicatorProgress by remember { mutableFloatStateOf(0f) }
    var showGestureIndicator by remember { mutableStateOf(false) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    // Subtitle picker launcher
    val subtitlePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            playerManager.attachSubtitle(uri)
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(areControlsVisible, autoHideCounter, snapshot.isPlaying, isLocked) {
        if (areControlsVisible && snapshot.isPlaying && !isLocked) {
            delay(4000)
            areControlsVisible = false
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_screen")
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Media3 ExoPlayer AndroidView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.getPlayer()
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = playerManager.getPlayer()
                playerView.resizeMode = when (resizeMode) {
                    PlayerResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    PlayerResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    PlayerResizeMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    PlayerResizeMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    PlayerResizeMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Overlay Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isLocked) {
                    if (isLocked) {
                        detectTapGestures(
                            onTap = {
                                areControlsVisible = !areControlsVisible
                            }
                        )
                    } else {
                        detectTapGestures(
                            onTap = {
                                areControlsVisible = !areControlsVisible
                                autoHideCounter++
                            },
                            onDoubleTap = { offset ->
                                autoHideCounter++
                                if (offset.x < size.width / 2) {
                                    playerManager.rewind10s()
                                    gestureIndicatorText = "-10s"
                                    gestureIndicatorIcon = Icons.Default.FastRewind
                                    showGestureIndicator = true
                                } else {
                                    playerManager.forward10s()
                                    gestureIndicatorText = "+10s"
                                    gestureIndicatorIcon = Icons.Default.FastForward
                                    showGestureIndicator = true
                                }
                            }
                        )
                    }
                }
                .pointerInput(isLocked) {
                    if (!isLocked) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                showGestureIndicator = false
                            },
                            onVerticalDrag = { change, dragAmount ->
                                val x = change.position.x
                                val isLeftSide = x < size.width / 2
                                val delta = -dragAmount / size.height

                                if (isLeftSide) {
                                    // Brightness control
                                    activity?.let { act ->
                                        val lp = act.window.attributes
                                        var currentB = if (lp.screenBrightness < 0f) 0.5f else lp.screenBrightness
                                        currentB = (currentB + delta * 2).coerceIn(0.01f, 1.0f)
                                        lp.screenBrightness = currentB
                                        act.window.attributes = lp

                                        gestureIndicatorText = "${(currentB * 100).toInt()}%"
                                        gestureIndicatorIcon = Icons.Default.BrightnessMedium
                                        gestureIndicatorProgress = currentB
                                        showGestureIndicator = true
                                    }
                                } else {
                                    // Volume control
                                    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    val step = if (delta > 0) 1 else -1
                                    val targetVol = (currentVol + step).coerceIn(0, maxVolume)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)

                                    val progress = targetVol.toFloat() / maxVolume.toFloat()
                                    gestureIndicatorText = "${(progress * 100).toInt()}%"
                                    gestureIndicatorIcon = Icons.Default.VolumeUp
                                    gestureIndicatorProgress = progress
                                    showGestureIndicator = true
                                }
                            }
                        )
                    }
                }
        )

        // Center Gesture Feedback HUD (Brightness/Volume/Seek)
        AnimatedVisibility(
            visible = showGestureIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    gestureIndicatorIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = gestureIndicatorText ?: "",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Center Buffering Spinner
        if (snapshot.isBuffering && snapshot.errorMessage == null) {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(54.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
        }

        // Center Error Card with Retry & Close
        if (snapshot.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.88f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .testTag("video_error_card")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Playback Error",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = snapshot.errorMessage ?: "Unable to stream media",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = onClose) {
                            Text("Close", color = Color.White)
                        }
                        Button(
                            onClick = { playerManager.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }

        // Floating Lock Toggle if Locked
        if (isLocked) {
            AnimatedVisibility(
                visible = areControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                FilledIconButton(
                    onClick = { isLocked = false },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock Controls",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Full Interactive Controls Overlay (when not locked)
        AnimatedVisibility(
            visible = areControlsVisible && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Gradient & Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = currentMedia.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        // Top Action Icons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Subtitles
                            IconButton(onClick = { subtitlePicker.launch("*/*") }) {
                                Icon(
                                    imageVector = Icons.Default.ClosedCaption,
                                    contentDescription = "Attach Subtitle",
                                    tint = if (snapshot.hasSubtitle) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }

                            // Aspect Ratio Toggle
                            var showAspectMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showAspectMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Aspect Ratio",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showAspectMenu,
                                    onDismissRequest = { showAspectMenu = false }
                                ) {
                                    PlayerResizeMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.title) },
                                            onClick = {
                                                onResizeModeChange(mode)
                                                showAspectMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Speed Selector
                            var showSpeedMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showSpeedMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Playback Speed",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false }
                                ) {
                                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                        DropdownMenuItem(
                                            text = { Text("${spd}x") },
                                            onClick = {
                                                playerManager.setPlaybackSpeed(spd)
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Lock Screen Button
                            IconButton(onClick = { isLocked = true }) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = "Lock Screen",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Center Play/Rewind/Forward Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            playerManager.rewind10s()
                            autoHideCounter++
                        },
                        modifier = Modifier.size(52.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.55f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10s",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            playerManager.playPauseToggle()
                            autoHideCounter++
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("video_player_play_pause"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (snapshot.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (snapshot.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            playerManager.forward10s()
                            autoHideCounter++
                        },
                        modifier = Modifier.size(52.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.55f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 10s",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Bottom Gradient & Scrubber Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        // Slider & Times
                        var sliderPos by remember { mutableFloatStateOf(0f) }
                        var isDragging by remember { mutableStateOf(false) }
                        val maxD = if (snapshot.durationMs > 0) snapshot.durationMs.toFloat() else 100f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatVideoDuration(if (isDragging) sliderPos.toLong() else snapshot.currentPositionMs),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )

                            Slider(
                                value = if (isDragging) sliderPos else snapshot.currentPositionMs.toFloat().coerceIn(0f, maxD),
                                onValueChange = {
                                    isDragging = true
                                    sliderPos = it
                                },
                                onValueChangeFinished = {
                                    isDragging = false
                                    playerManager.seekTo(sliderPos.toLong())
                                },
                                valueRange = 0f..maxD,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                                    .testTag("video_scrubber_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )

                            Text(
                                text = formatVideoDuration(snapshot.durationMs),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Bottom Action Icons (Prev, Next, PiP, Speed)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { playerManager.playPrevious() }) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Previous Video",
                                        tint = Color.White
                                    )
                                }

                                IconButton(onClick = { playerManager.playNext() }) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next Video",
                                        tint = Color.White
                                    )
                                }

                                if (snapshot.currentPositionMs > 15000) {
                                    AssistChip(
                                        onClick = onRestartFromBeginning,
                                        label = { Text("Start Over", color = Color.White) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Replay,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            // Picture in Picture Button
                            IconButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                        try {
                                            val params = PictureInPictureParams.Builder()
                                                .setAspectRatio(Rational(16, 9))
                                                .build()
                                            activity.enterPictureInPictureMode(params)
                                        } catch (e: Exception) {
                                            // PiP not supported or failed
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = "Picture in Picture",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatVideoDuration(ms: Long): String {
    if (ms <= 0) return "00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
