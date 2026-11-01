package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItemData
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.LowLightGlossBadgeBorder
import com.example.ui.theme.LowLightGlossBadgeContainer
import com.example.ui.theme.LowLightGlossBadgeText
import com.example.ui.theme.LowLightGlossCyan
import com.example.ui.theme.LowLightGlossSilver

@Composable
fun VideoListTab(
    videos: List<MediaItemData>,
    layoutColumns: Int = 2, // 1 (list), 2 (standard grid), 3 (compact 3-line grid)
    isScanning: Boolean,
    onVideoClick: (MediaItemData) -> Unit,
    onAddToPlaylist: (MediaItemData) -> Unit,
    onConvertMp3: (MediaItemData) -> Unit = {},
    onRefreshScan: () -> Unit
) {
    if (videos.isEmpty() && !isScanning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Videos Found",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap scan to search storage for video files.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRefreshScan) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Device")
                }
            }
        }
    } else {
        // Group videos by category/period like Image 2 ("Recent", "Mar", etc.)
        val groups = remember(videos) {
            val half = (videos.size / 2).coerceAtLeast(1)
            listOf(
                "Recent Videos" to videos.take(half),
                "Saved & Downloads" to videos.drop(half)
            ).filter { it.second.isNotEmpty() }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(layoutColumns),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("video_grid")
        ) {
            groups.forEach { (sectionTitle, sectionVideos) ->
                item(span = { GridItemSpan(layoutColumns) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = LowLightGlossBadgeContainer,
                            border = BorderStroke(0.8.dp, LowLightGlossBadgeBorder)
                        ) {
                            Text(
                                text = "${sectionVideos.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LowLightGlossBadgeText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                items(sectionVideos, key = { it.uri }) { video ->
                    RectangularCircularVideoCard(
                        video = video,
                        layoutColumns = layoutColumns,
                        onClick = { onVideoClick(video) },
                        onAddToPlaylist = { onAddToPlaylist(video) },
                        onConvertMp3 = { onConvertMp3(video) }
                    )
                }
            }
        }
    }
}

/**
 * Rectangular video card with circular rounded corners ("rectangular circular ho")
 * Supports 1-column list, 2-column grid, and 3-column / 3-line grid.
 */
@Composable
fun RectangularCircularVideoCard(
    video: MediaItemData,
    layoutColumns: Int,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onConvertMp3: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    // Circular rounded corner radius (18.dp for rich modern curved look)
    val cardShape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("video_card_${video.id}"),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    Color(0x8838BDF8),
                    Color(0x88A855F7),
                    Color(0x88EC4899),
                    Color(0x8838BDF8)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Dynamic cinematic gradient backdrop so cards are NEVER plain black ("black na rahe")
            val titleHash = video.title.hashCode().let { if (it < 0) -it else it }
            val cinematicGradient = remember(video.title) {
                when (titleHash % 4) {
                    0 -> listOf(Color(0xFF1E3A8A), Color(0xFF0F172A), Color(0xFF1E1B4B))
                    1 -> listOf(Color(0xFF581C87), Color(0xFF1E1B4B), Color(0xFF0F172A))
                    2 -> listOf(Color(0xFF065F46), Color(0xFF0F172A), Color(0xFF042F2E))
                    else -> listOf(Color(0xFF831843), Color(0xFF1E1B4B), Color(0xFF0F172A))
                }
            }

            // Thumbnail container with circular-rectangular clipping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (layoutColumns == 3) 1f else 1.35f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                    .background(Brush.linearGradient(cinematicGradient))
            ) {
                // Front look poster resolution ("Ye video show ho frant look")
                val thumbUri = video.thumbnailUri ?: ""
                when {
                    thumbUri.contains("img_bunny_poster") || video.title.contains("Bunny", ignoreCase = true) -> {
                        Image(
                            painter = painterResource(R.drawable.img_bunny_poster),
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    thumbUri.contains("img_scifi_poster") || video.title.contains("Elephant", ignoreCase = true) -> {
                        Image(
                            painter = painterResource(R.drawable.img_scifi_poster),
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    thumbUri.contains("img_flame_poster") || video.title.contains("Blazes", ignoreCase = true) -> {
                        Image(
                            painter = painterResource(R.drawable.img_flame_poster),
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    thumbUri.contains("img_vfx_poster") || video.title.contains("Steel", ignoreCase = true) -> {
                        Image(
                            painter = painterResource(R.drawable.img_vfx_poster),
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        AsyncImage(
                            model = video.thumbnailUri ?: video.uri,
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Central glowing glass play badge
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(if (layoutColumns == 3) 30.dp else 38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(if (layoutColumns == 3) 18.dp else 22.dp)
                    )
                }

                // Subtle Bottom Vignette only (keeps the top and center crystal clear)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                startY = 120f
                            )
                        )
                )

                // Duration circular pill badge (top-end matching screenshot)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = video.formattedDuration,
                            color = Color.White,
                            fontSize = if (layoutColumns == 3) 9.sp else 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Low Light Gloss Resolution Badge (top-start, replaces harsh green with frosted ice-cyan)
                if (video.resolution.isNotBlank() && layoutColumns < 3) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LowLightGlossBadgeContainer,
                        border = BorderStroke(0.8.dp, LowLightGlossBadgeBorder),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = if (video.resolution.contains("1920")) "1080p" else "HD",
                            color = LowLightGlossBadgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Info row with title and 3-dot menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (layoutColumns == 3) 6.dp else 10.dp,
                        vertical = if (layoutColumns == 3) 6.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = if (layoutColumns == 3) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (layoutColumns < 3) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${video.folderName} ${if (video.formattedSize.isNotBlank()) "• ${video.formattedSize}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(if (layoutColumns == 3) 24.dp else 28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (layoutColumns == 3) 18.dp else 22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play Video") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Convert to MP3") },
                            leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null, tint = LowLightGlossCyan) },
                            onClick = {
                                showMenu = false
                                onConvertMp3()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Playlist") },
                            leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onAddToPlaylist()
                            }
                        )
                    }
                }
            }
        }
    }
}
