package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Pinggo Pink Primary Palette (Brand Colors)
val PinggoPinkPrimary = Color(0xFFFF69B4) // Hot Pink requested
val PinggoPinkLight = Color(0xFFFFB6D9) // Soft Pink requested
val PinggoPinkDeep = Color(0xFFE91E63) 
val PinggoPinkGlow = Color(0x33FF69B4)

// Neutral Palette - ENSURING HIGH CONTRAST (AS REQUESTED)
val PinggoWhite = Color(0xFFFFFFFF) // Primary background
val PinggoOffWhite = Color(0xFFF8F8FA) // Secondary background
val PinggoBlack = Color(0xFF1C1C1E) // Dark readable text requested

// Light Theme Specific Colors
val LightPrimaryText = Color(0xFF1C1C1E)
val LightSecondaryText = Color(0xFF555555)
val LightPlaceholderText = Color(0xFF666666)

// Dark Theme Specific Colors
val DarkPrimaryText = Color(0xFFFFFFFF)
val DarkSecondaryText = Color(0xFFD0D0D0)
val DarkPlaceholderText = Color(0xFFBDBDBD)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF242424)

// Liquid Glass Accent & Glow
val PinggoGlowPink = Color(0x66FF69B4)
val PinggoGlowWhite = Color(0x44FFFFFF)

// Glass Panels & Surfaces
val GlassSurface = Color(0xCCFFFFFF)
val GlassBorder = Color(0x4DFF69B4)
val GlassBorderSoft = Color(0x26FF69B4)
val GlassCard = Color(0x99FFFFFF)
val GlassBubbleReceived = Color(0xCCF2F2F7)
val GlassBubbleSent = Color(0xE6FF69B4)
val GlassInnerHighlight = Color(0x33FFFFFF)

// Status & Indicators
val OnlinePink = Color(0xFFFF69B4)
val OnlineGreen = Color(0xFF22C55E)
val OfflineGray = Color(0xFF8E8E93)
val UnreadBadgeColor = Color(0xFFFF69B4)
val DestructiveRed = Color(0xFFFF3B30)
val WarningAmber = Color(0xFFFFCC00)

// Accessibility Helper Aliases (Legacy Support)
val DarkTextPrimary = LightPrimaryText
val DarkTextSecondary = LightSecondaryText
val LightTextPrimary = DarkPrimaryText
val LightTextSecondary = DarkSecondaryText

// Legacy Compatibility Aliases
val PinggoEmeraldPrimary = PinggoPinkPrimary
val PinggoMint = PinggoPinkPrimary
val PinggoMintUltraLight = PinggoPinkLight
val PinggoEmeraldDark = PinggoPinkDeep
val PinggoTeal = PinggoPinkPrimary
val PinggoCyan = PinggoPinkLight
