package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.Canvas
import coil.compose.AsyncImage
import com.example.ui.theme.*

/**
 * Dynamic Liquid Glass Background - RECREATED OCEAN SUNSET GRADIENT.
 * Matches Reference Image 2: Lavender (top) -> Peach/Pink (middle) -> Muted Blue (bottom).
 */
@Composable
fun LiquidGlassBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  val backgroundBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFFDCDDF1), // Soft Lavender Top
      Color(0xFFF7E2D5), // Soft Peach Middle-Top
      Color(0xFFF9D8DA), // Warm Pink Middle
      Color(0xFFE2EAF1), // Light Blue Middle-Bottom
      Color(0xFFC7D9E8)  // Muted Blue Bottom
    )
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(backgroundBrush)
  ) {
    // Subtle animated refraction orbs to simulate "Liquid" feel
    val infiniteTransition = rememberInfiniteTransition(label = "refraction")
    val orbOffset by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 100f,
      animationSpec = infiniteRepeatable(
        animation = tween(10000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "orb"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
          center = Offset(size.width * 0.8f, size.height * 0.2f + orbOffset),
          radius = size.width * 0.5f
        )
      )
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent),
          center = Offset(size.width * 0.2f, size.height * 0.8f - orbOffset),
          radius = size.width * 0.4f
        )
      )
    }

    content()
  }
}

/**
 * Reusable GlassContainer with crystal-clear frosted glass surface.
 * PREMIUM: Highly transparent white with delicate edge refraction.
 */
@Composable
fun GlassContainer(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(28.dp),
  elevation: Dp = 4.dp,
  animateModalReveal: Boolean = true,
  content: @Composable BoxScope.() -> Unit
) {
  val surfaceColor = Color.White.copy(alpha = 0.45f) 
  val borderColor = Color.White.copy(alpha = 0.7f)
  val modalModifier = if (animateModalReveal) Modifier.liquidDropModalReveal(isPinkTint = true) else Modifier

  Box(
    modifier = modifier
      .shadow(
        elevation = elevation, 
        shape = shape, 
        ambientColor = Color.Black.copy(alpha = 0.05f), 
        spotColor = Color.Black.copy(alpha = 0.05f)
      )
      .clip(shape)
      .background(surfaceColor)
      .then(modalModifier)
      .drawBehind {
        // Soft inner white highlight for crystal look (Top-Left Glint)
        drawRect(
          brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.3f, size.height * 0.3f)
          )
        )
      }
      .border(0.5.dp, borderColor, shape)
  ) {
    content()
  }
}

/**
 * Reusable GlassCard for lists and interactive cards.
 * PREMIUM: Ultra-thin borders and realistic highlights.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(22.dp),
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val surfaceColor = Color.White.copy(alpha = 0.4f)
  val borderColor = Color.White.copy(alpha = 0.7f)
  
  val interactionSource = remember { MutableInteractionSource() }
  val gestureModifier = if (onClick != null || onLongClick != null) {
    Modifier
      .liquidDrop(interactionSource = interactionSource, isPinkTint = true, maxRadius = 48.dp)
      .combinedClickable(
        interactionSource = interactionSource,
        indication = ripple(bounded = true, color = PinggoPinkPrimary),
        onClick = { onClick?.invoke() },
        onLongClick = { onLongClick?.invoke() }
      )
  } else Modifier

  Box(
    modifier = modifier
      .clip(shape)
      .background(surfaceColor)
      .drawBehind {
        // Realistic crystal glass highlight glint
        drawRect(
          brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent, Color.White.copy(alpha = 0.1f)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
          )
        )
      }
      .border(0.5.dp, borderColor, shape)
      .then(gestureModifier)
  ) {
    content()
  }
}

/**
 * Reusable GlassButton.
 * REDESIGN: Vibrant branding for primary, Crystal Glass for secondary.
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
  testTag: String = "glass_button"
) {
  val backgroundBrush = if (isPrimary) {
    Brush.horizontalGradient(listOf(PinggoPinkPrimary, PinggoPinkDeep))
  } else {
    Brush.verticalGradient(
      listOf(
        Color.White.copy(alpha = 0.6f),
        Color.White.copy(alpha = 0.3f)
      )
    )
  }

  val contentColor = if (isPrimary) Color.White else LightPrimaryText
  val borderColor = if (isPrimary) Color.Transparent else Color.White.copy(alpha = 0.8f)

  val interactionSource = remember { MutableInteractionSource() }

  Surface(
    modifier = modifier
      .heightIn(min = 54.dp)
      .clip(shape)
      .testTag(testTag)
      .liquidDrop(interactionSource = interactionSource, isPinkTint = !isPrimary, maxRadius = 52.dp)
      .clickable(
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        indication = ripple(bounded = true, color = if(isPrimary) Color.White else PinggoPinkPrimary)
      ) { onClick() },
    shape = shape,
    color = Color.Transparent,
    border = BorderStroke(0.5.dp, borderColor),
    shadowElevation = if (isPrimary) 6.dp else 2.dp
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
  tint: Color? = null,
  size: Dp = 44.dp,
  testTag: String = "glass_icon_button"
) {
  val iconTint = tint ?: LightPrimaryText
  val surfaceColor = Color.White.copy(alpha = 0.5f)
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(surfaceColor)
      .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.8f)), CircleShape)
      .liquidDrop(interactionSource = interactionSource, isPinkTint = true, maxRadius = (size / 2) + 6.dp)
      .clickable(
        interactionSource = interactionSource,
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
 * PREMIUM: Crystal Glass look on blurred background.
 */
