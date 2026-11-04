package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.example.ui.AroyViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure Coil to decode video frames for thumbnails
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            val viewModel: AroyViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isPrivacyUnlocked by viewModel.isPrivacyUnlocked.collectAsStateWithLifecycle()
            val privateUris by viewModel.privateUris.collectAsStateWithLifecycle()
            val playbackSnapshot by viewModel.playbackState.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current

            // Security Guard: Apply FLAG_SECURE whenever Privacy Vault is open or private media is playing
            val isPrivateMediaActive = playbackSnapshot.currentMedia?.uri?.let { it in privateUris } == true
            val shouldProtectScreen = uiState.showPrivacyVault || isPrivacyUnlocked || isPrivateMediaActive

            LaunchedEffect(shouldProtectScreen) {
                try {
                    if (shouldProtectScreen) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                } catch (_: Throwable) {}
            }

            // Security Guard: Auto-lock Privacy Vault when app moves to background
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.onAppBackgrounded()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            MyApplicationTheme(themeMode = uiState.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
