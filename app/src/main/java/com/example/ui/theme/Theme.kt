package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

enum class GlassDesign {
  CLEAR,
  PINK,
  CRYSTAL
}

val LocalGlassDesign = staticCompositionLocalOf { GlassDesign.CLEAR }

private val LightColorScheme =
  lightColorScheme(
    primary = PinggoPinkPrimary,
    onPrimary = PinggoWhite,
    primaryContainer = PinggoPinkLight,
    onPrimaryContainer = PinggoPinkDeep,
    secondary = PinggoPinkLight,
    onSecondary = PinggoBlack,
    tertiary = PinggoPinkLight,
    background = PinggoOffWhite,
    onBackground = PinggoBlack,
    surface = GlassSurface,
    onSurface = PinggoBlack,
    surfaceVariant = GlassCard,
    onSurfaceVariant = PinggoGray,
    outline = GlassBorder,
    error = DestructiveRed,
    onError = PinggoWhite
  )

// For now, we'll use a slightly darker version of the pink theme for "Dark" if requested, 
// but the user specified a clean white/pink theme as default.
private val DarkColorScheme =
  darkColorScheme(
    primary = PinggoPinkPrimary,
    onPrimary = PinggoWhite,
    primaryContainer = PinggoPinkDeep,
    onPrimaryContainer = PinggoPinkLight,
    secondary = PinggoPinkLight,
    onSecondary = PinggoBlack,
    tertiary = PinggoPinkLight,
    background = PinggoBlack,
    onBackground = PinggoWhite,
    surface = Color(0xCC1C1C1E),
    onSurface = PinggoWhite,
    surfaceVariant = Color(0x992C2C2E),
    onSurfaceVariant = PinggoLightGray,
    outline = Color(0x4DFF69B4),
    error = DestructiveRed,
    onError = PinggoWhite
  )

@Composable
fun PinggoTheme(
  isDarkTheme: Boolean = isSystemInDarkTheme(),
  glassDesign: GlassDesign = GlassDesign.CLEAR,
  content: @Composable () -> Unit
) {
  val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

  CompositionLocalProvider(LocalGlassDesign provides glassDesign) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
