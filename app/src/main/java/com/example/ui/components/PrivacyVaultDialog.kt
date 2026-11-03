package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MediaItemData
import com.example.ui.VaultUnlockResult
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.LowLightGlossBadgeBorder
import com.example.ui.theme.LowLightGlossBadgeContainer
import com.example.ui.theme.LowLightGlossBadgeText
import com.example.ui.theme.LowLightGlossCyan

@Composable
fun PrivacyVaultDialog(
    isPinSet: Boolean,
    isUnlocked: Boolean,
    privateVideos: List<MediaItemData>,
    privateAudios: List<MediaItemData>,
    publicVideos: List<MediaItemData>,
    publicAudios: List<MediaItemData>,
    onSetPin: (String) -> Boolean,
    onUnlock: (String) -> VaultUnlockResult,
    onResetPin: (() -> Unit)? = null,
    onLock: () -> Unit,
    onLockMedia: (MediaItemData) -> Unit,
    onUnlockMedia: (MediaItemData) -> Unit,
    onPlayMedia: (MediaItemData) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddPicker by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            onLock()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (!isUnlocked) 24.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = if (!isUnlocked) {
                    Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 330.dp)
                        .wrapContentHeight()
                        .testTag("privacy_vault_modal")
                } else {
                    Modifier
                        .fillMaxSize()
                        .testTag("privacy_vault_modal")
                },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xF20F172A),
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0x8838BDF8),
                            Color(0x88A855F7),
                            Color(0x88EC4899),
                            Color(0x8838BDF8)
                        )
                    )
                ),
                shadowElevation = 24.dp
            ) {
                if (!isUnlocked) {
                    // PIN ENTRY / PIN SETUP SCREEN (Compact ~2 inch card)
                    VaultPinScreen(
                        isPinSet = isPinSet,
                        onSetPin = onSetPin,
                        onUnlock = onUnlock,
                        onResetPin = onResetPin,
                        onDismiss = {
                            onLock()
                            onDismiss()
                        }
                    )
                } else {
                    // VAULT UNLOCKED: MAIN VAULT CONTENT
                    VaultContentScreen(
                        privateVideos = privateVideos,
                        privateAudios = privateAudios,
                        onOpenAddPicker = { showAddPicker = true },
                        onChangePinClick = { showChangePinDialog = true },
                        onLockExit = {
                            onLock()
                            onDismiss()
                        },
                        onPlayMedia = onPlayMedia,
                        onUnlockMedia = onUnlockMedia
                    )
                }
            }
        }
    }

    // Add Media Picker Dialog
    if (showAddPicker) {
        AddMediaToVaultDialog(
            publicVideos = publicVideos,
            publicAudios = publicAudios,
            onLockItems = { items ->
                items.forEach { onLockMedia(it) }
                showAddPicker = false
            },
            onDismiss = { showAddPicker = false }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        ChangePinDialog(
            onConfirmChange = { newPin ->
                onSetPin(newPin)
                showChangePinDialog = false
            },
            onDismiss = { showChangePinDialog = false }
        )
    }
}

