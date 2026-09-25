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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.theme.DarkBgAtmosphere
import com.example.ui.theme.DarkBgEnd
import com.example.ui.theme.DarkBgMid
import com.example.ui.theme.DarkBgStart
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassBorderSoft
import com.example.ui.theme.DarkGlassBubbleReceived
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBgEnd
import com.example.ui.theme.LightBgMid
import com.example.ui.theme.LightBgStart
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.LightGlassCard
import com.example.ui.theme.LightGlassSurface
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PinggoCyan
import com.example.ui.theme.PinggoEmeraldDark
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.ui.theme.PinggoMint
import com.example.ui.theme.PinggoMintLight
import com.example.ui.theme.PinggoMintUltraLight
import com.example.ui.theme.PinggoTeal

/**
 * Dynamic Liquid Glass Background with realistic luminous gradient orbs and glass reflections,
 * faithfully matching the primary visual reference image.
 */
@Composable
fun LiquidGlassBackground(
  modifier: Modifier = Modifier,
  isDark: Boolean = isSystemInDarkTheme(),
  content: @Composable BoxScope.() -> Unit
) {
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

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = if (isDark) {
            listOf(DarkBgStart, DarkBgMid, DarkBgEnd, DarkBgAtmosphere)
          } else {
            listOf(LightBgStart, LightBgMid, LightBgEnd)
          }
        )
      )
      .drawBehind {
        val width = size.width
        val height = size.height

        // Orb 1: Volumetric glowing emerald orb top-right
        drawCircle(
          brush = Brush.radialGradient(
            colors = if (isDark) {
              listOf(Color(0x5510B981), Color(0x2834D399), Color(0x00000000))
            } else {
              listOf(Color(0x4034D399), Color(0x18A7F3D0), Color(0x00FFFFFF))
            },
            center = Offset(width * 0.82f + orb1Offset, height * 0.18f + orb2Offset),
            radius = width * 0.7f
          )
        )

        // Orb 2: Teal-cyan liquid refraction sphere bottom-left
        drawCircle(
          brush = Brush.radialGradient(
            colors = if (isDark) {
              listOf(Color(0x4514B8A6), Color(0x1806B6D4), Color(0x00000000))
            } else {
              listOf(Color(0x3599F6E4), Color(0x15BAE6FD), Color(0x00FFFFFF))
            },
            center = Offset(width * 0.18f + orb2Offset, height * 0.75f + orb1Offset),
            radius = width * 0.75f
          )
        )

        // Orb 3: Center ambient emerald glow
        drawCircle(
          brush = Brush.radialGradient(
            colors = if (isDark) {
              listOf(Color(0x30059669), Color(0x00000000))
            } else {
              listOf(Color(0x20A7F3D0), Color(0x00000000))
            },
            center = Offset(width * 0.5f, height * 0.45f),
            radius = width * 0.55f
          )
        )

        // Subtle specular highlight line across top safe area
        drawLine(
          brush = Brush.horizontalGradient(
            colors = listOf(
              Color.Transparent,
              if (isDark) Color(0x2234D399) else Color(0x44FFFFFF),
              Color.Transparent
            )
          ),
          start = Offset(0f, 0f),
          end = Offset(width, 0f),
          strokeWidth = 1.5f
        )
      }
  ) {
    content()
  }
}

/**
 * Reusable GlassContainer with translucent frosted glass surface, specular border, and inner light reflection.
 */
@Composable
fun GlassContainer(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(28.dp),
  isDark: Boolean = isSystemInDarkTheme(),
  elevation: Dp = 10.dp,
  backgroundColor: Color? = null,
  borderColor: Color? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val surfaceColor = backgroundColor ?: if (isDark) DarkGlassSurface else LightGlassSurface
  val borderStrokeColor = borderColor ?: if (isDark) DarkGlassBorder else LightGlassBorder

  Box(
    modifier = modifier
      .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = if (isDark) Color(0x60000000) else Color(0x1A000000),
        spotColor = if (isDark) Color(0x4010B981) else Color(0x2010B981)
      )
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            surfaceColor,
            surfaceColor.copy(alpha = (surfaceColor.alpha * 0.88f).coerceIn(0f, 1f))
          )
        )
      )
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          colors = listOf(
            borderStrokeColor,
            borderStrokeColor.copy(alpha = 0.25f)
          )
        ),
        shape = shape
      )
  ) {
    // Top specular inner edge highlight for authentic liquid glass look
    Box(
      modifier = Modifier
        .matchParentSize()
        .drawBehind {
          drawLine(
            brush = Brush.horizontalGradient(
              colors = listOf(
                Color.Transparent,
                if (isDark) Color(0x33A7F3D0) else Color(0x66FFFFFF),
                Color.Transparent
              )
            ),
            start = Offset(24f, 1.5f),
            end = Offset(size.width - 24f, 1.5f),
            strokeWidth = 2f
          )
        }
    )
    content()
  }
}

