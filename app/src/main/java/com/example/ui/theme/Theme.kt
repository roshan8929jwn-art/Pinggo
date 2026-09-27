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

private val LightColorScheme = lightColorScheme(
  primary = PinggoPinkPrimary,
  onPrimary = Color.White,
  primaryContainer = PinggoPinkLight,
  onPrimaryContainer = PinggoPinkDeep,
  secondary = PinggoPinkPrimary,
  onSecondary = Color.White,
  tertiary = PinggoPinkLight,
  background = PinggoWhite,
  onBackground = LightPrimaryText,
  surface = PinggoWhite,
  onSurface = LightPrimaryText,
  surfaceVariant = PinggoOffWhite,
  onSurfaceVariant = LightSecondaryText,
  outline = LightSecondaryText.copy(alpha = 0.2f),
  outlineVariant = LightSecondaryText.copy(alpha = 0.1f),
  error = DestructiveRed,
  onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
  primary = PinggoPinkPrimary,
  onPrimary = Color.White,
  primaryContainer = PinggoPinkDeep,
  onPrimaryContainer = PinggoPinkLight,
  secondary = PinggoPinkPrimary,
  onSecondary = Color.White,
  tertiary = PinggoPinkLight,
  background = PinggoWhite, // Forced white
  onBackground = DarkPrimaryText,
  surface = PinggoWhite,
  onSurface = DarkPrimaryText,
  surfaceVariant = PinggoOffWhite,
  onSurfaceVariant = DarkSecondaryText,
  outline = DarkSecondaryText.copy(alpha = 0.2f),
  outlineVariant = DarkSecondaryText.copy(alpha = 0.1f),
  error = DestructiveRed,
  onError = Color.White
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