@Composable
fun GlassSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Search...",
  onClear: () -> Unit = { onQueryChange("") }
) {
  val surfaceColor = Color.White.copy(alpha = 0.45f)
  val textColor = LightPrimaryText
  val placeholderColor = LightSecondaryText.copy(alpha = 0.6f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .clip(RoundedCornerShape(26.dp))
      .background(surfaceColor)
      .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.8f)), RoundedCornerShape(26.dp))
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
 */
@Composable
fun GlassInput(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  leadingIcon: ImageVector? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  maxLines: Int = 1,
  visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
  textAlign: TextAlign = TextAlign.Start,
  testTag: String = "glass_input"
) {
  val surfaceColor = Color.White.copy(alpha = 0.4f)
  val textColor = LightPrimaryText
  val placeholderColor = LightSecondaryText.copy(alpha = 0.6f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(surfaceColor)
      .border(0.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
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
  onClick: (() -> Unit)? = null
) {
  val borderBrush = if (hasStatusUpdate) {
    Brush.sweepGradient(listOf(PinggoPinkPrimary, PinggoPinkLight, PinggoPinkPrimary))
  } else {
    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.Transparent))
  }

  val avatarInteraction = remember { MutableInteractionSource() }
  val clickableModifier = if (onClick != null) {
    Modifier
      .liquidDrop(interactionSource = avatarInteraction, isPinkTint = true, maxRadius = (size / 2) + 6.dp)
      .clickable(
        interactionSource = avatarInteraction,
        indication = ripple(bounded = false, color = PinggoPinkPrimary)
      ) { onClick() }
  } else Modifier

  Box(modifier = modifier.size(size).then(clickableModifier)) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .border(width = if (hasStatusUpdate) 2.5.dp else 1.dp, brush = borderBrush, shape = CircleShape)
        .background(Color.White.copy(alpha = 0.3f)),
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
          .background(Color.White)
          .padding(2.dp)
          .clip(CircleShape)
          .background(OnlinePink)
      )
    }
  }
}

/**
 * Reusable GlassChatBubble.
 */