/**
 * Reusable GlassCard for lists and interactive cards
 */
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(22.dp),
  isDark: Boolean = isSystemInDarkTheme(),
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val surfaceColor = if (isDark) DarkGlassCard else LightGlassCard
  val borderColor = if (isDark) DarkGlassBorderSoft else LightGlassBorder

  val clickableModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = ripple(bounded = true, color = PinggoEmeraldPrimary)
    ) { onClick() }
  } else Modifier

  Box(
    modifier = modifier
      .clip(shape)
      .background(surfaceColor)
      .border(
        BorderStroke(
          1.dp,
          Brush.verticalGradient(
            listOf(borderColor, borderColor.copy(alpha = 0.3f))
          )
        ),
        shape
      )
      .then(clickableModifier)
  ) {
    content()
  }
}

/**
 * Reusable GlassButton (Pill-shaped capsule with emerald gradient or translucent frosted glass)
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
    Brush.horizontalGradient(listOf(PinggoEmeraldDark, PinggoEmeraldPrimary, PinggoMint))
  } else {
    Brush.verticalGradient(
      listOf(
        if (isDark) Color(0x66143A31) else LightGlassSurface,
        if (isDark) Color(0x440C2922) else LightGlassCard
      )
    )
  }

  val contentColor = if (isPrimary) Color.White else if (isDark) DarkTextPrimary else LightTextPrimary
  val borderColor = if (isPrimary) Color(0x55FFFFFF) else if (isDark) DarkGlassBorder else LightGlassBorder

  Surface(
    modifier = modifier
      .heightIn(min = 54.dp)
      .clip(shape)
      .testTag(testTag)
      .clickable(
        enabled = enabled && !isLoading,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = Color.White)
      ) { onClick() },
    shape = shape,
    color = Color.Transparent,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = if (isPrimary) 8.dp else 2.dp
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
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
          }
          Text(
            text = text,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
          )
        }
      }
    }
  }
}

/**
 * Reusable GlassIconButton
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
  val iconTint = tint ?: if (isDark) DarkTextPrimary else LightTextPrimary
  val surfaceColor = if (isDark) Color(0x6613382F) else Color(0x99FFFFFF)
  val borderColor = if (isDark) DarkGlassBorderSoft else LightGlassBorder

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(surfaceColor)
      .border(BorderStroke(1.dp, borderColor), CircleShape)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = PinggoEmeraldPrimary)
      ) { onClick() }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
  }
}

/**
 * Reusable GlassSearchBar (Capsule pill shaped)
 */
