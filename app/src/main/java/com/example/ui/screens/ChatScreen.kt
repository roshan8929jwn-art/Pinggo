package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Conversation
import com.example.model.Message
import com.example.model.User
import com.example.ui.components.GlassAvatar
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChatBubble
import com.example.ui.components.GlassContainer
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.*
import com.example.viewmodel.PinggoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
  viewModel: PinggoViewModel,
  conversation: Conversation,
  onBack: () -> Unit,
  onStartCall: (User, String) -> Unit
) {
  val currentUser by viewModel.userProfile.collectAsState()
  val messages by viewModel.activeMessages.collectAsState()
  val typingUsers by viewModel.typingUsers.collectAsState()
  val replyingTo by viewModel.replyingTo.collectAsState()
  val isRecording by viewModel.voiceHelper.isRecording.collectAsState()
  val recordingDuration by viewModel.voiceHelper.recordingDurationSec.collectAsState()
  val isPlaying by viewModel.voiceHelper.isPlaying.collectAsState()
  val playingMessageId by viewModel.voiceHelper.playingMessageId.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var showMenu by remember { mutableStateOf(false) }
  var showMediaPicker by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  val myUid = currentUser?.uid ?: ""
  val otherParticipant = conversation.getOtherParticipant(myUid)
  val chatTitle = conversation.getTitle(myUid)
  val chatAvatar = conversation.getAvatarUrl(myUid)

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { viewModel.sendImageMessage(it) }
  }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .imePadding()
    ) {
      // Liquid Glass Header matching Screen 5
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        shadowElevation = 8.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          GlassAvatar(
            photoUrl = chatAvatar,
            name = chatTitle,
            size = 42.dp,
            isOnline = true
          )

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = chatTitle,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (typingUsers.isNotEmpty()) "typing..." else "online",
              fontSize = 12.sp,
              color = if (typingUsers.isNotEmpty()) PinggoPinkPrimary else OnlinePink
            )
          }

          // Voice Call Icon
          IconButton(
            onClick = {
              if (otherParticipant != null) {
                val target = User(uid = otherParticipant.uid, displayName = otherParticipant.displayName, username = otherParticipant.username, photoURL = otherParticipant.photoURL)
                onStartCall(target, "voice")
              }
            },
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Voice Call",
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          // Video Call Icon
          IconButton(
            onClick = {
              if (otherParticipant != null) {
                val target = User(uid = otherParticipant.uid, displayName = otherParticipant.displayName, username = otherParticipant.username, photoURL = otherParticipant.photoURL)
                onStartCall(target, "video")
              }
            },
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = "Video Call",
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(22.dp)
            )
          }

          // More Options
          Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.White
              )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
              DropdownMenuItem(
                text = { Text("Block User") },
                onClick = {
                  showMenu = false
                  if (otherParticipant != null) {
                    viewModel.blockUser(User(uid = otherParticipant.uid, username = otherParticipant.username))
                  }
                }
              )
              DropdownMenuItem(
                text = { Text("Report User") },
                onClick = {
                  showMenu = false
                  if (otherParticipant != null) {
                    viewModel.reportUser(User(uid = otherParticipant.uid, username = otherParticipant.username), "Inappropriate behavior")
                  }
                }
              )
            }
          }
        }
      }

      // Messages Area
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (messages.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
              text = "Say hello! 👋",
              color = PinggoMintUltraLight,
              fontSize = 15.sp
            )
          }
        } else {
          LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(messages, key = { it.id }) { msg ->
              val isSent = msg.senderId == myUid
              val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))

              Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart
              ) {
                when (msg.type) {
                  "voice" -> {
                    VoiceMessageBubble(
                      message = msg,
                      isSent = isSent,
                      isPlaying = isPlaying && playingMessageId == msg.id,
                      onPlayClick = { viewModel.playVoiceMessage(msg) }
                    )
                  }
                  "image" -> {
                    Column(
                      modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSent) PinggoPinkPrimary else Color.White)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(6.dp)
                    ) {
                      AsyncImage(
                        model = msg.mediaUrl,
                        contentDescription = "Shared photo",
                        modifier = Modifier
                          .size(width = 220.dp, height = 220.dp)
                          .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                      )
                      Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = Color(0xCCFFFFFF),
                        modifier = Modifier
                          .align(Alignment.End)
                          .padding(top = 4.dp, end = 6.dp)
                      )
                    }
                  }
                  else -> {
                    GlassChatBubble(
                      text = msg.text,
                      timestamp = timeFormatted,
                      isSent = isSent,
                      replySnippet = msg.replyToText.ifEmpty { null },
                      replySender = msg.replyToSender.ifEmpty { null }
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Reply Preview Bar
      AnimatedVisibility(visible = replyingTo != null) {
        replyingTo?.let { reply ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xCC0E342B),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorderSoft)
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Replying to ${reply.senderName}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = PinggoMint
                )
                Text(
                  text = reply.text,
                  fontSize = 12.sp,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              IconButton(onClick = { viewModel.setReplyingTo(null) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // Bottom Message Composer matching Screen 5
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        shadowElevation = 10.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Plus button (opens "Choose Media" sheet matching Screen 8)
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(PinggoPinkLight.copy(alpha = 0.3f))
              .clickable { showMediaPicker = true },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Add, "Choose Media", tint = PinggoPinkPrimary, modifier = Modifier.size(20.dp))
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Text Input Field
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 6.dp)
          ) {
            if (inputText.isEmpty()) {
              Text(
                text = "Type a message...",
                color = Color(0x99FFFFFF),
                fontSize = 15.sp
              )
            }
            BasicTextField(
              value = inputText,
              onValueChange = {
                inputText = it
                viewModel.setTyping(it.isNotEmpty())
              },
              maxLines = 4,
              textStyle = TextStyle(
                color = Color.Black,
                fontSize = 15.sp
              ),
              cursorBrush = SolidColor(PinggoPinkPrimary),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("message_input")
            )
          }

          // Emoji sticker icon
          IconButton(
            onClick = {
              viewModel.sendMessage("🐧")
            },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(Icons.Default.SentimentSatisfiedAlt, "Emoji", tint = Color(0xCCFFFFFF), modifier = Modifier.size(20.dp))
          }

          // Attachment icon
          IconButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(Icons.Default.AttachFile, "Attach", tint = Color(0xCCFFFFFF), modifier = Modifier.size(20.dp))
          }

          // Camera icon
          IconButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(Icons.Default.CameraAlt, "Camera", tint = Color(0xCCFFFFFF), modifier = Modifier.size(20.dp))
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Send or Mic Action button
          if (inputText.trim().isNotEmpty()) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PinggoPinkPrimary)
                .clickable {
                  viewModel.sendMessage(inputText)
                  inputText = ""
                  viewModel.setTyping(false)
                }
                .testTag("send_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          } else {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PinggoPinkPrimary)
                .clickable { viewModel.startVoiceRecording() }
                .testTag("record_mic_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Record voice",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }

    // "Choose Media" Liquid Glass Dialog matching Screen 8
    if (showMediaPicker) {
      Dialog(onDismissRequest = { showMediaPicker = false }) {
        GlassContainer(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(32.dp),
          elevation = 16.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Choose Media",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2-row grid of glass media options matching screenshot 8
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              MediaTile(
                icon = Icons.Default.Photo,
                label = "Gallery",
                onClick = {
                  showMediaPicker = false
                  imagePickerLauncher.launch("image/*")
                }
              )
              MediaTile(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                onClick = {
                  showMediaPicker = false
                  imagePickerLauncher.launch("image/*")
                }
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              MediaTile(
                icon = Icons.Default.Description,
                label = "Document",
                onClick = {
                  showMediaPicker = false
                  imagePickerLauncher.launch("*/*")
                }
              )
              MediaTile(
                icon = Icons.Default.LocationOn,
                label = "Location",
                onClick = {
                  showMediaPicker = false
                  viewModel.sendMessage("📍 Shared Location: Pinggo Central")
                }
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center
            ) {
              MediaTile(
                icon = Icons.Default.Mic,
                label = "Voice",
                onClick = {
                  showMediaPicker = false
                  viewModel.startVoiceRecording()
                }
              )
            }

            Spacer(modifier = Modifier.height(22.dp))

            GlassButton(
              text = "Cancel",
              isPrimary = false,
              onClick = { showMediaPicker = false },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // "Recording..." Liquid Glass overlay matching Screen 9
    if (isRecording) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.White.copy(alpha = 0.95f))
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(onClick = { viewModel.cancelVoiceRecording() }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Recording...",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )
          }

          Spacer(modifier = Modifier.height(40.dp))

          // Audio waveform animation visualizer matching Screen 9
          AudioWaveformVisualizer()

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = String.format("%02d:%02d", recordingDuration / 60, recordingDuration % 60),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
          )

          Spacer(modifier = Modifier.height(60.dp))

          // Controls row: Cancel (X), glowing mic button, Send (checkmark)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Cancel button
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0x33EF4444))
                .border(1.dp, DestructiveRed, CircleShape)
                .clickable { viewModel.cancelVoiceRecording() },
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Close, "Cancel", tint = DestructiveRed, modifier = Modifier.size(26.dp))
            }

            // Pulsing Mic Icon
            Box(
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                  Brush.radialGradient(
                    colors = listOf(PinggoPinkLight, PinggoPinkPrimary)
                  )
                )
                .border(2.dp, Color.White, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Mic, "Recording", tint = Color.White, modifier = Modifier.size(40.dp))
            }

            // Send checkmark button
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0x3322C55E))
                .border(1.dp, Color(0xFF22C55E), CircleShape)
                .clickable { viewModel.stopAndSendVoiceRecording() },
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Check, "Send", tint = Color(0xFF22C55E), modifier = Modifier.size(26.dp))
            }
          }

          Spacer(modifier = Modifier.height(20.dp))
        }
      }
    }
  }
}

