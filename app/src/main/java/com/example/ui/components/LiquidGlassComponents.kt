package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.ui.theme.*

/**
 * Clean Modern Solid Surface Background.
 * Replaces glassmorphism with a clean, calm, solid off-white canvas.
 * No backdrop blur, no glowing orbs, no transparency effects.
 */
@Composable
fun LiquidGlassBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(PinggoOffWhite)
  ) {
    content()
  }
}

/**
 * Reusable Solid Surface Container.
 * Pure solid white surface with clean subtle border and soft elevation.
 */
@Composable
fun GlassContainer(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  elevation: Dp = 1.dp,
  animateModalReveal: Boolean = false,
  content: @Composable BoxScope.() -> Unit
) {
  Surface(
    modifier = modifier,
    shape = shape,
    color = SolidSurfaceWhite,
    border = BorderStroke(1.dp, SolidBorderColor),
    shadowElevation = elevation
  ) {
    Box {
      content()
    }
  }
}

/**
 * Reusable Solid Surface Card for lists and interactive cards.
 * Clean solid white background, dark charcoal text, subtle border.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val gestureModifier = if (onClick != null || onLongClick != null) {
    Modifier.combinedClickable(
      interactionSource = interactionSource,
      indication = ripple(bounded = true, color = PinggoPinkPrimary.copy(alpha = 0.12f)),
      onClick = { onClick?.invoke() },
      onLongClick = { onLongClick?.invoke() }
    )
  } else Modifier

  Surface(
    modifier = modifier.then(gestureModifier),
    shape = shape,
    color = SolidSurfaceWhite,
    border = BorderStroke(1.dp, SolidBorderColor),
    shadowElevation = 1.dp
  ) {
    Box {
      content()
    }
  }
}

/**
 * Reusable Solid Button.
 * Primary: Solid vibrant Pinggo Pink with high-contrast white text.
 * Secondary: Clean solid neutral button with subtle border and dark text.
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
  shape: Shape = RoundedCornerShape(16.dp),
  testTag: String = "glass_button"
) {
  val backgroundColor = if (isPrimary) PinggoPinkPrimary else SolidSurfaceVariant
  val contentColor = if (isPrimary) Color.White else LightPrimaryText
  val borderColor = if (isPrimary) Color.Transparent else SolidBorderColor

  Surface(
    modifier = modifier
      .heightIn(min = 50.dp)
      .clip(shape)
      .testTag(testTag)
      .clickable(
        enabled = enabled && !isLoading,
        onClick = onClick
      ),
    shape = shape,
    color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = if (isPrimary && enabled) 2.dp else 0.dp
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
      contentAlignment = Alignment.Center
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = contentColor,
          strokeWidth = 2.dp
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
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

/**
 * Reusable Solid Icon Button.
 * Clean circular white button with subtle border and crisp icon.
 */
