package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

enum class GlassDesign {
  CLEAR,   // Clean Solid White
  PINK,    // Soft Pink Tint
  CRYSTAL  // Minimal Pure
}

@Immutable
data class PinggoCustomColors(
  val background: Color,
  val surface: Color,
  val surfaceVariant: Color,
  val cardBackground: Color,
  val primaryText: Color,
  val secondaryText: Color,
  val placeholderText: Color,
  val border: Color,
  val borderSubtle: Color,
  val accent: Color,
  val isDark: Boolean,
  val surfaceStyle: GlassDesign
)

fun getPinggoThemeColors(isDark: Boolean, style: GlassDesign): PinggoCustomColors {
  return if (isDark) {
    when (style) {
      GlassDesign.PINK -> PinggoCustomColors(
        background = Color(0xFF161016),      // Deep dark background with subtle pink tint
        surface = Color(0xFF241923),         // Solid dark plum/pink surface
        surfaceVariant = Color(0xFF352433),  // Slightly lighter variant
        cardBackground = Color(0xFF241923),  // Card background
        primaryText = Color(0xFFFDF2F8),     // High-contrast clean light text
        secondaryText = Color(0xFFD4B8C8),   // Soft warm light gray
        placeholderText = Color(0xFF9E8293), // Muted placeholder
        border = Color(0xFF4C2A40),          // Subtle pink-tinted dark border
        borderSubtle = Color(0xFF331B2A),
        accent = PinggoPinkPrimary,
        isDark = true,
        surfaceStyle = style
      )
      GlassDesign.CRYSTAL -> PinggoCustomColors(
        background = Color(0xFF090D16),      // Pure minimal deep slate dark
        surface = Color(0xFF131B2E),         // Minimal pure solid dark surface
        surfaceVariant = Color(0xFF1E293B),  // Minimal slate variant
        cardBackground = Color(0xFF131B2E),
        primaryText = Color(0xFFF8FAFC),     // Pure crisp readable white text
        secondaryText = Color(0xFF94A3B8),   // Neutral slate text
        placeholderText = Color(0xFF64748B),
        border = Color(0xFF1E293B),          // Very subtle dark border
        borderSubtle = Color(0xFF0F172A),
        accent = PinggoPinkPrimary,
        isDark = true,
        surfaceStyle = style
      )
      GlassDesign.CLEAR -> PinggoCustomColors(
        background = Color(0xFF111827),      // Clean solid dark charcoal background
        surface = Color(0xFF1F2937),         // Clean solid dark gray surface
        surfaceVariant = Color(0xFF374151),  // Solid dark gray variant
        cardBackground = Color(0xFF1F2937),
        primaryText = Color(0xFFF9FAFB),     // Clean readable light text
        secondaryText = Color(0xFF9CA3AF),   // Soft neutral gray
        placeholderText = Color(0xFF6B7280),
        border = Color(0xFF374151),          // Clean solid dark border
        borderSubtle = Color(0xFF1F2937),
        accent = PinggoPinkPrimary,
        isDark = true,
        surfaceStyle = style
      )
    }
  } else {
    when (style) {
      GlassDesign.PINK -> PinggoCustomColors(
        background = Color(0xFFFFF5F8),      // Soft pink-tinted surface
        surface = Color(0xFFFFFFFF),         // Clean solid white surface
        surfaceVariant = Color(0xFFFFEDF4),  // Subtle pink accent container
        cardBackground = Color(0xFFFFFFFF),  // Solid card
        primaryText = Color(0xFF111827),     // Dark readable charcoal text
        secondaryText = Color(0xFF6B7280),   // Soft neutral gray
        placeholderText = Color(0xFF9CA3AF),
        border = Color(0xFFFFD1E3),          // Delicate subtle pink border
        borderSubtle = Color(0xFFFFE5F0),
        accent = PinggoPinkPrimary,
        isDark = false,
        surfaceStyle = style
      )
      GlassDesign.CRYSTAL -> PinggoCustomColors(
        background = Color(0xFFF5F5F7),      // Minimal pure clean neutral surface
        surface = Color(0xFFFFFFFF),         // Pure solid white
        surfaceVariant = Color(0xFFEFEFF2),  // Minimal neutral variant
        cardBackground = Color(0xFFFFFFFF),
        primaryText = Color(0xFF0F172A),     // Crisp dark charcoal
        secondaryText = Color(0xFF64748B),   // Neutral slate secondary
        placeholderText = Color(0xFF94A3B8),
        border = Color(0xFFE2E8F0),          // Very subtle border
        borderSubtle = Color(0xFFF1F5F9),
        accent = PinggoPinkPrimary,
        isDark = false,
        surfaceStyle = style
      )
      GlassDesign.CLEAR -> PinggoCustomColors(
        background = Color(0xFFF8F9FA),      // Clean soft off-white background
        surface = Color(0xFFFFFFFF),         // Clean white surfaces
        surfaceVariant = Color(0xFFF3F4F6),  // Neutral light gray
        cardBackground = Color(0xFFFFFFFF),
        primaryText = Color(0xFF111827),     // Dark readable charcoal text
        secondaryText = Color(0xFF6B7280),   // Soft neutral gray
        placeholderText = Color(0xFF9CA3AF),
        border = Color(0xFFE5E7EB),          // Subtle neutral border
        borderSubtle = Color(0xFFF3F4F6),
        accent = PinggoPinkPrimary,
        isDark = false,
        surfaceStyle = style
      )
    }
  }
}

