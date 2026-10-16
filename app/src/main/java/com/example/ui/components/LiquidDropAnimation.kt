package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PinggoPinkPrimary

/**
 * Global composition local to track the screen coordinates of the most recent user tap.
 */
val LocalLastTapPosition = staticCompositionLocalOf { mutableStateOf<Offset?>(null) }

/**
 * Checks whether system reduced-motion or animator duration scale is disabled.
 */
@Composable
fun rememberReducedMotion(): Boolean {
  val context = LocalContext.current
  return remember {
    try {
      val transitionScale = android.provider.Settings.Global.getFloat(
        context.contentResolver,
        android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
        1f
      )
      val animatorScale = android.provider.Settings.Global.getFloat(
        context.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
      )
      transitionScale == 0f || animatorScale == 0f
    } catch (_: Exception) {
      false
    }
  }
}

/**
 * Clean native touch feedback modifier.
 * Glass reflections, water droplets, and shine effects have been removed.
 */
@Composable
fun Modifier.liquidDrop(
  interactionSource: MutableInteractionSource,
  enabled: Boolean = true,
  isPinkTint: Boolean = true,
  maxRadius: Dp = 36.dp,
  durationMillis: Int = 230
): Modifier {
  return this
}

/**
 * Clean clickable helper.
 */
@Composable
fun Modifier.liquidDropClickable(
  onClick: () -> Unit,
  enabled: Boolean = true,
  isPinkTint: Boolean = true,
  boundedRipple: Boolean = true,
  rippleColor: Color = PinggoPinkPrimary
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  return this.clickable(
    enabled = enabled,
    interactionSource = interactionSource,
    indication = ripple(bounded = boundedRipple, color = rippleColor),
    onClick = onClick
  )
}

/**
 * Clean Screen-level Transition Container.
 * Replaces liquid meniscus and water drop rings with a smooth, solid, clean crossfade.
 */
@Composable
fun <T> LiquidDropScreenTransition(
  targetState: T,
  modifier: Modifier = Modifier,
  isBackNavigation: Boolean = false,
  origin: Offset? = null,
  durationMillis: Int = 250,
  content: @Composable (T) -> Unit
) {
  val isReducedMotion = rememberReducedMotion()
  if (isReducedMotion) {
    Box(modifier = modifier.fillMaxSize()) {
      content(targetState)
    }
  } else {
    Crossfade(
      targetState = targetState,
      animationSpec = tween(durationMillis = durationMillis),
      modifier = modifier.fillMaxSize(),
      label = "ScreenTransition"
    ) { state ->
      content(state)
    }
  }
}

/**
 * Clean modal reveal modifier.
 * Replaces glass droplet ripples with clean native appearance.
 */
@Composable
fun Modifier.liquidDropModalReveal(
  isPinkTint: Boolean = true,
  durationMillis: Int = 200
): Modifier {
  return this
}
