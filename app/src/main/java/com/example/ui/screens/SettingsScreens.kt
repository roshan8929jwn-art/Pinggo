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
            "notifications" -> "Notifications"
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

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "Glass Design",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          val currentGlassDesign by viewModel.glassDesign.collectAsState()

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
              GlassDesignOptionRow(
                title = "Clear Glass",
                description = "Transparent frosted-glass surfaces",
                selected = currentGlassDesign == com.example.ui.theme.GlassDesign.CLEAR,
                onSelect = { viewModel.setGlassDesign(com.example.ui.theme.GlassDesign.CLEAR) }
              )
              GlassDesignOptionRow(
                title = "Pink Glass",
                description = "Soft pink translucent glass surfaces",
                selected = currentGlassDesign == com.example.ui.theme.GlassDesign.PINK,
                onSelect = { viewModel.setGlassDesign(com.example.ui.theme.GlassDesign.PINK) }
              )
              GlassDesignOptionRow(
                title = "Crystal Glass",
                description = "Clear crystal-inspired translucent surfaces",
                selected = currentGlassDesign == com.example.ui.theme.GlassDesign.CRYSTAL,
                onSelect = { viewModel.setGlassDesign(com.example.ui.theme.GlassDesign.CRYSTAL) }
              )
            }
          }
        }

        "privacy" -> {
          val blockedUsers by viewModel.blockedUsersList.collectAsState()
          androidx.compose.runtime.LaunchedEffect(Unit) {
            viewModel.loadBlockedUsers()
          }

          Text(
            text = "Last Seen Visibility",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
              val currentLastSeen = user?.privacyLastSeen ?: "everyone"
              listOf("Everyone" to "everyone", "My Friends" to "friends", "Nobody" to "nobody").forEach { (label, key) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      user?.let { u ->
                        viewModel.updatePrivacySettings(key, u.privacyReadReceipts, u.privacyStatus)
                      }
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = label,
                    fontSize = 15.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                  )
                  RadioButton(
                    selected = currentLastSeen == key,
                    onClick = {
                      user?.let { u ->
                        viewModel.updatePrivacySettings(key, u.privacyReadReceipts, u.privacyStatus)
                      }
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = com.example.ui.theme.PinggoPinkPrimary)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Read Receipts",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Send Read Receipts",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.White
                )
                Text(
                  text = if (user?.privacyReadReceipts != false) "On • Senders see when messages are read" else "Off • No read status sent or seen",
                  fontSize = 12.sp,
                  color = Color(0xCCFFFFFF)
                )
              }
              Switch(
                checked = user?.privacyReadReceipts ?: true,
                onCheckedChange = { checked ->
                  user?.let { u ->
                    viewModel.updatePrivacySettings(u.privacyLastSeen, checked, u.privacyStatus)
                  }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PinggoPinkPrimary)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Status Privacy",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
              val currentStatusPriv = user?.privacyStatus ?: "everyone"
              listOf("Everyone" to "everyone", "My Friends" to "friends").forEach { (label, key) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      user?.let { u ->
                        viewModel.updatePrivacySettings(u.privacyLastSeen, u.privacyReadReceipts, key)
                      }
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = label,
                    fontSize = 15.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                  )
                  RadioButton(
                    selected = currentStatusPriv == key,
                    onClick = {
                      user?.let { u ->
                        viewModel.updatePrivacySettings(u.privacyLastSeen, u.privacyReadReceipts, key)
                      }
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = com.example.ui.theme.PinggoPinkPrimary)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Blocked Users (${blockedUsers.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          if (blockedUsers.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
              Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                  text = "No blocked users",
                  fontSize = 14.sp,
                  color = Color(0xCCFFFFFF)
                )
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              blockedUsers.forEach { blockedUser ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    com.example.ui.components.GlassAvatar(
                      photoUrl = blockedUser.photoURL,
                      name = blockedUser.displayName,
                      size = 44.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = blockedUser.displayName.ifEmpty { blockedUser.username },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                      )
                      Text(
                        text = "@${blockedUser.username}",
                        fontSize = 12.sp,
                        color = com.example.ui.theme.PinggoMint
                      )
                    }
                    com.example.ui.components.GlassButton(
                      text = "Unblock",
                      isPrimary = false,
                      onClick = { viewModel.unblockUser(blockedUser.uid) }
                    )
                  }
                }
              }
            }
          }
        }

        "notifications" -> {
          Text(
            text = "Message Notifications",
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
                  Text(text = "Direct Messages", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                  Text(text = "Receive alerts for one-on-one chats", fontSize = 12.sp, color = Color(0xCCFFFFFF))
                }
                Switch(
                  checked = user?.notificationMessage ?: true,
                  onCheckedChange = { checked ->
                    user?.let { u ->
                      viewModel.updateNotificationSettings(checked, u.notificationGroup, u.notificationPreview, u.notificationCall)
                    }
                  },
                  colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PinggoPinkPrimary)
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = "Group Messages", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                  Text(text = "Receive alerts for group chats", fontSize = 12.sp, color = Color(0xCCFFFFFF))
                }
                Switch(
                  checked = user?.notificationGroup ?: true,
                  onCheckedChange = { checked ->
                    user?.let { u ->
                      viewModel.updateNotificationSettings(u.notificationMessage, checked, u.notificationPreview, u.notificationCall)
                    }
                  },
                  colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PinggoPinkPrimary)
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = "Show Notification Preview", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                  Text(text = "Preview message text inside lockscreen notification banner", fontSize = 12.sp, color = Color(0xCCFFFFFF))
                }
                Switch(
                  checked = user?.notificationPreview ?: true,
                  onCheckedChange = { checked ->
                    user?.let { u ->
                      viewModel.updateNotificationSettings(u.notificationMessage, u.notificationGroup, checked, u.notificationCall)
                    }
                  },
                  colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PinggoPinkPrimary)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Call Notifications",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xCCFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Incoming Calls", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(text = "Vibrate and ring for incoming voice & video calls", fontSize = 12.sp, color = Color(0xCCFFFFFF))
              }
              Switch(
                checked = user?.notificationCall ?: true,
                onCheckedChange = { checked ->
                  user?.let { u ->
                    viewModel.updateNotificationSettings(u.notificationMessage, u.notificationGroup, u.notificationPreview, checked)
                  }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = com.example.ui.theme.PinggoPinkPrimary)
              )
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
fun GlassDesignOptionRow(
  title: String,
  description: String,
  selected: Boolean,
  onSelect: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onSelect() }
      .padding(horizontal = 8.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = Color.White
      )
      Text(
        text = description,
        fontSize = 12.sp,
        color = Color(0xAAFFFFFF)
      )
    }
    RadioButton(
      selected = selected,
      onClick = onSelect,
      colors = RadioButtonDefaults.colors(selectedColor = com.example.ui.theme.PinggoPinkPrimary)
    )
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
      tint = if (selected) com.example.ui.theme.PinggoPinkPrimary else Color(0xAAFFFFFF),
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
      colors = RadioButtonDefaults.colors(selectedColor = com.example.ui.theme.PinggoPinkPrimary)
    )
  }
}
