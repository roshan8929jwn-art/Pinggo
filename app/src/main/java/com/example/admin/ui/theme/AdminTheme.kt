package com.example.admin.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Admin Theme Palette
val AdminPinkPrimary = Color(0xFFFF2D78)
val AdminPinkSubtle = Color(0xFFFFF1F6)
val AdminNavyDark = Color(0xFF0F172A)
val AdminTextPrimary = Color(0xFF1E293B)
val AdminTextSecondary = Color(0xFF64748B)
val AdminCobalt = Color(0xFF3B82F6)
val AdminEmerald = Color(0xFF10B981)
val AdminRuby = Color(0xFFEF4444)
val AdminAmber = Color(0xFFF59E0B)

val AdminBackgroundBrush = Brush.verticalGradient(
  colors = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFFFF6F9),
    Color(0xFFF1F5F9)
  )
)

private val AdminColorScheme = lightColorScheme(
  primary = AdminPinkPrimary,
  onPrimary = Color.White,
  background = Color(0xFFF8FAFC),
  surface = Color.White,
  onSurface = AdminTextPrimary
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = AdminColorScheme,
    content = content
  )
}

@Composable
fun AdminLiquidBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AdminBackgroundBrush)
  ) {
    content()
  }
}

@Composable
fun AdminGlassCard(
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(24.dp),
  elevation: Dp = 8.dp,
  content: @Composable () -> Unit
) {
  Surface(
    modifier = modifier
      .shadow(elevation, shape, ambientColor = Color(0x1A000000), spotColor = Color(0x1AFF2D78))
      .border(1.dp, Color.White.copy(alpha = 0.85f), shape)
      .clip(shape),
    color = Color.White.copy(alpha = 0.88f),
    shape = shape
  ) {
    content()
  }
}