@Composable
fun GlassChatBubble(
  text: String,
  timestamp: String,
  isSent: Boolean,
  modifier: Modifier = Modifier,
  delivered: Boolean = true,
  read: Boolean = false,
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
        Color.White.copy(alpha = 0.55f),
        Color.White.copy(alpha = 0.35f)
      )
    )
  }

  val textColor = if (isSent) Color.White else LightPrimaryText
  val timeColor = if (isSent) Color.White.copy(alpha = 0.75f) else LightSecondaryText
  val borderColor = if (isSent) Color.Transparent else Color.White.copy(alpha = 0.6f)

  Column(
    modifier = modifier
      .shadow(elevation = 2.dp, shape = bubbleShape)
      .clip(bubbleShape)
      .background(backgroundBrush)
      .border(BorderStroke(0.5.dp, borderColor), bubbleShape)
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
  content: @Composable RowScope.() -> Unit
) {
  val surfaceColor = Color.White.copy(alpha = 0.65f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 10.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(36.dp),
      color = surfaceColor,
      border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.8f)),
      shadowElevation = 8.dp,
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
 * Reusable LiquidBlurDropdownMenu.
 * REDESIGNED: Premium iOS-style Liquid Blur floating menu.
 * High-translucency frosted surface with subtle pink tint and strong rounded corners.
 */
@Composable
fun LiquidBlurDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 8.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val transition = updateTransition(expanded, label = "LiquidBlurMenuTransition")
  
  val scale by transition.animateFloat(
    transitionSpec = {
      if (false isTransitioningTo true) {
        spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)
      } else {
        tween(durationMillis = 200, easing = FastOutSlowInEasing)
      }
    },
    label = "scale"
  ) { if (it) 1f else 0.85f }

  val alphaTransition by transition.animateFloat(
    transitionSpec = {
      if (false isTransitioningTo true) {
        tween(durationMillis = 250)
      } else {
        tween(durationMillis = 150)
      }
    },
    label = "alpha"
  ) { if (it) 1f else 0f }

  if (expanded || transition.currentState) {
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismissRequest,
      modifier = modifier
        .widthIn(min = 230.dp)
        .padding(horizontal = 12.dp)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
          this.alpha = alphaTransition
          clip = true
          shape = RoundedCornerShape(32.dp)
        },
      offset = offset,
      containerColor = Color.Transparent,
      scrollState = rememberScrollState(),
      properties = PopupProperties(focusable = true)
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.88f), // High alpha for frosted look
        shadowElevation = 20.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
      ) {
        // Subtle Pink Blur Layer (Tint)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(PinggoPinkPrimary.copy(alpha = 0.04f))
            .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
          Column {
            content()
          }
        }
      }
    }
  }
}

/**
 * Custom Menu Item for Liquid Blur Menu with subtle pink pressed state.
 */
@Composable
fun LiquidBlurDropdownMenuItem(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 2.dp)
      .clip(RoundedCornerShape(20.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(color = PinggoPinkPrimary),
        onClick = onClick
      ),
    color = Color.Transparent
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color(0xFF1C1C1E), // Dark iOS style text color
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(14.dp))
      Text(
        text = text,
        color = Color(0xFF1C1C1E),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

/**
 * Reusable GlassDropdownMenu.
 * @deprecated Use LiquidBlurDropdownMenu instead for iOS style.
 */
@Composable
fun GlassDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 4.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  LiquidBlurDropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    offset = offset,
    content = content
  )
}

/**
 * Reusable GlassDialog.
 */
@Composable
fun GlassDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Dialog(onDismissRequest = onDismissRequest) {
    GlassContainer(
      modifier = modifier
        .fillMaxWidth(0.9f)
        .wrapContentHeight(),
      shape = RoundedCornerShape(32.dp),
      elevation = 16.dp,
      animateModalReveal = true,
      content = content
    )
  }
}

/**
 * Premium Glass Action Toolbar.
 */
@Composable
fun GlassActionToolbar(
  modifier: Modifier = Modifier,
  onClose: () -> Unit,
  title: String = "1 selected",
  actions: @Composable RowScope.() -> Unit
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 8.dp),
    shape = RoundedCornerShape(32.dp),
    color = Color.White.copy(alpha = 0.7f),
    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.9f)),
    shadowElevation = 10.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, "Cancel Selection", tint = LightPrimaryText)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = LightPrimaryText
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        content = actions
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
  isError: Boolean = false
) {
  val surfaceColor = if (isError) Color(0xFFB91C1C).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.75f)
  val borderColor = if (isError) Color.Red else PinggoPinkPrimary
  val textColor = if (isError) Color.White else LightPrimaryText

  Surface(
    shape = RoundedCornerShape(26.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
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