@Composable
fun GlassIconButton(
  icon: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color? = null,
  size: Dp = 42.dp,
  testTag: String = "glass_icon_button"
) {
  val iconTint = tint ?: LightPrimaryText

  Surface(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = CircleShape,
    color = SolidSurfaceWhite,
    border = BorderStroke(1.dp, SolidBorderColor),
    shadowElevation = 1.dp
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

/**
 * Reusable Solid Search Bar.
 * Clean solid white capsule shape, clear search icon, readable placeholder, subtle border.
 */
@Composable
fun GlassSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Search...",
  onClear: () -> Unit = { onQueryChange("") }
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp),
    shape = RoundedCornerShape(24.dp),
    color = SolidSurfaceWhite,
    border = BorderStroke(1.dp, SolidBorderColor),
    shadowElevation = 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Search",
        tint = LightPlaceholderText,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Box(modifier = Modifier.weight(1f)) {
        if (query.isEmpty()) {
          Text(
            text = placeholder,
            color = LightPlaceholderText,
            style = MaterialTheme.typography.bodyMedium
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyMedium.copy(color = LightPrimaryText),
          cursorBrush = SolidColor(PinggoPinkPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_input")
        )
      }
      if (query.isNotEmpty()) {
        IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear",
            tint = LightSecondaryText,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

/**
 * Reusable Solid Input Field.
 * Clean solid white background, rounded corners, subtle border, readable text.
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
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = SolidSurfaceWhite,
    border = BorderStroke(1.dp, SolidBorderColor),
    shadowElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = LightSecondaryText,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
      }
      Box(modifier = Modifier.weight(1f)) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            color = LightPlaceholderText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          singleLine = singleLine,
          maxLines = maxLines,
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = LightPrimaryText,
            textAlign = textAlign
          ),
          keyboardOptions = keyboardOptions,
          keyboardActions = keyboardActions,
          visualTransformation = visualTransformation,
          cursorBrush = SolidColor(PinggoPinkPrimary),
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
 * Reusable Solid Avatar.
 * Clean circular avatar with solid border and crisp online status indicator.
 */
@Composable
fun GlassAvatar(
  photoUrl: String?,
  name: String,
  modifier: Modifier = Modifier,
  size: Dp = 50.dp,
  isOnline: Boolean = false,
  hasStatusUpdate: Boolean = false,
  onClick: (() -> Unit)? = null
) {
  val clickableModifier = if (onClick != null) {
    Modifier.clickable(onClick = onClick)
  } else Modifier

  Box(modifier = modifier.size(size).then(clickableModifier)) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      shape = CircleShape,
      color = SolidSurfaceVariant,
      border = BorderStroke(
        width = if (hasStatusUpdate) 2.dp else 1.dp,
        color = if (hasStatusUpdate) PinggoPinkPrimary else SolidBorderColor
      )
    ) {
      Box(contentAlignment = Alignment.Center) {
        if (!photoUrl.isNullOrEmpty()) {
          AsyncImage(
            model = photoUrl,
            contentDescription = "$name's avatar",
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape),
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
    }

    if (isOnline) {
      Box(
        modifier = Modifier
          .size(size * 0.28f)
          .align(Alignment.BottomEnd)
          .offset(x = 1.dp, y = 1.dp)
          .clip(CircleShape)
          .background(Color.White)
          .padding(2.dp)
          .clip(CircleShape)
          .background(OnlineGreen)
      )
    }
  }
}

/**
 * Reusable Solid Chat Bubble.
 * Outgoing: Solid Pinggo Pink with crisp white text.
 * Incoming: Solid White with subtle gray border and dark charcoal text.
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
    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
  } else {
    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
  }

  val backgroundColor = if (isSent) PinggoPinkPrimary else SolidSurfaceWhite
  val textColor = if (isSent) Color.White else LightPrimaryText
  val timeColor = if (isSent) Color.White.copy(alpha = 0.8f) else LightSecondaryText
  val borderStroke = if (isSent) null else BorderStroke(1.dp, SolidBorderColor)

  Surface(
    modifier = modifier,
    shape = bubbleShape,
    color = backgroundColor,
    border = borderStroke,
    shadowElevation = 1.dp
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
      if (!replySnippet.isNullOrEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSent) Color.Black.copy(alpha = 0.12f) else SolidSurfaceVariant)
            .padding(start = 8.dp, top = 6.dp, bottom = 6.dp, end = 8.dp)
        ) {
          Column {
            Text(
              text = replySender ?: "Pinggo User",
              color = if (isSent) Color.White else PinggoPinkPrimary,
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
        style = MaterialTheme.typography.bodyMedium,
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
          style = MaterialTheme.typography.labelSmall
        )
        if (isSent) {
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (read) "✓✓" else if (delivered) "✓✓" else "✓",
            color = if (read) Color(0xFFE0F2FE) else Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

/**
 * Reusable Solid Bottom Bar.
 * Clean solid white bottom navigation dock.
 */
@Composable
fun GlassBottomBar(
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = SolidSurfaceWhite,
    border = BorderStroke(0.5.dp, SolidBorderColor),
    shadowElevation = 8.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
      content = content
    )
  }
}

/**
 * Reusable Solid Dropdown Menu.
 * Clean solid white popup with high-contrast text and crisp icons.
 * Completely opaque and readable on every screen.
 */
@Composable
fun LiquidBlurDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 8.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier
      .widthIn(min = 220.dp)
      .background(SolidSurfaceWhite)
      .border(1.dp, SolidBorderColor, RoundedCornerShape(16.dp)),
    offset = offset,
    containerColor = SolidSurfaceWhite,
    shape = RoundedCornerShape(16.dp),
    shadowElevation = 8.dp,
    properties = PopupProperties(focusable = true)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(SolidSurfaceWhite)
        .padding(vertical = 4.dp)
    ) {
      content()
    }
  }
}

