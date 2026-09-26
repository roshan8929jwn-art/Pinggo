package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallSession
import com.example.ui.components.GlassAvatar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.*
import com.example.viewmodel.PinggoViewModel

@Composable
fun CallScreen(
  viewModel: PinggoViewModel,
  call: CallSession,
  onCallEnded: () -> Unit
) {
  val durationSec by viewModel.callDurationSec.collectAsState()
  val isMuted by viewModel.isMuted.collectAsState()
  val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
  val isVideoOn by viewModel.isVideoOn.collectAsState()
  val currentUser by viewModel.userProfile.collectAsState()

  val isIncoming = call.receiverId == currentUser?.uid && call.status == "ringing"
  val targetName = if (call.callerId == currentUser?.uid) call.receiverName else call.callerName
  val targetPhoto = if (call.callerId == currentUser?.uid) call.receiverPhoto else call.callerPhoto

  // Glowing pulse animation around the avatar
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top info
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 30.dp)) {
        Text(
          text = targetName.ifEmpty { "Pinggo Contact" },
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (isIncoming) "Incoming ${call.type} call..." else if (call.status == "ringing") "Ringing..." else String.format("%02d:%02d", durationSec / 60, durationSec % 60),
          fontSize = 16.sp,
          color = Color(0xCCFFFFFF),
          fontWeight = FontWeight.Medium
        )
      }

      // Center glowing pulsating avatar matching screenshot 4
      Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
      ) {
        // Outer glowing pulse ring
        Box(
          modifier = Modifier
            .size(190.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(Color(0x2210B981))
            .border(2.dp, Color(0x4434D399), CircleShape)
        )
        // Inner avatar
        GlassAvatar(
          photoUrl = targetPhoto,
          name = targetName,
          size = 140.dp
        )
      }

      // Controls Bar
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (isIncoming) {
          // Accept or Decline buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            CallActionButton(
              icon = Icons.Default.CallEnd,
              label = "Decline",
              bgColor = DestructiveRed,
              onClick = {
                viewModel.endCall()
                onCallEnded()
              }
            )
            CallActionButton(
              icon = Icons.Default.Phone,
              label = "Accept",
              bgColor = OnlineGreen,
              onClick = { viewModel.acceptCall() }
            )
          }
        } else {
          // In-call glass buttons: Mute, Video, Speaker
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            CallActionButton(
              icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
              label = if (isMuted) "Unmute" else "Mute",
              isActive = isMuted,
              onClick = { viewModel.toggleMute() }
            )
            CallActionButton(
              icon = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
              label = "Video",
              isActive = isVideoOn,
              onClick = { viewModel.toggleVideo() }
            )
            CallActionButton(
              icon = Icons.Default.VolumeUp,
              label = "Speaker",
              isActive = isSpeakerOn,
              onClick = { viewModel.toggleSpeaker() }
            )
          }

          Spacer(modifier = Modifier.height(30.dp))

          // End Call Floating Button
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(DestructiveRed)
              .clickable {
                viewModel.endCall()
                onCallEnded()
              }
              .testTag("end_call_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CallEnd,
              contentDescription = "End Call",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
fun CallActionButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  isActive: Boolean = false,
  bgColor: Color? = null
) {
  val background = bgColor ?: if (isActive) PinggoEmeraldPrimary else Color(0x33FFFFFF)
  val iconColor = Color.White

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .background(background)
        .border(1.dp, Color(0x40FFFFFF), CircleShape)
        .clickable { onClick() },
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = iconColor,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      color = Color.White,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
