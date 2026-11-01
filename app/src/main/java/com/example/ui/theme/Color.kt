package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Low-Light Gloss & Liquid Aurora Palette (replaces all green with low-light frosted gloss)
val LowLightGlossCyan = Color(0xFF38BDF8)           // Primary Low-Light Ice Sheen
val LowLightGlossSilver = Color(0xFFE2E8F0)         // Luminous Platinum Sheen
val LowLightGlossViolet = Color(0xFF818CF8)         // Aurora Violet Sheen
val LowLightGlossSapphire = Color(0xFF2563EB)       // Deep Glass Sapphire
val LowLightGlossBorder = Color(0x5538BDF8)         // Frosted Glass Border
val LowLightGlossBadgeBorder = Color(0x6638BDF8)    // Badge border
val LowLightGlossSurface = Color(0x2238BDF8)        // Low-light translucent surface
val LowLightGlossBadgeContainer = Color(0x3338BDF8) // Low-light gloss badge pill
val LowLightGlossBadgeText = Color(0xFFBAE6FD)      // Low-light badge text
val LowLightGlossIndicator = Color(0xFF38BDF8)      // Selected nav indicator

// Compatibility alias: redirects AccentGreen and AccentEmerald to LowLightGlossCyan so zero green remains!
val AccentGreen = LowLightGlossCyan
val AccentEmerald = LowLightGlossCyan
val NeonCyan = Color(0xFF00E5FF)
val BrightCyan = Color(0xFF38BDF8)
val NeonPurple = Color(0xFFA855F7)
val ElectricIndigo = Color(0xFF6366F1)
val DeepIndigo = Color(0xFF4F46E5)

// Glossy Red-Blue-White 5% Ambient Blend Accents
val GlossyRedGlow = Color(0xFFFF2A4D)   // 5% red radiance
val GlossyBlueGlow = Color(0xFF2563EB)  // 5% blue radiance
val GlossyWhiteSheen = Color(0xFFFFFFFF) // 5% specular gloss

// 1793368649701 Liquid Glass & Aurora Palette (from user reference)
val AuroraDarkBase = Color(0xFF0C131F)
val AuroraSlateMid = Color(0xFF131B2A)
val AuroraCyanGlow = Color(0xFF0EA5E9)
val AuroraTealGlow = Color(0xFF0284C7)
val AuroraEmeraldGlow = LowLightGlossCyan // Converted from green to low-light gloss!
val AuroraPurpleGlow = Color(0xFFA855F7)
val AuroraPinkGlow = Color(0xFFEC4899)
val AuroraAmberGlow = Color(0xFFF59E0B)

// Liquid Glass Palette (1793368649701 inspired)
val LiquidGlassSurfaceLight = Color(0xF0FFFFFF) // Milky translucent white liquid glass
val LiquidGlassBorderLight = Color(0x6694A3B8)
val LiquidGlassSurfaceDark = Color(0x55141E2F)  // Frosted obsidian glass from 1793368649701
val LiquidGlassBorderDark = Color(0x44FFFFFF)

// High-contrast Word Colors for 100% visibility ("word color black")
val WordColorBlack = Color(0xFF000000)        // Pure crisp black
val WordColorDarkSlate = Color(0xFF1E293B)     // Charcoal for subtitles
val WordColorSecondaryBlack = Color(0xFF334155) // Slate dark for secondary text
val WordColorWhite = Color(0xFFFFFFFF)        // Pure white for dark mode
val WordColorLightSilver = Color(0xFFE2E8F0)  // Silver for dark mode subtitles

val GlassBorder = Color(0x33FFFFFF)
val GlassSurface = Color(0x22FFFFFF)

val DarkSurface = Color(0xFF10141E)
val DarkSurfaceVariant = Color(0xFF181F2E)
val DarkBackground = Color(0xFF080B12)
val DarkContainer = Color(0xFF131A26)

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0B0E14)

val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightContainer = Color(0xFFCBD5E1)

val AccentRose = Color(0xFFF43F5E)
val AccentAmber = Color(0xFFF59E0B)

val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFFE2E8F0)
val TextPrimaryLight = Color(0xFF000000) // Pure black
val TextSecondaryLight = Color(0xFF334155) // Dark slate
