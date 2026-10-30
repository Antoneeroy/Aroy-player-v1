package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
    SYSTEM, DARK, LIGHT, AMOLED
}

private val AroyDarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003D1E),
    onPrimaryContainer = Color(0xFF75FFAE),
    secondary = BrightCyan,
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF004C70),
    onSecondaryContainer = Color(0xFFCEE5FF),
    tertiary = NeonPurple,
    onTertiary = Color(0xFF4C007C),
    tertiaryContainer = Color(0xFF6B18A5),
    onTertiaryContainer = Color(0xFFF2DAFF),
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = WordColorLightSilver,
    surfaceContainer = DarkContainer,
    error = AccentRose,
    onError = Color.White
)

private val AroyAmoledColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = NeonCyan,
    secondary = BrightCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF002A3D),
    onSecondaryContainer = BrightCyan,
    tertiary = NeonPurple,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF3B0B5E),
    onTertiaryContainer = NeonPurple,
    background = AmoledBackground,
    onBackground = Color.White,
    surface = AmoledSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF141923),
    onSurfaceVariant = Color(0xFFA0AAB8),
    surfaceContainer = Color(0xFF080B10),
    error = AccentRose,
    onError = Color.White
)

private val AroyLightColorScheme = lightColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = BrightCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = NeonPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF581C87),
    background = LightBackground,
    onBackground = WordColorBlack,
    surface = LightSurface,
    onSurface = WordColorBlack,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = WordColorDarkSlate,
    surfaceContainer = LightContainer,
    error = AccentRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.AMOLED -> true
    }

    val colorScheme = when {
        themeMode == AppThemeMode.AMOLED -> AroyAmoledColorScheme
        isDark -> AroyDarkColorScheme
        else -> AroyLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
