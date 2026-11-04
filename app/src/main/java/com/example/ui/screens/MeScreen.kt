package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.LiquidGlassBorderDark
import com.example.ui.theme.LiquidGlassBorderLight
import com.example.ui.theme.LiquidGlassSurfaceDark
import com.example.ui.theme.LiquidGlassSurfaceLight
import com.example.ui.theme.WordColorBlack
import com.example.ui.theme.WordColorDarkSlate
import com.example.ui.theme.WordColorLightSilver

import com.example.ui.theme.LowLightGlossBadgeBorder
import com.example.ui.theme.LowLightGlossBadgeContainer
import com.example.ui.theme.LowLightGlossBadgeText
import com.example.ui.theme.LowLightGlossCyan
import com.example.ui.theme.LowLightGlossSilver

data class MeToolItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
fun MeScreen(
    isDark: Boolean = true,
    onToggleDayNight: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenThemePicker: () -> Unit,
    onOpenMp3Converter: () -> Unit = {},
    onOpenPrivacyVault: () -> Unit = {},
    onOpenSecurityGuard: () -> Unit = {},
    onRescanStorage: () -> Unit
) {
    var showFileTransferDialog by remember { mutableStateOf(false) }
    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    // Liquid Glass Container & Specular Border Colors (1793368649701 iridescent rainbow glow)
    val cardBackground = if (isDark) LiquidGlassSurfaceDark else LiquidGlassSurfaceLight
    val cardBorder = BorderStroke(
        1.2.dp,
        if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color(0x8838BDF8),
                    Color(0x88A855F7),
                    Color(0x88EC4899),
                    Color(0x8838BDF8)
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
    )

    // High-contrast Word Colors ("word color black" in day mode, crisp white in dark mode)
    val titleColor = if (isDark) Color.White else WordColorBlack
    val subtitleColor = if (isDark) WordColorLightSilver else WordColorDarkSlate

    val tools = listOf(
        MeToolItem("downloads", "Downloads", Icons.Default.Download),
        MeToolItem("mp3_converter", "MP3 Converter", Icons.Default.Headphones),
        MeToolItem("privacy", "Privacy", Icons.Default.Lock),
        MeToolItem("security_guard", "Security Guard", Icons.Default.Security, badge = "SAFE"),
        MeToolItem("file_transfer", "File Transfer", Icons.Default.SwapHoriz, badge = "NEW"),
        MeToolItem("media_manage", "Media Manage", Icons.Default.Folder),
        MeToolItem("theme", "Theme", Icons.Default.ColorLens),
        MeToolItem("history", "History", Icons.Default.History)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("me_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Profile Banner Card (Liquid Glass with App Logo and Day/Night toggle)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = cardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Official 3D Golden Aroy Player Logo
                    Image(
                        painter = painterResource(id = R.drawable.ic_aroy_logo),
                        contentDescription = "Aroy Player Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFE2B755),
                                        Color(0xFFB45309)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Aroy User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LowLightGlossBadgeContainer,
                                border = BorderStroke(0.8.dp, LowLightGlossBadgeBorder)
                            ) {
                                Text(
                                    text = "v2.1.0 VIP",
                                    color = LowLightGlossBadgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Aroy Player Pro • Offline & Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }

                    // Quick Day / Night Switch button
                    IconButton(
                        onClick = onToggleDayNight,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x22000000))
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Day / Night Mode",
                            tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF4F46E5)
                        )
                    }
                }
            }
        }

        // Tools Grid (3 columns matching user uploaded image, in liquid glass card)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = cardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Media Tools",
                        style = MaterialTheme.typography.labelLarge,
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )

                    // 3 columns grid for tools
                    val rows = tools.chunked(3)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            rowItems.forEach { tool ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            when (tool.id) {
                                                "downloads" -> onRescanStorage()
                                                "mp3_converter" -> onOpenMp3Converter()
                                                "privacy" -> onOpenPrivacyVault()
                                                "security_guard" -> onOpenSecurityGuard()
                                                "file_transfer" -> showFileTransferDialog = true
                                                "media_manage" -> onRescanStorage()
                                                "theme" -> onOpenThemePicker()
                                                "history" -> onOpenHistory()
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (isDark) Color(0x331E293B) else Color(0x15000000)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isDark) {
                                                        Brush.linearGradient(
                                                            listOf(
                                                                Color(0x8838BDF8),
                                                                Color(0x88A855F7),
                                                                Color(0x88EC4899)
                                                            )
                                                        )
                                                    } else {
                                                        Brush.linearGradient(
                                                            listOf(
                                                                Color(0x6694A3B8),
                                                                Color(0x44CBD5E1)
                                                            )
                                                        )
                                                    },
                                                    RoundedCornerShape(16.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = tool.icon,
                                                contentDescription = tool.title,
                                                tint = AccentGreen,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }

                                        if (tool.badge != null) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFEF4444)
                                            ) {
                                                Text(
                                                    text = tool.badge,
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = tool.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleColor // 100% visible, high contrast!
                                    )
                                }
                            }
                            // Fill empty slots if last row has less than 3
                            if (rowItems.size < 3) {
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // List Section (Settings, Recycle Bin, Help & Feedback matching reference screenshot)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = cardBorder
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    MeListRow(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        textColor = titleColor,
                        iconTint = AccentGreen,
                        onClick = onOpenSettings
                    )
                    HorizontalDivider(
                        color = if (isDark) Color(0x1FFFFFFF) else Color(0x15000000),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MeListRow(
                        title = "Recycle Bin",
                        icon = Icons.Default.DeleteSweep,
                        textColor = titleColor,
                        iconTint = AccentGreen,
                        onClick = { showRecycleBinDialog = true }
                    )
                    HorizontalDivider(
                        color = if (isDark) Color(0x1FFFFFFF) else Color(0x15000000),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    MeListRow(
                        title = "Help & Feedback",
                        icon = Icons.Default.Feedback,
                        textColor = titleColor,
                        iconTint = AccentGreen,
                        onClick = { showFeedbackDialog = true }
                    )
                }
            }
        }

        // Official AROY PLAYER Brand Showcase Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = cardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_aroy_logo),
                        contentDescription = "Aroy Player Official Emblem",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFE2B755),
                                        Color(0xFFB45309)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AROY PLAYER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = titleColor,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LowLightGlossBadgeContainer,
                                border = BorderStroke(0.8.dp, LowLightGlossBadgeBorder)
                            ) {
                                Text(
                                    text = "v2.1.0",
                                    color = LowLightGlossBadgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Liquid Glass Media Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = LowLightGlossCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ultra Smooth 4K HDR Video & Hi-Res MP3 Player",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                }
            }
        }
    }

    // File Transfer Dialog
    if (showFileTransferDialog) {
        AlertDialog(
            onDismissRequest = { showFileTransferDialog = false },
            title = { Text("File Transfer") },
            text = {
                Text("Send and receive videos and MP3 files between devices instantly via local Wi-Fi Hotspot without using internet data.")
            },
            confirmButton = {
                TextButton(onClick = { showFileTransferDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Recycle Bin Dialog
    if (showRecycleBinDialog) {
        AlertDialog(
            onDismissRequest = { showRecycleBinDialog = false },
            title = { Text("Recycle Bin") },
            text = {
                Text("Recycle bin is empty. Recently deleted files are kept safe for 30 days before permanent deletion.")
            },
            confirmButton = {
                TextButton(onClick = { showRecycleBinDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Feedback Dialog
    if (showFeedbackDialog) {
        var feedbackText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Help & Feedback") },
            text = {
                Column {
                    Text("Have suggestions for Aroy Player? Write to us below:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Your Feedback") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MeListRow(
    title: String,
    icon: ImageVector,
    textColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
