package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

/**
 * Dynamic Liquid Glass Background with realistic luminous gradient orbs and glass reflections.
 * ENSURED: Correct contrast background for text visibility.
 */
@Composable
fun LiquidGlassBackground(
  modifier: Modifier = Modifier,
  isDark: Boolean = isSystemInDarkTheme(),
  content: @Composable BoxScope.() -> Unit
) {
  val design = LocalGlassDesign.current
  val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_orbs")
  val orb1Offset by infiniteTransition.animateFloat(
    initialValue = -30f,
    targetValue = 40f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb1"
  )
  val orb2Offset by infiniteTransition.animateFloat(
    initialValue = 20f,
    targetValue = -50f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb2"
  )

  val bgColor = if (isDark) DarkBackground else PinggoOffWhite
  val orbColor1 = when (design) {
    GlassDesign.PINK -> Color(0x40FF69B4)
    GlassDesign.CRYSTAL -> Color(0x204285F4)
    else -> if (isDark) Color(0x30FF69B4) else Color(0x20FF69B4)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .drawBehind {
        val width = size.width
        val height = size.height

        // Orb 1: Volumetric glowing orb top-right
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(orbColor1, orbColor1.copy(alpha = 0.1f), Color.Transparent),
            center = Offset(width * 0.82f + orb1Offset, height * 0.18f + orb2Offset),
            radius = width * 0.7f
          )
        )

        // Orb 2: Refraction sphere bottom-left
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(orbColor1.copy(alpha = 0.6f), Color.Transparent),
            center = Offset(width * 0.18f + orb2Offset, height * 0.75f + orb1Offset),
            radius = width * 0.75f
          )
        )
      }
  ) {
    content()
  }
}

/**
 * Reusable GlassContainer with translucent frosted glass surface.
 * FIX: Improved surface opacity for text readability.
 */
@Composable
fun GlassContainer(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(28.dp),
  isDark: Boolean = isSystemInDarkTheme(),
  elevation: Dp = 10.dp,
  content: @Composable BoxScope.() -> Unit
) {
  val design = LocalGlassDesign.current
  val surfaceColor = when (design) {
    GlassDesign.PINK -> PinggoPinkLight.copy(alpha = 0.25f)
    GlassDesign.CRYSTAL -> if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.15f)
    else -> if (isDark) Color(0xFF1E1E1E).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.75f)
  }
  val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else PinggoPinkPrimary.copy(alpha = 0.25f)

  Box(
    modifier = modifier
      .shadow(elevation = elevation, shape = shape)
      .clip(shape)
      .background(surfaceColor)
      .border(1.dp, borderColor, shape)
  ) {
    content()
  }
}

/**
 * Reusable GlassCard for lists and interactive cards.
 * FIX: Higher opacity and theme-aware colors for maximum text contrast.
 */
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(22.dp),
  isDark: Boolean = isSystemInDarkTheme(),
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val design = LocalGlassDesign.current
  val surfaceColor = when (design) {
    GlassDesign.PINK -> PinggoPinkLight.copy(alpha = 0.2f)
    GlassDesign.CRYSTAL -> if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.15f)
    else -> if (isDark) DarkSurface.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
  }
  
  val clickableModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = ripple(bounded = true, color = PinggoPinkPrimary)
    ) { onClick() }
  } else Modifier

  Box(
    modifier = modifier
      .clip(shape)
      .background(surfaceColor)
      .border(0.5.dp, if(isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f), shape)
      .then(clickableModifier)
  ) {
    content()
  }
}

/**
 * Reusable GlassButton.
 * FIX: High contrast text colors for primary and secondary variants.
 */
@Composable
fun GlassButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isPrimary: Boolean = true,
  icon: ImageVector? = null,
  isLoading: Boolean = false,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(28.dp),
  isDark: Boolean = isSystemInDarkTheme(),
  testTag: String = "glass_button"
) {
  val backgroundBrush = if (isPrimary) {
    Brush.horizontalGradient(listOf(PinggoPinkPrimary, PinggoPinkDeep))
  } else {
    Brush.verticalGradient(
      listOf(
        if (isDark) Color(0xFF3A3A3C) else Color.White,
        if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
      )
    )
  }

  // Primary: White text always works on pink. Secondary: uses semantic primary text color.
  val contentColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onBackground
  val borderColor = if (isPrimary) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

  Surface(
    modifier = modifier
      .heightIn(min = 54.dp)
      .clip(shape)
      .testTag(testTag)
      .clickable(
        enabled = enabled && !isLoading,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = if(isPrimary) Color.White else PinggoPinkPrimary)
      ) { onClick() },
    shape = shape,
    color = Color.Transparent,
    border = BorderStroke(1.5.dp, borderColor),
    shadowElevation = if (isPrimary) 6.dp else 0.dp
  ) {
    Box(
      modifier = Modifier
        .background(backgroundBrush)
        .padding(horizontal = 24.dp, vertical = 14.dp),
      contentAlignment = Alignment.Center
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(22.dp),
          color = contentColor,
          strokeWidth = 2.5.dp
        )
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          if (icon != null) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = contentColor,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
          }
          Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