@Composable
private fun VaultPinScreen(
    isPinSet: Boolean,
    onSetPin: (String) -> Boolean,
    onUnlock: (String) -> VaultUnlockResult,
    onResetPin: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Reset Privacy Vault PIN?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to reset your 4-digit Vault PIN? All your private videos and songs will remain safely stored. You will be able to create a new PIN immediately."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetPin?.invoke()
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Compact Vault Glow Icon (~50dp)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x3338BDF8),
                            Color(0x11A855F7),
                            Color.Transparent
                        )
                    )
                )
                .background(Color(0x22FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPinSet) Icons.Default.Lock else Icons.Default.Shield,
                contentDescription = null,
                tint = LowLightGlossCyan,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isPinSet) "Privacy Vault" else "Setup Privacy Vault",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isPinSet) {
                "Enter your 4-digit PIN to access private media"
            } else {
                "Create a 4-digit security PIN to protect personal media"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PIN Input
        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                    pin = it
                    errorMessage = null
                }
            },
            label = { Text("4-digit PIN", color = Color.White.copy(alpha = 0.7f)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("privacy_pin_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = LowLightGlossCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = LowLightGlossCyan
            )
        )

        if (!isPinSet) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = confirmPin,
                onValueChange = {
                    if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                        confirmPin = it
                        errorMessage = null
                    }
                },
                label = { Text("Confirm 4-digit PIN", color = Color.White.copy(alpha = 0.7f)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_confirm_pin_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = LowLightGlossCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = LowLightGlossCyan
                )
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ $errorMessage",
                color = Color(0xFFEF4444),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (pin.length != 4) {
                        errorMessage = "PIN must be exactly 4 digits."
                        return@Button
                    }

                    if (!isPinSet) {
                        if (pin != confirmPin) {
                            errorMessage = "PINs do not match. Please re-enter."
                            return@Button
                        }
                        val success = onSetPin(pin)
                        if (!success) {
                            errorMessage = "Failed to save PIN. Try again."
                        }
                    } else {
                        val result = onUnlock(pin)
                        when (result) {
                            is VaultUnlockResult.Success -> {
                                errorMessage = null
                            }
                            is VaultUnlockResult.WrongPin -> {
                                errorMessage = "Incorrect PIN. ${result.attemptsLeft} attempt(s) remaining."
                                pin = ""
                            }
                            is VaultUnlockResult.LockedOut -> {
                                errorMessage = "Security Lockout: Too many attempts. Try again in ${result.secondsLeft}s."
                                pin = ""
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("privacy_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LowLightGlossCyan,
                    contentColor = Color(0xFF0F172A)
                )
            ) {
                Text(
                    text = if (isPinSet) "Unlock" else "Create",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Reset PIN link if user forgot or wants to reset
        if (isPinSet && onResetPin != null) {
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                onClick = { showResetConfirmDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Forgot / Reset PIN?",
                    color = Color(0xFF38BDF8),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun VaultContentScreen(
    privateVideos: List<MediaItemData>,
    privateAudios: List<MediaItemData>,
    onOpenAddPicker: () -> Unit,
    onChangePinClick: () -> Unit,
    onLockExit: () -> Unit,
    onPlayMedia: (MediaItemData) -> Unit,
    onUnlockMedia: (MediaItemData) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Videos, 1 = Audios

    Column(modifier = Modifier.fillMaxSize()) {
        // Vault Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = LowLightGlossCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Privacy Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Hidden & Encrypted from Gallery",
                    style = MaterialTheme.typography.labelSmall,
                    color = LowLightGlossCyan
                )
            }

            // Change PIN Button
            IconButton(
                onClick = onChangePinClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Change PIN",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Lock & Close Button
            IconButton(
                onClick = onLockExit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Lock & Exit",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        // Tabs: Private Videos vs Private Music
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = LowLightGlossCyan,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 0) LowLightGlossCyan else Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Videos (${privateVideos.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) LowLightGlossCyan else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 1) LowLightGlossCyan else Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Music (${privateAudios.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) LowLightGlossCyan else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }

        // Vault Media List Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val currentList = if (selectedTab == 0) privateVideos else privateAudios

            if (currentList.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Movie else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedTab == 0) "No Private Videos" else "No Private Music",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hide your personal files so they won't appear in public lists.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onOpenAddPicker,
                        colors = ButtonDefaults.buttonColors(containerColor = LowLightGlossCyan, contentColor = Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Media to Vault", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentList, key = { it.uri }) { item ->
                        PrivateMediaItemRow(
                            item = item,
                            onPlay = { onPlayMedia(item) },
                            onUnlock = { onUnlockMedia(item) }
                        )
                    }
                }
            }
        }

        // Bottom Action Bar inside Vault
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0x33000000),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onLockExit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lock & Exit")
                }

                Button(
                    onClick = onOpenAddPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = LowLightGlossCyan, contentColor = Color(0xFF0F172A))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Media", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PrivateMediaItemRow(
    item: MediaItemData,
    onPlay: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (item.thumbnailUri != null) {
                    AsyncImage(
                        model = item.thumbnailUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (item.isVideo) Icons.Default.Movie else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = LowLightGlossCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.formattedDuration} • ${if (item.formattedSize.isNotBlank()) item.formattedSize else item.folderName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Play Button
            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = LowLightGlossCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Unlock / Restore Button
            IconButton(
                onClick = onUnlock,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = "Unlock to library",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AddMediaToVaultDialog(
    publicVideos: List<MediaItemData>,
    publicAudios: List<MediaItemData>,
    onLockItems: (List<MediaItemData>) -> Unit,
    onDismiss: () -> Unit
) {
    var pickerTab by remember { mutableIntStateOf(0) }
    val selectedUris = remember { mutableStateListOf<String>() }

    val currentList = if (pickerTab == 0) publicVideos else publicAudios

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Media to Hide",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                TabRow(
                    selectedTabIndex = pickerTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = pickerTab == 0,
                        onClick = { pickerTab = 0 },
                        text = { Text("Videos (${publicVideos.size})") }
                    )
                    Tab(
                        selected = pickerTab == 1,
                        onClick = { pickerTab = 1 },
                        text = { Text("Music (${publicAudios.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (currentList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No public files found to hide.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(currentList, key = { it.uri }) { item ->
                            val isChecked = selectedUris.contains(item.uri)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isChecked) selectedUris.remove(item.uri) else selectedUris.add(item.uri)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedUris.add(item.uri) else selectedUris.remove(item.uri)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = LowLightGlossCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.formattedDuration} • ${item.folderName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val itemsToLock = (publicVideos + publicAudios).filter { it.uri in selectedUris }
                    onLockItems(itemsToLock)
                },
                enabled = selectedUris.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = LowLightGlossCyan, contentColor = Color(0xFF0F172A))
            ) {
                Text("Lock to Vault (${selectedUris.size})", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangePinDialog(
    onConfirmChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Vault PIN", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter a new 4-digit security PIN for your vault.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { ch -> ch.isDigit() }) newPin = it
                    },
                    label = { Text("New 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmNewPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmNewPin = it
                    },
                    label = { Text("Confirm New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "⚠️ $errorMsg", color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin.length != 4) {
                        errorMsg = "PIN must be 4 digits."
                        return@Button
                    }
                    if (newPin != confirmNewPin) {
                        errorMsg = "PINs do not match."
                        return@Button
                    }
                    onConfirmChange(newPin)
                }
            ) {
                Text("Save PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