@Composable
fun GlassSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Search users, chats...",
  isDark: Boolean = isSystemInDarkTheme(),
  onClear: () -> Unit = { onQueryChange("") }
) {
  val surfaceColor = if (isDark) Color(0x880E342B) else LightGlassSurface
  val borderColor = if (isDark) DarkGlassBorderSoft else LightGlassBorder
  val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
  val placeholderColor = if (isDark) DarkTextMuted else LightTextMuted

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp)
      .clip(RoundedCornerShape(24.dp))
      .background(surfaceColor)
      .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(24.dp))
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
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Box(modifier = Modifier.weight(1f)) {
        if (query.isEmpty()) {
          Text(
            text = placeholder,
            color = placeholderColor,
            fontSize = 14.sp
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = TextStyle(
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
          ),
          cursorBrush = SolidColor(PinggoEmeraldPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_input")
        )
      }
      if (query.isNotEmpty()) {
        IconButton(
          onClick = onClear,
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear search",
            tint = placeholderColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * Reusable GlassInput for forms & chat setup
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
  testTag: String = "glass_input"
) {
  val surfaceColor = if (isDark) Color(0x990E342B) else LightGlassSurface
  val borderColor = if (isDark) DarkGlassBorderSoft else LightGlassBorder
  val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
  val placeholderColor = if (isDark) DarkTextMuted else LightTextMuted

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(surfaceColor)
      .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(20.dp))
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
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
      }
      Box(modifier = Modifier.weight(1f)) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            color = placeholderColor,
            fontSize = 15.sp
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          singleLine = singleLine,
          maxLines = maxLines,
          textStyle = TextStyle(
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
          ),
          keyboardOptions = keyboardOptions,
          keyboardActions = keyboardActions,
          cursorBrush = SolidColor(PinggoEmeraldPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
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
 * Reusable GlassAvatar with glowing online status badge and story ring
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
  val shape = CircleShape
  val borderBrush = if (hasStatusUpdate) {
    Brush.sweepGradient(listOf(PinggoEmeraldPrimary, PinggoMint, PinggoCyan, PinggoEmeraldPrimary))
  } else {
    Brush.verticalGradient(
      listOf(
        if (isDark) DarkGlassBorder else LightGlassBorder,
        Color.Transparent
      )
    )
  }

  val clickableModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = ripple(bounded = false)
    ) { onClick() }
  } else Modifier

  Box(
    modifier = modifier
      .size(size)
      .then(clickableModifier)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(shape)
        .border(
          width = if (hasStatusUpdate) 2.5.dp else 1.dp,
          brush = borderBrush,
          shape = shape
        )
        .background(if (isDark) Color(0xFF13382F) else Color(0xFFE2E8F0)),
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
          .mapNotNull { it.firstOrNull()?.toString() }
          .take(2)
          .joinToString("")
          .uppercase()
          .ifEmpty { "P" }

        Text(
          text = initials,
          color = PinggoMint,
          fontSize = (size.value * 0.38f).sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Online indicator glowing green dot
    if (isOnline) {
      Box(
        modifier = Modifier
          .size(size * 0.3f)
          .align(Alignment.BottomEnd)
          .offset(x = 1.dp, y = 1.dp)
          .clip(CircleShape)
          .background(if (isDark) DarkBgStart else Color.White)
          .padding(2.dp)
          .clip(CircleShape)
          .background(OnlineGreen)
      )
    }
  }
}

/**
 * Reusable GlassChatBubble matching reference image
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
    Brush.horizontalGradient(
      listOf(PinggoEmeraldDark, PinggoEmeraldPrimary)
    )
  } else {
    Brush.verticalGradient(
      listOf(
        if (isDark) DarkGlassBubbleReceived else Color(0xF2FFFFFF),
        if (isDark) Color(0xCC0D2F27) else Color(0xE6F8FAFC)
      )
    )
  }

  val textColor = if (isSent) Color.White else if (isDark) DarkTextPrimary else LightTextPrimary
  val timeColor = if (isSent) Color(0xDDFFFFFF) else if (isDark) DarkTextMuted else LightTextMuted
  val borderColor = if (isSent) Color(0x40FFFFFF) else if (isDark) DarkGlassBorderSoft else LightGlassBorder

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
          .background(if (isSent) Color(0x33000000) else if (isDark) Color(0x44000000) else Color(0x15000000))
          .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
      ) {
        Column {
          Text(
            text = replySender ?: "Pinggo User",
            color = if (isSent) PinggoMintLight else PinggoMint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = replySnippet,
            color = if (isSent) Color(0xEEFFFFFF) else if (isDark) DarkTextSecondary else LightTextSecondary,
            fontSize = 12.sp,
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
      fontSize = 15.sp,
      lineHeight = 21.sp
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      modifier = Modifier.align(Alignment.End),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = timestamp,
        color = timeColor,
        fontSize = 11.sp
      )
      if (isSent) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (read) "✓✓" else if (delivered) "✓✓" else "✓",
          color = if (read) Color(0xFF67E8F9) else Color(0xCCFFFFFF),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

/**
 * Reusable Floating GlassBottomBar matching reference image
 */
@Composable
fun GlassBottomBar(
  modifier: Modifier = Modifier,
  isDark: Boolean = isSystemInDarkTheme(),
  content: @Composable RowScope.() -> Unit
) {
  val surfaceColor = if (isDark) Color(0xD908251E) else Color(0xF2FFFFFF)
  val borderColor = if (isDark) DarkGlassBorder else LightGlassBorder

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 10.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(36.dp),
      color = surfaceColor,
      border = BorderStroke(1.dp, borderColor),
      shadowElevation = 14.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        content = content
      )
    }
  }
}

/**
 * Reusable GlassToast notification
 */
@Composable
fun GlassToast(
  message: String,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  isDark: Boolean = isSystemInDarkTheme()
) {
  val surfaceColor = if (isError) Color(0xEE7F1D1D) else if (isDark) Color(0xEE064E3B) else Color(0xF0ECFDF5)
  val borderColor = if (isError) Color(0xFFEF4444) else PinggoEmeraldPrimary
  val textColor = if (isError) Color.White else if (isDark) Color.White else LightTextPrimary

  Surface(
    shape = RoundedCornerShape(26.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 10.dp,
    modifier = modifier.padding(16.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isError) "⚠️ " else "✨ ",
        fontSize = 16.sp
      )
      Text(
        text = message,
        color = textColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
