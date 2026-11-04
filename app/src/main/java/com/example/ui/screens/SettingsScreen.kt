package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.glossyRedBlueWhiteBackground
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    onRescanStorage: () -> Unit,
    dolbyAtmosEnabled: Boolean = true,
    dolbyProfile: String = "Dynamic",
    dolbyBassBoost: Boolean = true,
    dolbySurroundVirtualizer: Boolean = true,
    onToggleDolbyAtmos: (Boolean) -> Unit = {},
    onSelectDolbyProfile: (String) -> Unit = {},
    onToggleDolbyBassBoost: (Boolean) -> Unit = {},
    onToggleDolbySurroundVirtualizer: (Boolean) -> Unit = {},
    isPrivacyPinSet: Boolean = false,
    onResetPrivacyPin: () -> Unit = {},
    onOpenSecurityGuard: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var autoResume by remember { mutableStateOf(true) }
    var backgroundPlay by remember { mutableStateOf(true) }
    var hardwareAcceleration by remember { mutableStateOf(true) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showResetSuccessDialog by remember { mutableStateOf(false) }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Reset Privacy Vault PIN?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to reset your 4-digit Privacy Vault PIN? All your hidden videos and songs will remain safely preserved in storage. You will be able to set a new PIN whenever you open the Privacy Vault."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetPrivacyPin()
                        showResetSuccessDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reset PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showResetSuccessDialog = false },
            title = {
                Text("PIN Reset Successful", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Your Privacy Vault PIN has been successfully cleared. When you next open the Privacy Vault, you will be prompted to create a new 4-digit PIN.")
            },
            confirmButton = {
                Button(
                    onClick = { showResetSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .glossyRedBlueWhiteBackground()
            .testTag("settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dolby Atmos Support Section ("Dollby atoms support add karo setting me")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dolby_atmos_card"),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    1.dp,
                    if (dolbyAtmosEnabled) {
                        Brush.linearGradient(listOf(Color(0xFF00D2FF), Color(0xFF3A7BD5), Color(0xFFA855F7)))
                    } else {
                        Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x22FFFFFF)))
                    }
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (dolbyAtmosEnabled) Color(0xFF00D2FF).copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SurroundSound,
                                    contentDescription = null,
                                    tint = if (dolbyAtmosEnabled) Color(0xFF00D2FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Dolby Atmos® 3D Sound",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (dolbyAtmosEnabled) "Cinema-grade 3D surround sound active" else "Spatial audio processing disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (dolbyAtmosEnabled) Color(0xFF00D2FF) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = dolbyAtmosEnabled,
                            onCheckedChange = { onToggleDolbyAtmos(it) },
                            modifier = Modifier.testTag("dolby_switch")
                        )
                    }

                    AnimatedVisibility(visible = dolbyAtmosEnabled) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // Active status badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF00D2FF).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF00D2FF).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFF00D2FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DOLBY ATMOS SPATIAL AUDIO ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00D2FF)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Acoustic Sound Profile",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Sound Profile Chips
                            val profiles = listOf("Dynamic", "Movie", "Music", "Voice")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                profiles.forEach { profile ->
                                    val isSelected = dolbyProfile.equals(profile, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSelectDolbyProfile(profile) },
                                        label = {
                                            Text(
                                                text = profile,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF00D2FF).copy(alpha = 0.25f),
                                            selectedLabelColor = Color(0xFF00D2FF)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            val profileDesc = when (dolbyProfile) {
                                "Movie" -> "Cinematic surround soundstage with crystal clear dialogue & impact"
                                "Music" -> "Studio acoustic fidelity with warm vocals & rich low-end frequency"
                                "Voice" -> "Maximum vocal clarity and speech enhancement for podcasts & lectures"
                                else -> "Intelligent adaptive real-time 360° audio optimization"
                            }
                            Text(
                                text = profileDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bass Boost Enhancement Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dolby Bass Enhancement", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Deep cinematic sub-bass boost without audio distortion",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = dolbyBassBoost,
                                    onCheckedChange = { onToggleDolbyBassBoost(it) }
                                )
                            }

                            // 3D Spatial Surround Virtualizer Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("3D Spatial Surround Virtualizer", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Wider 360-degree virtual soundstage for headphones & speakers",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = dolbySurroundVirtualizer,
                                    onCheckedChange = { onToggleDolbySurroundVirtualizer(it) }
                                )
                            }
                        }
                    }
                }
            }

            // Theme Mode Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Theme & Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentThemeMode == AppThemeMode.DARK,
                            onClick = { onThemeChange(AppThemeMode.DARK) },
                            label = { Text("Cyber Dark") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentThemeMode == AppThemeMode.AMOLED,
                            onClick = { onThemeChange(AppThemeMode.AMOLED) },
                            label = { Text("OLED Black") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentThemeMode == AppThemeMode.LIGHT,
                            onClick = { onThemeChange(AppThemeMode.LIGHT) },
                            label = { Text("Light") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentThemeMode == AppThemeMode.SYSTEM,
                            onClick = { onThemeChange(AppThemeMode.SYSTEM) },
                            label = { Text("System Default") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Playback Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Player Defaults",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Resume Playback Position", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Automatically resume from where you stopped",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoResume,
                            onCheckedChange = { autoResume = it }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Background Audio Playback", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Continue playing audio when app is minimized",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = backgroundPlay,
                            onCheckedChange = { backgroundPlay = it }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hardware Acceleration", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Smooth GPU decoding for 1080p and 4K video",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hardwareAcceleration,
                            onCheckedChange = { hardwareAcceleration = it }
                        )
                    }
                }
            }

            // Privacy Vault Security Section ("password reseat ka boton setting me add karo")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_vault_settings_card"),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8)))
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x228B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Privacy Vault Password / PIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPrivacyPinSet) "Protected with 4-digit PIN" else "No PIN configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPrivacyPinSet) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reset or clear your 4-digit vault PIN. Private videos and songs remain safely preserved in storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_privacy_pin_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPrivacyPinSet) "Reset Vault Password / PIN" else "Setup / Reset PIN",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onOpenSecurityGuard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_security_guard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Security Guard Shield (6 Layers Active)",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Storage & Formats Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SdStorage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Storage & Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Scans device storage via Android MediaStore for video and audio formats (MP4, MKV, AVI, WebM, MP3, AAC, WAV, FLAC).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRescanStorage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rescan Media Files")
                    }
                }
            }

            // App About Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "About Aroy Player",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aroy Player v2.1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Clean, fast Video & MP3 player with Dolby Atmos 3D audio, offline file scanning, swipe gestures, subtitle support, and HLS streaming.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Engine: AndroidX Media3 (ExoPlayer)\nAudio DSP: Dolby Atmos Surround Engine\nDatabase: Android Room\nUI: Jetpack Compose Material 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