@Composable
fun MediaTile(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(110.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(Color.White.copy(alpha = 0.5f))
      .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .padding(vertical = 16.dp)
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(PinggoPinkLight.copy(alpha = 0.3f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = label, tint = PinggoPinkPrimary, modifier = Modifier.size(24.dp))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = label,
      color = Color.Black,
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun AudioWaveformVisualizer() {
  val infiniteTransition = rememberInfiniteTransition(label = "waveform")
  val heights = (0..24).map { i ->
    infiniteTransition.animateFloat(
      initialValue = 12f + (i % 5) * 6f,
      targetValue = 44f - (i % 4) * 8f,
      animationSpec = infiniteRepeatable(
        animation = tween(400 + (i * 35), easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "wave_$i"
    )
  }

  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.height(60.dp)
  ) {
    heights.forEach { animHeight ->
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(animHeight.value.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(PinggoPinkPrimary)
      )
    }
  }
}

@Composable
fun VoiceMessageBubble(
  message: Message,
  isSent: Boolean,
  isPlaying: Boolean,
  onPlayClick: () -> Unit
) {
  val shape = RoundedCornerShape(20.dp)
  val bg = if (isSent) PinggoPinkPrimary else Color.White
  val contentColor = if (isSent) Color.White else Color.Black

  Row(
    modifier = Modifier
      .clip(shape)
      .background(bg)
      .border(1.dp, if (isSent) Color(0x40FFFFFF) else DarkGlassBorderSoft, shape)
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(if (isSent) Color.White else PinggoPinkPrimary)
        .clickable { onPlayClick() },
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
        contentDescription = "Play voice",
        tint = if (isSent) PinggoPinkPrimary else Color.White,
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column {
      Text(
        text = "Voice message",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = contentColor
      )
      Text(
        text = "${message.voiceDurationSec}s",
        fontSize = 11.sp,
        color = contentColor.copy(alpha = 0.8f)
      )
    }
  }
}
