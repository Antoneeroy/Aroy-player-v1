package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.MediaItemData
import com.example.ui.theme.LiquidGlassBorderDark
import com.example.ui.theme.LiquidGlassBorderLight
import com.example.ui.theme.LiquidGlassSurfaceDark
import com.example.ui.theme.LiquidGlassSurfaceLight
import com.example.ui.theme.LowLightGlossBadgeBorder
import com.example.ui.theme.LowLightGlossBadgeContainer
import com.example.ui.theme.LowLightGlossBadgeText
import com.example.ui.theme.LowLightGlossCyan
import com.example.ui.theme.LowLightGlossSilver
import com.example.ui.theme.WordColorBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Mp3ConverterDialog(
    allVideos: List<MediaItemData>,
    initialVideo: MediaItemData?,
    isDark: Boolean,
    onConvert: (MediaItemData, String) -> MediaItemData,
    onPlayConverted: (MediaItemData) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedVideo by remember { mutableStateOf(initialVideo ?: allVideos.firstOrNull()) }
    var selectedBitrate by remember { mutableStateOf("320 kbps (Studio HQ)") }
    var showVideoDropdown by remember { mutableStateOf(false) }
    var isConverting by remember { mutableStateOf(false) }
    var conversionProgress by remember { mutableFloatStateOf(0f) }
    var conversionStatus by remember { mutableStateOf("Ready to convert") }
    var convertedMedia by remember { mutableStateOf<MediaItemData?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val animatedProgress by animateFloatAsState(targetValue = conversionProgress, label = "progress")

    val primaryTextColor = if (isDark) Color.White else WordColorBlack
    val secondaryTextColor = if (isDark) LowLightGlossSilver else Color(0xFF334155)

    Dialog(
        onDismissRequest = { if (!isConverting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("mp3_converter_dialog"),
            color = if (isDark) LiquidGlassSurfaceDark else LiquidGlassSurfaceLight,
            border = BorderStroke(
                1.2.dp,
                Brush.linearGradient(
                    listOf(
                        LowLightGlossCyan.copy(alpha = 0.6f),
                        Color(0x55818CF8),
                        LowLightGlossSilver.copy(alpha = 0.4f)
                    )
                )
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(LowLightGlossCyan.copy(alpha = 0.35f), Color(0x33818CF8))
                                    )
                                )
                                .border(1.dp, LowLightGlossCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = LowLightGlossCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "MP3 Converter Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Text(
                                text = "Extract Lossless Audio from Video",
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryTextColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isConverting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video Source Selection Card
                if (allVideos.isEmpty()) {
                    Text(
                        text = "No videos available to convert.",
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "SOURCE VIDEO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LowLightGlossCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!isConverting && allVideos.size > 1) showVideoDropdown = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0x33000000) else Color(0x22FFFFFF)
                        ),
                        border = BorderStroke(1.dp, if (isDark) LiquidGlassBorderDark else LiquidGlassBorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .size(64.dp, 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                            ) {
                                AsyncImage(
                                    model = selectedVideo?.thumbnailUri ?: selectedVideo?.uri,
                                    contentDescription = selectedVideo?.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedVideo?.title ?: "Select video",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedVideo?.formattedDuration ?: "0:00"} • ${selectedVideo?.resolution ?: "HD"} • ${selectedVideo?.formattedSize ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryTextColor
                                )
                            }

                            if (allVideos.size > 1 && !isConverting) {
                                Surface(
                                    shape = CircleShape,
                                    color = LowLightGlossBadgeContainer,
                                    border = BorderStroke(0.8.dp, LowLightGlossBadgeBorder)
                                ) {
                                    Text(
                                        text = "Change",
                                        color = LowLightGlossBadgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showVideoDropdown,
                            onDismissRequest = { showVideoDropdown = false }
                        ) {
                            allVideos.forEach { vid ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(vid.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            Text("${vid.formattedDuration} • ${vid.formattedSize}", fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedVideo = vid
                                        showVideoDropdown = false
                                        convertedMedia = null
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Quality / Bitrate Selector
                    Text(
                        text = "AUDIO QUALITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LowLightGlossCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("320 kbps (HQ)", "256 kbps (VBR)", "128 kbps (Fast)").forEach { bitrate ->
                            val isSelected = selectedBitrate.startsWith(bitrate.take(3))
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { if (!isConverting) selectedBitrate = bitrate },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) LowLightGlossCyan.copy(alpha = 0.22f) else if (isDark) Color(0x22FFFFFF) else Color(0x11000000),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) LowLightGlossCyan else if (isDark) Color(0x33FFFFFF) else Color(0x22000000)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bitrate,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) LowLightGlossCyan else secondaryTextColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Conversion Progress / Status Section
                    if (isConverting || convertedMedia != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0x33000000) else Color(0x18000000))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (convertedMedia != null) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = LowLightGlossCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = LowLightGlossCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = conversionStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryTextColor
                                    )
                                }
                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LowLightGlossCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LowLightGlossCyan,
                                trackColor = if (isDark) Color(0x33FFFFFF) else Color(0x22000000)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action Buttons
                    if (convertedMedia == null) {
                        Button(
                            onClick = {
                                val videoToConvert = selectedVideo ?: return@Button
                                isConverting = true
                                conversionProgress = 0.1f
                                conversionStatus = "Demuxing Video Stream..."

                                coroutineScope.launch {
                                    delay(400)
                                    conversionProgress = 0.45f
                                    conversionStatus = "Encoding High Quality MP3..."
                                    delay(500)
                                    conversionProgress = 0.85f
                                    conversionStatus = "Injecting ID3 Tags & Album Artwork..."
                                    delay(400)
                                    conversionProgress = 1.0f
                                    conversionStatus = "Conversion Completed!"

                                    val result = onConvert(videoToConvert, selectedBitrate)
                                    convertedMedia = result
                                    isConverting = false
                                }
                            },
                            enabled = !isConverting && selectedVideo != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_mp3_convert_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LowLightGlossCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConverting) "Converting..." else "Convert to MP3 Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Success Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isDark) Color(0x44FFFFFF) else Color(0x33000000))
                            ) {
                                Text("Done", color = primaryTextColor)
                            }

                            Button(
                                onClick = {
                                    onPlayConverted(convertedMedia!!)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("play_converted_mp3_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LowLightGlossCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play MP3 Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
