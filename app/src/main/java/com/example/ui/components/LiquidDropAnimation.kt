package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PinggoPinkLight
import com.example.ui.theme.PinggoPinkPrimary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

/**
 * Global composition local to track the screen coordinates of the most recent user tap.
 * Allows screen-level transitions to emerge directly from the tapped element's exact location.
 */
val LocalLastTapPosition = staticCompositionLocalOf { mutableStateOf<Offset?>(null) }

/**
 * Checks whether system reduced-motion or animator duration scale is disabled
 * for Android accessibility compliance.
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
 * Represents an active liquid droplet expanding on tap
 */
private class ActiveDroplet(
  val origin: Offset,
  val animatable: Animatable<Float, *> = Animatable(0f)
)

/**
 * Modifier that attaches subtle, premium liquid-drop touch feedback to any clickable component.
 * Expands a translucent crystal water drop with soft white highlights and gentle pink meniscus,
 * then smoothly dissolves within 180–260 ms without interfering with click events or gestures.
 */
@Composable
fun Modifier.liquidDrop(
  interactionSource: MutableInteractionSource,
  enabled: Boolean = true,
  isPinkTint: Boolean = true,
  maxRadius: Dp = 36.dp,
  durationMillis: Int = 230
): Modifier {
  if (!enabled) return this
  val isReducedMotion = rememberReducedMotion()
  if (isReducedMotion) return this

  val coroutineScope = rememberCoroutineScope()
  val activeDroplets = remember { mutableStateListOf<ActiveDroplet>() }
  val density = LocalDensity.current
  val maxRadiusPx = with(density) { maxRadius.toPx() }
  val lastTapState = LocalLastTapPosition.current
  var itemOffsetInRoot by remember { mutableStateOf(Offset.Zero) }

  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collectLatest { interaction ->
      if (interaction is PressInteraction.Press) {
        val globalPos = itemOffsetInRoot + interaction.pressPosition
        lastTapState.value = globalPos
        val droplet = ActiveDroplet(interaction.pressPosition)
        activeDroplets.add(droplet)
        coroutineScope.launch {
          droplet.animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = durationMillis,
              easing = FastOutSlowInEasing
            )
          )
          activeDroplets.remove(droplet)
        }
      }
    }
  }

  return this
    .onGloballyPositioned { coordinates ->
      if (coordinates.isAttached) {
        itemOffsetInRoot = coordinates.positionInRoot()
      }
    }
    .drawWithContent {
      drawContent()

      for (droplet in activeDroplets) {
        val progress = droplet.animatable.value
        val radius = maxRadiusPx * (0.25f + 0.75f * progress)
        val alpha = (1f - progress).coerceIn(0f, 1f)

        if (alpha > 0.01f) {
          val center = droplet.origin

          // 1. Crystal liquid inner refraction disc
          val innerBrush = Brush.radialGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.45f * alpha),
              if (isPinkTint) PinggoPinkLight.copy(alpha = 0.25f * alpha) else Color.White.copy(alpha = 0.20f * alpha),
              Color.Transparent
            ),
            center = center,
            radius = radius
          )
          drawCircle(
            brush = innerBrush,
            radius = radius,
            center = center
          )

          // 2. Liquid meniscus border (micro-thin refraction ring)
          val ringColor = if (isPinkTint) {
            PinggoPinkPrimary.copy(alpha = 0.42f * alpha)
          } else {
            Color.White.copy(alpha = 0.60f * alpha)
          }
          drawCircle(
            color = ringColor,
            radius = radius * 0.96f,
            center = center,
            style = Stroke(width = with(density) { 1.2.dp.toPx() })
          )

          // 3. Specular water drop highlight shine (top-left glint)
          val shineOffset = Offset(
            center.x - radius * 0.28f,
            center.y - radius * 0.28f
          )
          drawCircle(
            color = Color.White.copy(alpha = 0.60f * alpha),
            radius = radius * 0.22f,
            center = shineOffset
          )
        }
      }
    }
}

/**
 * Convenience extension combining liquid drop visual effect with click handling and ripple.
 */
@Composable
fun Modifier.liquidDropClickable(
  enabled: Boolean = true,
  isPinkTint: Boolean = true,
  maxRadius: Dp = 36.dp,
  durationMillis: Int = 230,
  boundedRipple: Boolean = true,
  rippleColor: Color = PinggoPinkPrimary,
  onClick: () -> Unit
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  return this
    .liquidDrop(
      interactionSource = interactionSource,
      enabled = enabled,
      isPinkTint = isPinkTint,
      maxRadius = maxRadius,
      durationMillis = durationMillis
    )
    .clickable(
      enabled = enabled,
      interactionSource = interactionSource,
      indication = ripple(bounded = boundedRipple, color = rippleColor),
      onClick = onClick
    )
}

