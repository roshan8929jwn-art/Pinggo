package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.components.PinggoFullLogo
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.viewmodel.PinggoViewModel

@Composable
fun SettingsScreen(
  viewModel: PinggoViewModel,
  section: String,
  onBack: () -> Unit
) {
  val themeMode by viewModel.themeMode.collectAsState()
  val user by viewModel.userProfile.collectAsState()

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        PinggoBubbleIcon(size = 30.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = when (section) {
            "privacy" -> "Privacy"
            "appearance" -> "Appearance"
            "about" -> "About Pinggo"
            else -> "Settings"
          },
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      when (section) {
        "appearance" -> {
          Text(
            text = "Theme Mode",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
              ThemeOptionRow(
                title = "System Default",
                icon = Icons.Default.PhoneAndroid,
                selected = themeMode == AppThemeMode.SYSTEM,
                onSelect = { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
              )
              ThemeOptionRow(
                title = "Light Mode",
                icon = Icons.Default.LightMode,
                selected = themeMode == AppThemeMode.LIGHT,
                onSelect = { viewModel.setThemeMode(AppThemeMode.LIGHT) }
              )
              ThemeOptionRow(
                title = "Dark Mode",
                icon = Icons.Default.DarkMode,
                selected = themeMode == AppThemeMode.DARK,
                onSelect = { viewModel.setThemeMode(AppThemeMode.DARK) }
              )
            }
          }
        }

        "privacy" -> {
          Text(
            text = "Privacy Controls",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Read Receipts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                  )
                  Text(
                    text = "If turned off, you won't send or see read receipts",
                    fontSize = 12.sp,
                    color = Color(0xCCFFFFFF)
                  )
                }
                Switch(
                  checked = user?.privacyReadReceipts ?: true,
                  onCheckedChange = {
                    user?.let { u ->
                      viewModel.createProfile(u.displayName, u.username, u.bio, u.photoURL) {}
                    }
                  },
                  colors = SwitchDefaults.colors(checkedThumbColor = PinggoEmeraldPrimary)
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Online Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                  )
                  Text(
                    text = "Show when you are actively using Pinggo",
                    fontSize = 12.sp,
                    color = Color(0xCCFFFFFF)
                  )
                }
                Switch(
                  checked = user?.privacyOnlineStatus ?: true,
                  onCheckedChange = { },
                  colors = SwitchDefaults.colors(checkedThumbColor = PinggoEmeraldPrimary)
                )
              }
            }
          }
        }

        "about" -> {
          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              PinggoFullLogo(
                iconSize = 90.dp,
                wordmarkSize = 32.sp,
                showTagline = true,
                subtitle = "Same Vibes, New Experience"
              )
              Spacer(modifier = Modifier.height(18.dp))
              Text(
                text = "Version 1.0.0 (Production Build)\n" +
                  "Backend: Google Cloud Firestore & Firebase Auth\n" +
                  "Design System: Apple-inspired Liquid Glass\n" +
                  "Media: Firebase Storage Cloud Infrastructure\n" +
                  "Signaling: WebRTC Peer-to-Peer Calls",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
              )
            }
          }
        }

        else -> {
          // General Settings overview
          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Pinggo Mobile Messenger",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Connected as @${user?.username ?: "user"}",
                fontSize = 13.sp,
                color = PinggoEmeraldPrimary,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ThemeOptionRow(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onSelect: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onSelect() }
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (selected) PinggoEmeraldPrimary else Color(0xAAFFFFFF),
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.width(14.dp))
    Text(
      text = title,
      fontSize = 15.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
      color = Color.White,
      modifier = Modifier.weight(1f)
    )
    RadioButton(
      selected = selected,
      onClick = onSelect,
      colors = RadioButtonDefaults.colors(selectedColor = PinggoEmeraldPrimary)
    )
  }
}