val LocalGlassDesign = staticCompositionLocalOf { GlassDesign.CLEAR }
val LocalPinggoCustomColors = staticCompositionLocalOf {
  getPinggoThemeColors(isDark = false, style = GlassDesign.CLEAR)
}

object PinggoTheme {
  val colors: PinggoCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPinggoCustomColors.current

  val surfaceStyle: GlassDesign
    @Composable
    @ReadOnlyComposable
    get() = LocalGlassDesign.current
}

@Composable
fun PinggoTheme(
  isDarkTheme: Boolean = isSystemInDarkTheme(),
  glassDesign: GlassDesign = GlassDesign.CLEAR,
  content: @Composable () -> Unit
) {
  val customColors = remember(isDarkTheme, glassDesign) {
    getPinggoThemeColors(isDarkTheme, glassDesign)
  }

  val colorScheme = if (isDarkTheme) {
    darkColorScheme(
      primary = customColors.accent,
      onPrimary = Color.White,
      primaryContainer = customColors.surfaceVariant,
      onPrimaryContainer = PinggoPinkLight,
      secondary = customColors.accent,
      onSecondary = Color.White,
      tertiary = PinggoPinkLight,
      background = customColors.background,
      onBackground = customColors.primaryText,
      surface = customColors.surface,
      onSurface = customColors.primaryText,
      surfaceVariant = customColors.surfaceVariant,
      onSurfaceVariant = customColors.secondaryText,
      outline = customColors.border,
      outlineVariant = customColors.borderSubtle,
      error = DestructiveRed,
      onError = Color.White
    )
  } else {
    lightColorScheme(
      primary = customColors.accent,
      onPrimary = Color.White,
      primaryContainer = customColors.surfaceVariant,
      onPrimaryContainer = PinggoPinkDeep,
      secondary = customColors.accent,
      onSecondary = Color.White,
      tertiary = PinggoPinkLight,
      background = customColors.background,
      onBackground = customColors.primaryText,
      surface = customColors.surface,
      onSurface = customColors.primaryText,
      surfaceVariant = customColors.surfaceVariant,
      onSurfaceVariant = customColors.secondaryText,
      outline = customColors.border,
      outlineVariant = customColors.borderSubtle,
      error = DestructiveRed,
      onError = Color.White
    )
  }

  CompositionLocalProvider(
    LocalGlassDesign provides glassDesign,
    LocalPinggoCustomColors provides customColors
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
