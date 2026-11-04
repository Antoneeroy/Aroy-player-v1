package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MediaItemData
import com.example.data.model.PlaybackMode
import com.example.data.model.PlayerRepeatMode
import com.example.player.PlaybackSnapshot
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// Glassmorphism Specular Palette
private val GlassSurfaceBase = Color(0x1AFFFFFF)
private val GlassSurfaceElevated = Color(0x24FFFFFF)
private val GlassSurfacePill = Color(0x1EFFFFFF)
private val GlassBorderSpecular = Brush.linearGradient(
    listOf(
        Color(0x66FFFFFF),
        Color(0x2838BDF8),
        Color(0x28EC4899),
        Color(0x12FFFFFF)
    )
)
private val GlassBorderHighlight = Color(0x40FFFFFF)
private val GlassShineGradient = Brush.verticalGradient(
    listOf(
        Color(0x2EFFFFFF),
        Color(0x08FFFFFF),
        Color(0x00FFFFFF)
    )
)
private val GlassOrbGlowMagenta = Color(0xFFE11D48)
private val GlassOrbGlowCyan = Color(0xFF06B6D4)
private val GlassOrbGlowPurple = Color(0xFF8B5CF6)

@Composable
fun AudioPlayerSheet(
    snapshot: PlaybackSnapshot,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onForward10s: () -> Unit,
    onRewind10s: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onAddToPlaylist: (MediaItemData) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onSelectTrack: (MediaItemData) -> Unit = {}
) {
    val current = snapshot.currentMedia ?: return
    val context = LocalContext.current
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    BackHandler {
        onClose()
    }

    LaunchedEffect(current.uri) {
        isDraggingSlider = false
        sliderPosition = 0f
    }

    val displayPos = if (isDraggingSlider) {
        sliderPosition.toLong()
    } else {
        snapshot.currentPositionMs
    }

    // Up Next preview calculation
    val nextSong = remember(snapshot.queue, snapshot.queueIndex) {
        val q = snapshot.queue
        if (q.size > 1) {
            val safeCurr = snapshot.queueIndex.coerceIn(0, q.size - 1)
            val nextIdx = (safeCurr + 1) % q.size
            if (nextIdx != safeCurr) q.getOrNull(nextIdx) else null
        } else null
    }

    // Glassmorphic Root with Ambient Light Blobs (Chromatic refraction)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B18))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("audio_player_sheet")
    ) {
        // --- Ambient Chromatic Background Orbs (Creates depth behind the glass) ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-left Violet / Magenta Nebula Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlassOrbGlowMagenta.copy(alpha = 0.35f), GlassOrbGlowPurple.copy(alpha = 0.18f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.22f),
                    radius = w * 0.65f
                )
            )

            // Center-right Cyan / Teal Ambient Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlassOrbGlowCyan.copy(alpha = 0.30f), Color(0x180284C7), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(w * 0.88f, h * 0.45f),
                    radius = w * 0.60f
                )
            )

            // Bottom Indigo / Violet Depth Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x334F46E5), Color(0x18312E81), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.88f),
                    radius = w * 0.70f
                )
            )
        }

        // --- Foreground Scrollable Glassmorphic UI ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glassmorphic Header Bar: [ ⌵ ] "Now Playing" [ ⋮ ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glass Circle Back Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceBase)
                        .border(1.dp, GlassBorderHighlight, CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close Audio Player",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Frosted Glass "Now Playing" Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GlassSurfaceBase,
                    border = BorderStroke(1.dp, Color(0x28FFFFFF))
                ) {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                // Glass Circle More Options Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceBase)
                        .border(1.dp, GlassBorderHighlight, CircleShape)
                        .clickable { showOptionsMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Player options",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sleep Timer (${if (snapshot.sleepTimerMinutesLeft > 0) "${snapshot.sleepTimerMinutesLeft}m" else "Off"})") },
                            leadingIcon = { Icon(Icons.Default.Bedtime, contentDescription = null) },
                            onClick = {
                                showOptionsMenu = false
                                onOpenSleepTimer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Playback Speed (${snapshot.playbackSpeed}x)") },
                            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                            onClick = {
                                showOptionsMenu = false
                                showSpeedMenu = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (snapshot.isMuted) "Unmute" else "Mute") },
                            leadingIcon = { Icon(if (snapshot.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = null) },
                            onClick = {
                                showOptionsMenu = false
                                onToggleMute()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Playlist") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) },
                            onClick = {
                                showOptionsMenu = false
                                onAddToPlaylist(current)
                            }
                        )
                    }

                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false }
                    ) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = {
                                    onSetSpeed(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glassmorphic Album Artwork with Ambient Bevel and Floating Glass Heart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(295.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background flanking equalizer waves
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlankingGlassWaves(isPlaying = snapshot.isPlaying, isLeft = true)
                    FlankingGlassWaves(isPlaying = snapshot.isPlaying, isLeft = false)
                }

                // Frosted Glass Beveled Album Artwork Container
                Box(
                    modifier = Modifier
                        .size(275.dp)
                        .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF8B5CF6), spotColor = Color(0xFF06B6D4))
                        .clip(RoundedCornerShape(28.dp))
                        .background(GlassSurfaceElevated)
                        .border(1.5.dp, GlassBorderSpecular, RoundedCornerShape(28.dp))
                ) {
                    // Artwork Content
                    if (current.thumbnailUri != null) {
                        if (current.thumbnailUri.contains("img_music_vinyl")) {
                            Image(
                                painter = painterResource(id = R.drawable.img_music_vinyl),
                                contentDescription = current.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = current.thumbnailUri,
                                contentDescription = current.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // Glossy Fallback Vinyl Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF2E1065),
                                            Color(0xFF1E1B4B),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(88.dp)
                            )
                        }
                    }

                    // Top Specular Glass Reflection Sheen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(GlassShineGradient)
                    )

                    // Floating Glassmorphic Favorite Button in Top Right Corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x4D000000))
                            .border(1.2.dp, Color(0x55FFFFFF), CircleShape)
                            .clickable { onToggleFavorite() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                            tint = if (isFavorite) Color(0xFFF43F5E) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Track Title, Artist, and Glass Crystal "HD" Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (current.artist.isNotBlank()) current.artist else "Unknown Artist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Glass Crystal "HD" Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x2806B6D4),
                    border = BorderStroke(1.dp, Color(0x6638BDF8))
                ) {
                    Text(
                        text = "HD",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glassmorphic Scrubber Slider
            val maxDur = snapshot.durationMs.toFloat().coerceAtLeast(100f)
            Slider(
                value = (if (isDraggingSlider) sliderPosition else snapshot.currentPositionMs.toFloat()).coerceIn(0f, maxDur),
                onValueChange = {
                    isDraggingSlider = true
                    sliderPosition = it.coerceIn(0f, maxDur)
                },
                onValueChangeFinished = {
                    val target = sliderPosition.coerceIn(0f, maxDur).toLong()
                    isDraggingSlider = false
                    onSeek(target)
                },
                valueRange = 0f..maxDur,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_scrubber_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF43F5E),
                    activeTrackColor = Color(0xFFF43F5E),
                    inactiveTrackColor = Color(0x26FFFFFF)
                )
            )

            // Current Time & Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMillis(displayPos),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatMillis(snapshot.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Glassmorphic Playback Controls: [ Mode ] [ Prev ] [ GLOWING GLASS ORB PLAY/PAUSE ] [ Next ] [ Repeat ]
            // Note: NO text underneath as requested ("mode ka name niche likh ke na aye")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glassmorphic Shuffle / Mode Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceBase)
                        .border(1.dp, GlassBorderHighlight, CircleShape)
                        .clickable { onToggleRepeat() },
                    contentAlignment = Alignment.Center
                ) {
                    when (snapshot.playbackMode) {
                        PlaybackMode.ORDER -> {
                            OrderPlayIcon(
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF38BDF8)
                            )
                        }
                        PlaybackMode.SHUFFLE -> {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        PlaybackMode.REPEAT_ALL -> {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repeat All",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        PlaybackMode.REPEAT_ONE -> {
                            Icon(
                                imageVector = Icons.Default.RepeatOne,
                                contentDescription = "Repeat Current",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        PlaybackMode.STOP_ON_FINISH -> {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop on Song End",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Previous Track Glass Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceBase)
                        .border(1.dp, GlassBorderHighlight, CircleShape)
                        .clickable { onPrevious() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Centerpiece: Glowing Chromatic Glass Orb Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(24.dp, CircleShape, ambientColor = Color(0xFFEC4899), spotColor = Color(0xFF8B5CF6))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF8B5CF6),
                                    Color(0xFFE11D48),
                                    Color(0xFF8B5CF6)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xDDFFFFFF),
                                    Color(0x6638BDF8),
                                    Color(0x66EC4899),
                                    Color(0x44FFFFFF)
                                )
                            ),
                            CircleShape
                        )
                        .clickable { onPlayPause() }
                        .testTag("audio_sheet_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    // Specular light arc on top half
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            brush = Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color.Transparent)),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false
                        )
                    }

                    Icon(
                        imageVector = if (snapshot.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (snapshot.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Next Track Glass Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceBase)
                        .border(1.dp, GlassBorderHighlight, CircleShape)
                        .clickable { onNext() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat / Order Toggle Glass Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceBase)
                        .border(1.dp, GlassBorderHighlight, CircleShape)
                        .clickable { onToggleRepeat() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (snapshot.repeatMode) {
                            PlayerRepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (snapshot.repeatMode != PlayerRepeatMode.OFF) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Glassmorphic Quick Action Bar: [ Share ] [ Favorite ] [ Download ] [ Playlist ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glass Quick Action: Share
                GlassActionButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, current.title)
                            putExtra(Intent.EXTRA_TEXT, "Listening to ${current.title} by ${current.artist} on Aroy Player!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                    }
                )

                // Glass Quick Action: Favorite
                GlassActionButton(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFF43F5E) else Color.White.copy(alpha = 0.85f),
                    onClick = onToggleFavorite
                )

                // Glass Quick Action: Download / Convert
                GlassActionButton(
                    icon = Icons.Default.FileDownload,
                    contentDescription = "Save / Convert",
                    onClick = { onAddToPlaylist(current) }
                )

                // Glass Quick Action: Playlist Queue
                GlassActionButton(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = "Queue / Playlist",
                    onClick = { onAddToPlaylist(current) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Glassmorphic "Up Next" Floating Plate
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        if (nextSong != null) {
                            onSelectTrack(nextSong)
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                border = BorderStroke(1.2.dp, GlassBorderSpecular)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Top Sheen Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0x10FFFFFF),
                                        Color(0x70FFFFFF),
                                        Color(0x10FFFFFF)
                                    )
                                )
                            )
                    )

                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Up Next",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "See all (${snapshot.queue.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (nextSong != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Glass Mini Thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (nextSong.thumbnailUri != null) {
                                        if (nextSong.thumbnailUri.contains("img_music_vinyl")) {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_music_vinyl),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            AsyncImage(
                                                model = nextSong.thumbnailUri,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nextSong.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = nextSong.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Neon soundwave mini visualizer bars
                                NeonSoundwaveMini(isPlaying = snapshot.isPlaying)
                            }
                        } else {
                            Text(
                                text = "End of playlist queue",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GlassActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color = Color.White.copy(alpha = 0.85f),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GlassSurfacePill)
            .border(1.dp, GlassBorderHighlight, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun FlankingGlassWaves(
    isPlaying: Boolean,
    isLeft: Boolean
) {
    val barCount = 4
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val indices = if (isLeft) (0 until barCount) else (barCount - 1 downTo 0)
        for (i in indices) {
            val animVal = remember { Animatable(0.2f + (i * 0.15f)) }
            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    animVal.animateTo(
                        targetValue = 0.35f + Random.nextFloat() * 0.65f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 320 + i * 70, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                } else {
                    animVal.snapTo(0.18f)
                }
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((64 * animVal.value).dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0x358B5CF6))
            )
        }
    }
}

@Composable
private fun NeonSoundwaveMini(isPlaying: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(18.dp)
    ) {
        listOf(0.4f, 0.85f, 0.55f, 0.95f).forEachIndexed { idx, initH ->
            val anim = remember { Animatable(initH) }
            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    anim.animateTo(
                        targetValue = 0.25f + Random.nextFloat() * 0.75f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 240 + idx * 55, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                } else {
                    anim.snapTo(0.2f)
                }
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((18 * anim.value).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFF43F5E),
                                Color(0xFF38BDF8)
                            )
                        )
                    )
            )
        }
    }
}

private fun formatMillis(ms: Long): String {
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

@Composable
fun OrderPlayIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = (w * 0.11f).coerceIn(2.5f, 5.5f)

        // 3 horizontal bars on the left (≡)
        val barEnd = w * 0.48f
        val y1 = h * 0.22f
        val y2 = h * 0.50f
        val y3 = h * 0.78f

        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.08f, y1),
            end = androidx.compose.ui.geometry.Offset(barEnd, y1),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.08f, y2),
            end = androidx.compose.ui.geometry.Offset(barEnd, y2),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.08f, y3),
            end = androidx.compose.ui.geometry.Offset(barEnd, y3),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Downward vertical arrow on the right (↓)
        val arrowX = w * 0.76f
        val arrowTop = h * 0.18f
        val arrowBottom = h * 0.82f

        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(arrowX, arrowTop),
            end = androidx.compose.ui.geometry.Offset(arrowX, arrowBottom),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        val headW = w * 0.16f
        val headH = h * 0.18f
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(arrowX - headW, arrowBottom - headH),
            end = androidx.compose.ui.geometry.Offset(arrowX, arrowBottom),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(arrowX + headW, arrowBottom - headH),
            end = androidx.compose.ui.geometry.Offset(arrowX, arrowBottom),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
