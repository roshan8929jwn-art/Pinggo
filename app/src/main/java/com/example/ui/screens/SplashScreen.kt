package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoFullLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onReady: () -> Unit
) {
  val scale = remember { Animatable(0.5f) }

  LaunchedEffect(Unit) {
    scale.animateTo(
      targetValue = 1f,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
      )
    )
    delay(1400)
    onReady()
  }

  LiquidGlassBackground {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      // Centered Pinggo Logo with subtle emerald glow and "Same Vibes, New Experience"
      PinggoFullLogo(
        modifier = Modifier.scale(scale.value),
        iconSize = 120.dp,
        wordmarkSize = 42.sp,
        showTagline = true,
        subtitle = "Same Vibes, New Experience"
      )

      // Bottom indicator glass pill
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(bottom = 20.dp)
          .width(48.dp)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(Color(0x66FFFFFF))
      )
    }
  }
}