/**
 * Screen-level Liquid Droplet Reveal Container.
 * When opening new destinations or navigating back, shows a brief crystal-liquid
 * drop transition that expands seamlessly from the tap origin across the pure white screen.
 */
@Composable
fun <T> LiquidDropScreenTransition(
  targetState: T,
  modifier: Modifier = Modifier,
  isBackNavigation: Boolean = false,
  origin: Offset? = null,
  durationMillis: Int = 340,
  content: @Composable (T) -> Unit
) {
  val isReducedMotion = rememberReducedMotion()
  val progress = remember { Animatable(0f) }
  var displayedState by remember { mutableStateOf(targetState) }
  val coroutineScope = rememberCoroutineScope()
  val lastTapState = LocalLastTapPosition.current
  var transitionOrigin by remember { mutableStateOf<Offset?>(null) }

  LaunchedEffect(targetState) {
    if (targetState != displayedState) {
      if (isReducedMotion) {
        displayedState = targetState
      } else {
        transitionOrigin = origin ?: lastTapState.value
        progress.snapTo(0f)
        displayedState = targetState
        coroutineScope.launch {
          progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = durationMillis,
              easing = LinearOutSlowInEasing
            )
          )
        }
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // Underlying screen content
    content(displayedState)

    // Crystal liquid drop reveal overlay (runs only during transitions, 0 GPU when idle)
    if (!isReducedMotion && progress.value in 0.001f..0.999f) {
      val p = progress.value
      val waveAlpha = if (isBackNavigation) {
        ((1f - p) * 0.40f).coerceIn(0f, 0.40f)
      } else {
        ((1f - p) * 0.45f).coerceIn(0f, 0.45f)
      }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .drawWithContent {
            val width = size.width
            val height = size.height
            val defaultOrigin = Offset(width * 0.5f, height * 0.4f)
            val center = transitionOrigin?.let {
              Offset(it.x.coerceIn(0f, width), it.y.coerceIn(0f, height))
            } ?: defaultOrigin

            val maxDimension = hypot(
              max(center.x, width - center.x),
              max(center.y, height - center.y)
            ) * 1.1f

            val currentRadius = if (isBackNavigation) {
              maxDimension * (1f - p * 0.85f).coerceAtLeast(0f)
            } else {
              maxDimension * (0.05f + 0.95f * p)
            }

            // 1. Crystal liquid refraction disc expanding from tap
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(
                  Color.White.copy(alpha = waveAlpha * 0.40f),
                  Color.White.copy(alpha = waveAlpha * 0.25f),
                  PinggoPinkLight.copy(alpha = waveAlpha * 0.20f),
                  Color.White.copy(alpha = waveAlpha * 0.50f),
                  Color.Transparent
                ),
                center = center,
                radius = currentRadius
              ),
              center = center,
              radius = currentRadius
            )

            // 2. Liquid meniscus outer refraction ring (delicate, glass-like edge)
            drawCircle(
              color = PinggoPinkPrimary.copy(alpha = waveAlpha * 0.35f),
              center = center,
              radius = currentRadius * 0.98f,
              style = Stroke(width = 2.dp.toPx())
            )

            // 3. Inner specular water ring
            drawCircle(
              color = Color.White.copy(alpha = waveAlpha * 0.55f),
              center = center,
              radius = currentRadius * 0.94f,
              style = Stroke(width = 1.dp.toPx())
            )
          }
      )
    }
  }
}

/**
 * Liquid Drop Modal Reveal Modifier for Dialogs and Bottom Sheets.
 * Adds a soft crystal droplet ripple around dialog surfaces on appearance.
 */
@Composable
fun Modifier.liquidDropModalReveal(
  isPinkTint: Boolean = true,
  durationMillis: Int = 280
): Modifier {
  val isReducedMotion = rememberReducedMotion()
  if (isReducedMotion) return this

  val anim = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    anim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
    )
  }

  return this.drawWithContent {
    drawContent()
    val progress = anim.value
    if (progress < 0.99f) {
      val alpha = (1f - progress).coerceIn(0f, 1f)
      val radius = max(size.width, size.height) * (0.55f + 0.45f * progress)

      // Inner soft crystal glow
      drawCircle(
        color = Color.White.copy(alpha = 0.35f * alpha),
        center = Offset(size.width / 2f, size.height / 2f),
        radius = radius * 0.96f,
        style = Stroke(width = 1.dp.toPx())
      )

      // Outer liquid glass meniscus ring
      drawCircle(
        color = if (isPinkTint) PinggoPinkPrimary.copy(alpha = 0.28f * alpha) else Color.White.copy(alpha = 0.40f * alpha),
        center = Offset(size.width / 2f, size.height / 2f),
        radius = radius,
        style = Stroke(width = 1.8.dp.toPx())
      )
    }
  }
}