/**
 * Custom Menu Item for Solid Dropdown Menu.
 */
@Composable
fun LiquidBlurDropdownMenuItem(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  DropdownMenuItem(
    text = {
      Text(
        text = text,
        color = LightPrimaryText,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
      )
    },
    leadingIcon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = LightSecondaryText,
        modifier = Modifier.size(20.dp)
      )
    },
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp)
  )
}

/**
 * Reusable Dropdown Menu (Compatibility).
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
 * Reusable Solid Dialog.
 * Clean solid white card dialog with dark charcoal text.
 */
@Composable
fun GlassDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Dialog(onDismissRequest = onDismissRequest) {
    Surface(
      modifier = modifier
        .fillMaxWidth(0.92f)
        .wrapContentHeight(),
      shape = RoundedCornerShape(20.dp),
      color = SolidSurfaceWhite,
      border = BorderStroke(1.dp, SolidBorderColor),
      shadowElevation = 8.dp
    ) {
      Box(modifier = Modifier.padding(20.dp)) {
        content()
      }
    }
  }
}

/**
 * Solid Action Toolbar for selection states.
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
      .padding(horizontal = 16.dp, vertical = 6.dp),
    shape = RoundedCornerShape(16.dp),
    color = SolidSurfaceWhite,
    border = BorderStroke(1.dp, SolidBorderColor),
    shadowElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
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
 * Reusable Solid Toast notification.
 */
@Composable
fun GlassToast(
  message: String,
  modifier: Modifier = Modifier,
  isError: Boolean = false
) {
  val surfaceColor = if (isError) DestructiveRed else PinggoBlack
  val textColor = Color.White

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = surfaceColor,
    shadowElevation = 4.dp,
    modifier = modifier.padding(16.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = if (isError) "⚠️ " else "✓ ", fontSize = 16.sp)
      Text(
        text = message,
        color = textColor,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

/**
 * Solid Surface Bottom Navigation Bar.
 * Clean solid white dock with pink active state indicator and soft neutral gray for inactive.
 * No backdrop blur, no frosted glass, solid native mobile navigation.
 */
@Composable
fun BlurCapsuleDock(
  selectedTab: String,
  onTabSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val items = remember {
    listOf(
      Triple("Chats", Icons.Default.Chat, "Chats"),
      Triple("Calls", Icons.Default.Call, "Calls"),
      Triple("Updates", Icons.Default.CameraAlt, "Updates"),
      Triple("Profile", Icons.Default.Person, "Profile")
    )
  }

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = SolidSurfaceWhite,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    border = BorderStroke(0.5.dp, SolidBorderColor),
    shadowElevation = 8.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      items.forEach { (label, icon, tab) ->
        val isSelected = selectedTab == tab
        val interactionSource = remember { MutableInteractionSource() }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .clickable(
              interactionSource = interactionSource,
              indication = ripple(bounded = true, color = PinggoPinkPrimary)
            ) { onTabSelected(tab) }
            .padding(vertical = 6.dp)
            .testTag("nav_item_$label")
        ) {
          Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PinggoPinkPrimary else LightSecondaryText,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PinggoPinkPrimary else LightSecondaryText
          )
        }
      }
    }
  }
}
