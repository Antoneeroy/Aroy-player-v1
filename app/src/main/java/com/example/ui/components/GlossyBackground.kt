package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AuroraAmberGlow
import com.example.ui.theme.AuroraCyanGlow
import com.example.ui.theme.AuroraDarkBase
import com.example.ui.theme.AuroraEmeraldGlow
import com.example.ui.theme.AuroraPinkGlow
import com.example.ui.theme.AuroraPurpleGlow
import com.example.ui.theme.AuroraSlateMid
import com.example.ui.theme.AuroraTealGlow
import com.example.ui.theme.GlossyWhiteSheen
import com.example.ui.theme.LightBackground

fun Modifier.glossyRedBlueWhiteBackground(
    isDark: Boolean = true
): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height

    if (isDark) {
        // --- 1793368649701 Deep Obsidian Liquid Glass Aurora Background ---
        // 1. Deep midnight slate base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(AuroraDarkBase, AuroraSlateMid, AuroraDarkBase)
            )
        )

        // 2. Diagonal Iridescent Aurora Ribbon (as seen in 1793368649701 reference)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    AuroraCyanGlow.copy(alpha = 0.20f),
                    AuroraEmeraldGlow.copy(alpha = 0.18f),
                    AuroraAmberGlow.copy(alpha = 0.22f),
                    AuroraPinkGlow.copy(alpha = 0.24f),
                    AuroraPurpleGlow.copy(alpha = 0.26f),
                    Color.Transparent
                ),
                start = Offset(width * 0.95f, height * 0.18f),
                end = Offset(width * 0.05f, height * 0.78f)
            )
        )

        // 3. Top-Right Cyan/Teal Atmospheric Bloom
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraCyanGlow.copy(alpha = 0.22f),
                    AuroraTealGlow.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(width * 0.85f, height * 0.10f),
                radius = width * 0.75f
            ),
            radius = width * 0.75f,
            center = Offset(width * 0.85f, height * 0.10f)
        )

        // 4. Center-Left Magenta/Violet Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraPinkGlow.copy(alpha = 0.15f),
                    AuroraPurpleGlow.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(width * 0.20f, height * 0.55f),
                radius = width * 0.80f
            ),
            radius = width * 0.80f,
            center = Offset(width * 0.20f, height * 0.55f)
        )

        // 5. Specular Liquid Glass Top Highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GlossyWhiteSheen.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, 0f),
                radius = width * 0.65f
            ),
            radius = width * 0.65f,
            center = Offset(width * 0.5f, 0f)
        )
    } else {
        // --- Over Light Day Mode Liquid Glass Background ---
        // 1. Crisp, luminous pearl slate base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFEDF2F7),
                    Color(0xFFE2E8F0)
                )
            )
        )

        // 2. Soft, airy pastel aurora light streams
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    AuroraCyanGlow.copy(alpha = 0.10f),
                    AuroraPinkGlow.copy(alpha = 0.08f),
                    AuroraPurpleGlow.copy(alpha = 0.09f),
                    Color.Transparent
                ),
                start = Offset(width * 0.90f, height * 0.15f),
                end = Offset(width * 0.10f, height * 0.80f)
            )
        )

        // 3. Ambient top crystal light
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.60f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.05f),
                radius = width * 0.80f
            ),
            radius = width * 0.80f,
            center = Offset(width * 0.5f, height * 0.05f)
        )
    }
}

@Composable
fun GlossyContainer(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .glossyRedBlueWhiteBackground(isDark)
    ) {
        content()
    }
}
