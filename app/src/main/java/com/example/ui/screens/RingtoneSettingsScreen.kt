package com.example.ui.screens

import android.media.RingtoneManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.theme.*
import com.example.util.RingtoneHelper
import com.example.util.RingtoneItem
import com.example.viewmodel.PinggoViewModel

@Composable
fun RingtoneSettingsScreen(
  viewModel: PinggoViewModel,
  isCallRingtone: Boolean,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val user by viewModel.userProfile.collectAsState()

  val ringtoneType = if (isCallRingtone) RingtoneManager.TYPE_RINGTONE else RingtoneManager.TYPE_NOTIFICATION
  val availableRingtones = remember { RingtoneHelper.getAvailableRingtones(context, ringtoneType) }

  // Initial loaded settings
  val initialSetting = remember(user, isCallRingtone) {
    if (isCallRingtone) {
      val (savedUri, savedTitle) = RingtoneHelper.getCallRingtone(context)
      val uri = user?.callRingtoneUri?.takeIf { it.isNotBlank() } ?: savedUri
      val title = user?.callRingtoneTitle?.takeIf { it.isNotBlank() } ?: savedTitle
      Pair(uri, title)
    } else {
      val (savedUri, savedTitle) = RingtoneHelper.getNotificationRingtone(context)
      val uri = user?.notificationRingtoneUri?.takeIf { it.isNotBlank() } ?: savedUri
      val title = user?.notificationRingtoneTitle?.takeIf { it.isNotBlank() } ?: savedTitle
      Pair(uri, title)
    }
  }

  var selectedUri by remember { mutableStateOf(initialSetting.first) }
  var selectedTitle by remember { mutableStateOf(initialSetting.second) }
  var playingUri by remember { mutableStateOf<String?>(null) }

  // Stop playback when leaving screen
  DisposableEffect(Unit) {
    onDispose {
      RingtoneHelper.stopPreview()
    }
  }

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            RingtoneHelper.stopPreview()
            onBack()
          },
          modifier = Modifier.size(40.dp)
        ) {
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
          text = if (isCallRingtone) "Call Ringtone" else "Notification Ringtone",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Current Selected Banner
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Color(0x3310B981)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isCallRingtone) Icons.Default.Phone else Icons.Default.Notifications,
              contentDescription = null,
              tint = PinggoMint,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isCallRingtone) "Active Call Ringtone" else "Active Notification Sound",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = PinggoMintUltraLight
            )
            Text(
              text = selectedTitle,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          // Play preview of currently selected
          IconButton(
            onClick = {
              if (playingUri == selectedUri) {
                RingtoneHelper.stopPreview()
                playingUri = null
              } else {
                RingtoneHelper.playPreview(context, selectedUri)
                playingUri = selectedUri
              }
            }
          ) {
            Icon(
              imageVector = if (playingUri == selectedUri) Icons.Default.Stop else Icons.Default.PlayArrow,
              contentDescription = "Preview Active Ringtone",
              tint = PinggoMint,
              modifier = Modifier.size(26.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Action Row for Notification Channel / Default Reset
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isCallRingtone) "System Ringtones (${availableRingtones.size})" else "Notification Sounds (${availableRingtones.size})",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xCCFFFFFF)
        )

        if (!isCallRingtone) {
          Text(
            text = "System Channel Settings",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PinggoMint,
            modifier = Modifier
              .clickable { RingtoneHelper.openChannelSettings(context) }
              .padding(4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // List of available system ringtones
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(availableRingtones, key = { it.uriString.ifEmpty { it.title } }) { item ->
          val isSelected = selectedUri == item.uriString || (selectedUri.isEmpty() && item.isDefault)
          val isPlaying = playingUri == item.uriString

          GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              selectedUri = item.uriString
              selectedTitle = item.title
              RingtoneHelper.playPreview(context, item.uriString)
              playingUri = item.uriString
            }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Selection Indicator
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) PinggoEmeraldPrimary else Color(0x22FFFFFF))
                  .border(1.dp, if (isSelected) PinggoMint else Color(0x44FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.title,
                  fontSize = 15.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else Color(0xEEFFFFFF)
                )
                if (item.isDefault) {
                  Text(
                    text = "System Default",
                    fontSize = 11.sp,
                    color = PinggoMintUltraLight
                  )
                }
              }

              // Play preview button
              IconButton(
                onClick = {
                  if (isPlaying) {
                    RingtoneHelper.stopPreview()
                    playingUri = null
                  } else {
                    RingtoneHelper.playPreview(context, item.uriString)
                    playingUri = item.uriString
                  }
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                  contentDescription = "Listen",
                  tint = if (isPlaying) PinggoMint else Color(0xCCFFFFFF),
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Save / Apply Button
      GlassButton(
        text = if (isCallRingtone) "Save Call Ringtone" else "Save Notification Sound",
        isPrimary = true,
        onClick = {
          RingtoneHelper.stopPreview()
          if (isCallRingtone) {
            viewModel.saveCallRingtone(selectedUri, selectedTitle)
          } else {
            viewModel.saveNotificationRingtone(selectedUri, selectedTitle)
          }
          onBack()
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("save_ringtone_button")
      )
    }
  }
}
