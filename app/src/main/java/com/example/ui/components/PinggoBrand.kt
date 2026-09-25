package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassBorderSoft
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.ui.theme.PinggoMint
import com.example.ui.theme.PinggoMintUltraLight

/**
 * Compact Penguin Chat-Bubble icon for top headers, tab bars, and inline branding.
 */
@Composable
fun PinggoBubbleIcon(
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
  withGlassContainer: Boolean = true
) {
  if (withGlassContainer) {
    Box(
      modifier = modifier
        .size(size)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x4034D399),
              Color(0x2010B981),
              Color(0x0A064E3B)
            )
          )
        )
        .border(1.dp, DarkGlassBorderSoft, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_pinggo_logo),
        contentDescription = "Pinggo Penguin Logo",
        modifier = Modifier.size(size * 0.85f)
      )
    }
  } else {
    Image(
      painter = painterResource(id = R.drawable.ic_pinggo_logo),
      contentDescription = "Pinggo Penguin Logo",
      modifier = modifier.size(size)
    )
  }
}

/**
 * Pinggo Wordmark with official color split:
 * "Pin" in crisp white, "ggo" in bright emerald green.
 */
@Composable
fun PinggoWordmark(
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 24.sp,
  letterSpacing: TextUnit = 0.5.sp
) {
  Text(
    text = buildAnnotatedString {
      withStyle(
        SpanStyle(
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      ) {
        append("Pin")
      }
      withStyle(
        SpanStyle(
          color = PinggoEmeraldPrimary,
          fontWeight = FontWeight.ExtraBold
        )
      ) {
        append("ggo")
      }
    },
    fontSize = fontSize,
    letterSpacing = letterSpacing,
    modifier = modifier
  )
}

/**
 * Official Pinggo tagline: "Chat • Connect • Together"
 */
@Composable
fun PinggoTagline(
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 12.sp,
  color: Color = PinggoMintUltraLight
) {
  Text(
    text = "Chat • Connect • Together",
    fontSize = fontSize,
    fontWeight = FontWeight.Medium,
    color = color,
    letterSpacing = 1.sp,
    textAlign = TextAlign.Center,
    modifier = modifier
  )
}

/**
 * Official Header Brand combining the compact penguin bubble icon with the Pinggo wordmark.
 * Used for Home, Chat list, Search, Profile, Settings, etc.
 */
@Composable
fun PinggoHeaderBrand(
  modifier: Modifier = Modifier,
  iconSize: Dp = 36.dp,
  fontSize: TextUnit = 22.sp
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    PinggoBubbleIcon(size = iconSize)
    Spacer(modifier = Modifier.width(10.dp))
    PinggoWordmark(fontSize = fontSize)
  }
}

/**
 * Full Pinggo Brand Presentation with subtle emerald/mint liquid glass aura,
 * official wordmark ("Pin" in white, "ggo" in green), and tagline.
 * Used prominently in Splash Screen, Login Screen, and About Pinggo.
 */
@Composable
fun PinggoFullLogo(
  modifier: Modifier = Modifier,
  iconSize: Dp = 110.dp,
  wordmarkSize: TextUnit = 36.sp,
  showTagline: Boolean = true,
  subtitle: String? = null
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Subtle emerald/mint liquid glass glow aura behind the penguin logo
    Box(
      modifier = Modifier
        .size(iconSize + 28.dp)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x6634D399),
              Color(0x3310B981),
              Color(0x0F059669),
              Color(0x00000000)
            )
          )
        )
        .border(1.5.dp, DarkGlassBorder, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_pinggo_logo),
        contentDescription = "Pinggo Official Logo",
        modifier = Modifier.size(iconSize)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // White "Pin" + Green "ggo" wordmark
    PinggoWordmark(
      fontSize = wordmarkSize,
      letterSpacing = 1.sp
    )

    if (showTagline) {
      Spacer(modifier = Modifier.height(6.dp))
      PinggoTagline(fontSize = (wordmarkSize.value * 0.35f).sp)
    }

    if (!subtitle.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = subtitle,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = PinggoMintUltraLight,
        textAlign = TextAlign.Center
      )
    }
  }
}
