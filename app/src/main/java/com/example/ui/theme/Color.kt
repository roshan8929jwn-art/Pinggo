package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Pinggo Pink Primary Palette (Brand Colors)
val PinggoPinkPrimary = Color(0xFFFF69B4) // Hot Pink brand color
val PinggoPinkLight = Color(0xFFFFB6D9) // Soft Pink brand color
val PinggoPinkDeep = Color(0xFFE91E63) 
val PinggoPinkGlow = Color(0x33FF69B4)

// Neutral Palette - CLEAN SOLID SURFACE THEME
val PinggoWhite = Color(0xFFFFFFFF)
val PinggoBlack = Color(0xFF111827) // Dark charcoal

// Status & Indicators
val OnlinePink = Color(0xFFFF69B4)
val OnlineGreen = Color(0xFF22C55E)
val OfflineGray = Color(0xFFC7C7CC)
val UnreadBadgeColor = Color(0xFFFF69B4)
val DestructiveRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)

// Legacy Compatibility Aliases
val PinggoEmeraldPrimary = PinggoPinkPrimary
val PinggoMint = Color(0xFF22C55E)
val PinggoMintUltraLight = Color(0xFFD1FAE5)

// Dynamic theme-aware color properties that connect directly to the shared PinggoTheme single source of truth
val PinggoOffWhite: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.background

val SolidSurfaceWhite: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.cardBackground

val SolidSurfaceVariant: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.surfaceVariant

val SolidBorderColor: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.border

val SolidBorderSubtle: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.borderSubtle

val CrystalGlassSurface: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.cardBackground

val CrystalGlassBorder: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.border

val CrystalGlassHighlight: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.cardBackground

val CrystalGlassShadow: Color
  @Composable
  @ReadOnlyComposable
  get() = if (PinggoTheme.colors.isDark) Color(0x33000000) else Color(0x0A000000)

val LightPrimaryText: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.primaryText

val LightSecondaryText: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.secondaryText

val LightPlaceholderText: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.placeholderText

val DarkPrimaryText: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.primaryText

val DarkSecondaryText: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.secondaryText

val DarkPlaceholderText: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.placeholderText

val DarkBackground: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.background

val DarkSurface: Color
  @Composable
  @ReadOnlyComposable
  get() = PinggoTheme.colors.surface
