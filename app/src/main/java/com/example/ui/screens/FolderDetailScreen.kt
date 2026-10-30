package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FolderItem
import com.example.data.model.MediaItemData
import com.example.ui.components.glossyRedBlueWhiteBackground
import com.example.ui.screens.tabs.AudioItemRow
import com.example.ui.screens.tabs.RectangularCircularVideoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    folder: FolderItem,
    items: List<MediaItemData>,
    onBack: () -> Unit,
    onItemClick: (MediaItemData) -> Unit,
    onAddToPlaylist: (MediaItemData) -> Unit
) {
    BackHandler {
        onBack()
    }

    val folderFiles = items.filter { it.folderName == folder.name }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${folderFiles.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (folderFiles.isNotEmpty()) {
                        Button(
                            onClick = { onItemClick(folderFiles.first()) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play All")
                        }
                    }
                }
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .glossyRedBlueWhiteBackground()
            .testTag("folder_detail_screen")
    ) { innerPadding ->
        if (folderFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files found in ${folder.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp)
            ) {
                items(folderFiles, key = { it.uri }) { mediaItem ->
                    if (mediaItem.isVideo) {
                        Box(modifier = Modifier.padding(vertical = 6.dp)) {
                            RectangularCircularVideoCard(
                                video = mediaItem,
                                layoutColumns = 1,
                                onClick = { onItemClick(mediaItem) },
                                onAddToPlaylist = { onAddToPlaylist(mediaItem) }
                            )
                        }
                    } else {
                        AudioItemRow(
                            audio = mediaItem,
                            onClick = { onItemClick(mediaItem) },
                            onAddToPlaylist = { onAddToPlaylist(mediaItem) }
                        )
                    }
                }
            }
        }
    }
}