/**
 * Reusable GlassIconButton.
 */
@Composable
fun GlassIconButton(
  icon: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isDark: Boolean = isSystemInDarkTheme(),
  tint: Color? = null,
  size: Dp = 44.dp,
  testTag: String = "glass_icon_button"
) {
  val iconTint = tint ?: MaterialTheme.colorScheme.onSurface
  val surfaceColor = if(isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(surfaceColor)
      .border(BorderStroke(1.dp, if(isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f)), CircleShape)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, color = PinggoPinkPrimary)
      ) { onClick() }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconTint,
      modifier = Modifier.size(24.dp)
    )
  }
}

/**
 * Reusable GlassSearchBar.
 * FIX: High contrast text and search icon.
 */
@Composable
fun GlassSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Search...",
  isDark: Boolean = isSystemInDarkTheme(),
  onClear: () -> Unit = { onQueryChange("") }
) {
  val surfaceColor = if(isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
  val textColor = MaterialTheme.colorScheme.onBackground
  val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .clip(RoundedCornerShape(26.dp))
      .background(surfaceColor)
      .border(BorderStroke(1.dp, if(isDark) Color.White.copy(alpha = 0.1f) else Color.Transparent), RoundedCornerShape(26.dp))
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Search",
        tint = placeholderColor,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Box(modifier = Modifier.weight(1f)) {
        if (query.isEmpty()) {
          Text(text = placeholder, color = placeholderColor, style = MaterialTheme.typography.bodyLarge)
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
          cursorBrush = SolidColor(PinggoPinkPrimary),
          modifier = Modifier.fillMaxWidth().testTag("search_input")
        )
      }
      if (query.isNotEmpty()) {
        IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Clear, null, tint = placeholderColor, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

/**
 * Reusable GlassInput.
 * FIX: Higher contrast for input text and placeholders.
 */
@Composable
fun GlassInput(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  leadingIcon: ImageVector? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  isDark: Boolean = isSystemInDarkTheme(),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  maxLines: Int = 1,
  visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
  textAlign: TextAlign = TextAlign.Start,
  testTag: String = "glass_input"
) {
  val surfaceColor = if(isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)
  val textColor = MaterialTheme.colorScheme.onBackground
  val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(surfaceColor)
      .border(1.dp, if(isDark) Color.White.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
      .padding(horizontal = 16.dp, vertical = 14.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = placeholderColor,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
      }
      Box(modifier = Modifier.weight(1f)) {
        if (value.isEmpty()) {
          Text(
            text = placeholder, 
            color = placeholderColor, 
            style = MaterialTheme.typography.bodyLarge,
            textAlign = textAlign, 
            modifier = Modifier.fillMaxWidth()
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          singleLine = singleLine,
          maxLines = maxLines,
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor, textAlign = textAlign),
          keyboardOptions = keyboardOptions,
          keyboardActions = keyboardActions,
          visualTransformation = visualTransformation,
          cursorBrush = SolidColor(PinggoPinkPrimary),
          modifier = Modifier.fillMaxWidth().testTag(testTag)
        )
      }
      if (trailingIcon != null) {
        Spacer(modifier = Modifier.width(8.dp))
        trailingIcon()
      }
    }
  }
}

/**
 * Reusable GlassAvatar.
 */
@Composable
fun GlassAvatar(
  photoUrl: String?,
  name: String,
  modifier: Modifier = Modifier,
  size: Dp = 52.dp,
  isOnline: Boolean = false,
  hasStatusUpdate: Boolean = false,
  isDark: Boolean = isSystemInDarkTheme(),
  onClick: (() -> Unit)? = null
) {
  val borderBrush = if (hasStatusUpdate) {
    Brush.sweepGradient(listOf(PinggoPinkPrimary, PinggoPinkLight, PinggoPinkPrimary))
  } else {
    Brush.verticalGradient(listOf(Color.LightGray.copy(alpha = 0.4f), Color.Transparent))
  }

  val clickableModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = ripple(bounded = false)
    ) { onClick() }
  } else Modifier

  Box(modifier = modifier.size(size).then(clickableModifier)) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .border(width = if (hasStatusUpdate) 2.5.dp else 1.dp, brush = borderBrush, shape = CircleShape)
        .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE2E8F0)),
      contentAlignment = Alignment.Center
    ) {
      if (!photoUrl.isNullOrEmpty()) {
        AsyncImage(
          model = photoUrl,
          contentDescription = "$name's avatar",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      } else {
        val initials = name.trim().split(" ")
          .filter { it.isNotEmpty() }
          .mapNotNull { it.firstOrNull()?.toString() }
          .take(2)
          .joinToString("")
          .uppercase()
          .ifEmpty { "P" }

        Text(
          text = initials,
          color = PinggoPinkPrimary,
          fontSize = (size.value * 0.38f).sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    if (isOnline) {
      Box(
        modifier = Modifier
          .size(size * 0.3f)
          .align(Alignment.BottomEnd)
          .offset(x = 1.dp, y = 1.dp)
          .clip(CircleShape)
          .background(if (isDark) DarkBackground else Color.White)
          .padding(2.dp)
          .clip(CircleShape)
          .background(OnlinePink)
      )
    }
  }
}

/**
 * Reusable GlassChatBubble.
 * FIX: Robust color logic for text in bubbles.
 */
@Composable
fun GlassChatBubble(
  text: String,
  timestamp: String,
  isSent: Boolean,
  modifier: Modifier = Modifier,
  delivered: Boolean = true,
  read: Boolean = false,
  isDark: Boolean = isSystemInDarkTheme(),
  replySnippet: String? = null,
  replySender: String? = null
) {
  val bubbleShape = if (isSent) {
    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
  } else {
    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)
  }

  val backgroundBrush = if (isSent) {
    Brush.horizontalGradient(listOf(PinggoPinkPrimary, PinggoPinkDeep))
  } else {
    Brush.verticalGradient(
      listOf(
        if (isDark) Color(0xFF2C2C2E) else Color.White,
        if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
      )
    )
  }

  // Sent messages always white text on pink. Received messages adaptive.
  val textColor = if (isSent) Color.White else MaterialTheme.colorScheme.onBackground
  val timeColor = if (isSent) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
  val borderColor = if (isSent) Color.Transparent else if(isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f)

  Column(
    modifier = modifier
      .shadow(elevation = 2.dp, shape = bubbleShape)
      .clip(bubbleShape)
      .background(backgroundBrush)
      .border(BorderStroke(1.dp, borderColor), bubbleShape)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    if (!replySnippet.isNullOrEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(if(isSent) Color.Black.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f))
          .padding(start = 8.dp, top = 6.dp, bottom = 6.dp, end = 8.dp)
      ) {
        Column {
          Text(
            text = replySender ?: "Pinggo User",
            color = if (isSent) PinggoPinkLight else PinggoPinkPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = replySnippet,
            color = textColor.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
    }

    Text(
      text = text,
      color = textColor,
      style = MaterialTheme.typography.bodyLarge,
      lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      modifier = Modifier.align(Alignment.End),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = timestamp, color = timeColor, style = MaterialTheme.typography.labelSmall)
      if (isSent) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (read) "✓✓" else if (delivered) "✓✓" else "✓",
          color = if (read) Color(0xFF67E8F9) else Color.White.copy(alpha = 0.8f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

/**
 * Reusable Floating GlassBottomBar.
 */
@Composable
fun GlassBottomBar(
  modifier: Modifier = Modifier,
  isDark: Boolean = isSystemInDarkTheme(),
  content: @Composable RowScope.() -> Unit
) {
  val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 10.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(36.dp),
      color = surfaceColor,
      border = BorderStroke(1.dp, if(isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f)),
      shadowElevation = 15.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        content = content
      )
    }
  }
}

/**
 * Reusable GlassToast.
 */
@Composable
fun GlassToast(
  message: String,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  isDark: Boolean = isSystemInDarkTheme()
) {
  val surfaceColor = if (isError) Color(0xFFB91C1C) else if (isDark) Color(0xFF2C2C2E) else Color(0xFFF8F8FA)
  val borderColor = if (isError) Color.Red else PinggoPinkPrimary
  val textColor = if (isError || isDark) Color.White else LightPrimaryText

  Surface(
    shape = RoundedCornerShape(26.dp),
    color = surfaceColor,
    border = BorderStroke(2.dp, borderColor),
    shadowElevation = 12.dp,
    modifier = modifier.padding(16.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = if (isError) "⚠️ " else "✨ ", fontSize = 18.sp)
      Text(
        text = message,
        color = textColor,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
