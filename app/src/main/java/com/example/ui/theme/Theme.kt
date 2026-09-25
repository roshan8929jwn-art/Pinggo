package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK
}

private val DarkColorScheme =
  darkColorScheme(
    primary = PinggoEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = PinggoEmeraldDark,
    onPrimaryContainer = PinggoMintLight,
    secondary = PinggoTeal,
    onSecondary = Color.White,
    tertiary = PinggoMint,
    background = DarkBgStart,
    onBackground = DarkTextPrimary,
    surface = DarkGlassSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkGlassCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder,
    error = DestructiveRed,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PinggoEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = PinggoMintLight,
    onPrimaryContainer = PinggoEmeraldDeep,
    secondary = PinggoTeal,
    onSecondary = Color.White,
    tertiary = PinggoMint,
    background = LightBgStart,
    onBackground = LightTextPrimary,
    surface = LightGlassSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightGlassCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    error = DestructiveRed,
    onError = Color.White
  )

@Composable
fun PinggoTheme(
  themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  content: @Composable () -> Unit
) {
  val isDark = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
