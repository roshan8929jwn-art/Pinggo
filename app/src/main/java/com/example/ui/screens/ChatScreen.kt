package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.components.*
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
  onStartCall: (User, String) -> Unit,
  onOpenCamera: () -> Unit
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
  val otherUid = otherParticipant?.uid ?: ""
  val otherUser by viewModel.getUserFlow(otherUid).collectAsState(initial = null)
  val chatTitle = conversation.getTitle(myUid)
  val chatAvatar = conversation.getAvatarUrl(myUid)

  val isOtherTyping = typingUsers.isNotEmpty()
  val canSeeLastSeen = remember(currentUser, otherUser) {
    if (otherUser == null) false
    else if (currentUser?.uid == otherUser?.uid) true
    else if (currentUser != null && currentUser!!.blockedUsers.contains(otherUser!!.uid)) false
    else if (otherUser!!.blockedUsers.contains(currentUser?.uid ?: "")) false
    else when (otherUser!!.privacyLastSeen) {
      "everyone" -> true
      "friends" -> currentUser != null && (otherUser!!.friends.contains(currentUser!!.uid) || currentUser!!.friends.contains(otherUser!!.uid))
      "nobody" -> false
      else -> true
    }
  }

  val lastSeenText = remember(otherUser?.lastSeen) {
    val ts = otherUser?.lastSeen ?: 0L
    if (ts <= 0L) "offline"
    else {
      val diff = System.currentTimeMillis() - ts
      when {
        diff < 60_000L -> "last seen just now"
        diff < 3600_000L -> "last seen ${diff / 60_000L}m ago"
        diff < 86400_000L -> "last seen at " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
        else -> "last seen " + SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
      }
    }
  }

  val statusSubtitle = when {
    isOtherTyping -> "typing..."
    canSeeLastSeen && (otherUser?.isOnline == true) -> "online"
    canSeeLastSeen && otherUser != null -> lastSeenText
    else -> ""
  }

  val isBlockedByMe = currentUser?.blockedUsers?.contains(otherUid) == true || viewModel.cachedBlockedUsers.contains(otherUid)
  val isBlockedByOther = otherUser?.blockedUsers?.contains(myUid) == true

  var showUserProfileDialog by remember { mutableStateOf(false) }
  var latestScoreEvent by remember { mutableStateOf<com.example.model.ScoreEvent?>(null) }

  LaunchedEffect(Unit) {
    viewModel.scoreEvent.collect { event ->
      latestScoreEvent = event
    }
  }

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
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    
    LaunchedEffect(isKeyboardVisible) {
      if (isKeyboardVisible && messages.isNotEmpty()) {
        listState.animateScrollToItem(messages.size - 1)
      }
    }

    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
      ) {
      // Liquid Glass Header (Matched to Bottom Composer)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = SolidSurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, SolidBorderColor),
        shadowElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val backInteraction = remember { MutableInteractionSource() }
          IconButton(
            onClick = onBack,
            modifier = Modifier
              .size(36.dp)
              .liquidDrop(interactionSource = backInteraction, isPinkTint = true, maxRadius = 22.dp),
            interactionSource = backInteraction
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = LightPrimaryText
            )
          }

          Box(modifier = Modifier.clickable {
            if (otherUser != null) showUserProfileDialog = true
          }) {
            GlassAvatar(
              photoUrl = chatAvatar,
              name = chatTitle,
              size = 42.dp,
              isOnline = canSeeLastSeen && (otherUser?.isOnline == true)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(
            modifier = Modifier
              .weight(1f)
              .clickable {
                if (otherUser != null) showUserProfileDialog = true
              }
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = chatTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LightPrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              val targetScore = otherUser?.chatScore ?: 0L
              if (targetScore > 0L) {
                Spacer(modifier = Modifier.width(6.dp))
                ChatScoreBadge(score = targetScore, streakCount = otherUser?.streakCount ?: 0)
              }
            }
            if (statusSubtitle.isNotEmpty()) {
              Text(
                text = statusSubtitle,
                fontSize = 12.sp,
                color = if (isOtherTyping) PinggoPinkPrimary else if (otherUser?.isOnline == true && canSeeLastSeen) OnlinePink else LightSecondaryText
              )
            }
          }

          // Voice Call Icon
          val callInteraction = remember { MutableInteractionSource() }
          IconButton(
            onClick = {
              if (otherParticipant != null) {
                val target = User(uid = otherParticipant.uid, displayName = otherParticipant.displayName, username = otherParticipant.username, photoURL = otherParticipant.photoURL)
                onStartCall(target, "voice")
              }
            },
            modifier = Modifier
              .size(38.dp)
              .liquidDrop(interactionSource = callInteraction, isPinkTint = true, maxRadius = 22.dp),
            interactionSource = callInteraction
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Voice Call",
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          // Video Call Icon
          val videoInteraction = remember { MutableInteractionSource() }
          IconButton(
            onClick = {
              if (otherParticipant != null) {
                val target = User(uid = otherParticipant.uid, displayName = otherParticipant.displayName, username = otherParticipant.username, photoURL = otherParticipant.photoURL)
                onStartCall(target, "video")
              }
            },
            modifier = Modifier
              .size(38.dp)
              .liquidDrop(interactionSource = videoInteraction, isPinkTint = true, maxRadius = 22.dp),
            interactionSource = videoInteraction
          ) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = "Video Call",
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(22.dp)
            )
          }

          // Camera Icon
          val chatCamInteraction = remember { MutableInteractionSource() }
          IconButton(
            onClick = onOpenCamera,
            modifier = Modifier
              .size(38.dp)
              .liquidDrop(interactionSource = chatCamInteraction, isPinkTint = true, maxRadius = 22.dp),
            interactionSource = chatCamInteraction
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Camera",
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          // More Options
          Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = LightPrimaryText
              )
            }
            LiquidBlurDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
              LiquidBlurDropdownMenuItem(
                text = if (isBlockedByMe) "Unblock User" else "Block User",
                icon = if (isBlockedByMe) Icons.Default.CheckCircle else Icons.Default.Block,
                onClick = {
                  showMenu = false
                  if (otherParticipant != null) {
                    if (isBlockedByMe) {
                      viewModel.unblockUser(otherParticipant.uid)
                    } else {
                      viewModel.blockUser(User(uid = otherParticipant.uid, displayName = otherParticipant.displayName, username = otherParticipant.username, photoURL = otherParticipant.photoURL))
                    }
                  }
                }
              )
              LiquidBlurDropdownMenuItem(
                text = "Report User",
                icon = Icons.Default.Report,
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
              color = LightSecondaryText,
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
                      onPlayClick = { viewModel.playVoiceMessage(msg) },
                      readReceiptsEnabled = currentUser?.privacyReadReceipts != false
                    )
                  }
                  "image" -> {
                    Column(
                      modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSent) PinggoPinkPrimary else SolidSurfaceWhite)
                        .border(1.dp, if (isSent) Color.Transparent else SolidBorderColor, RoundedCornerShape(20.dp))
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
                      Row(
                        modifier = Modifier
                          .align(Alignment.End)
                          .padding(top = 4.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = timeFormatted,
                          fontSize = 10.sp,
                          color = (if (isSent) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f)
                        )
                        if (isSent) {
                          Spacer(modifier = Modifier.width(4.dp))
                          val showRead = currentUser?.privacyReadReceipts != false && msg.read
                          Text(
                            text = if (showRead) "✓✓" else if (msg.delivered) "✓✓" else "✓",
                            color = if (showRead) Color(0xFFE0F2FE) else Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }
                    }
                  }
                  else -> {
                    GlassChatBubble(
                      text = msg.text,
                      timestamp = timeFormatted,
                      isSent = isSent,
                      delivered = msg.delivered,
                      read = if (currentUser?.privacyReadReceipts == false) false else msg.read,
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
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                  color = PinggoPinkPrimary
                )
                Text(
                  text = reply.text,
                  fontSize = 12.sp,
                  color = LightPrimaryText,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              IconButton(onClick = { viewModel.setReplyingTo(null) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = LightPrimaryText, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // Bottom Message Composer
      if (isBlockedByMe) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          shape = RoundedCornerShape(24.dp),
          color = SolidSurfaceWhite,
          border = androidx.compose.foundation.BorderStroke(1.dp, SolidBorderColor),
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "You have blocked this contact.",
              fontSize = 13.sp,
              color = DestructiveRed,
              fontWeight = FontWeight.Medium
            )
            com.example.ui.components.GlassButton(
              text = "Unblock",
              isPrimary = true,
              onClick = { viewModel.unblockUser(otherUid) }
            )
          }
        }
      } else if (isBlockedByOther) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          shape = RoundedCornerShape(24.dp),
          color = SolidSurfaceWhite,
          border = androidx.compose.foundation.BorderStroke(1.dp, SolidBorderColor),
          shadowElevation = 2.dp
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "You cannot send messages to this contact.",
              fontSize = 13.sp,
              color = LightSecondaryText,
              fontWeight = FontWeight.Medium
            )
          }
        }
      } else {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          shape = RoundedCornerShape(24.dp),
          color = SolidSurfaceWhite,
          border = androidx.compose.foundation.BorderStroke(1.dp, SolidBorderColor),
          shadowElevation = 2.dp
        ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Plus button
          val plusInteraction = remember { MutableInteractionSource() }
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(PinggoPinkLight.copy(alpha = 0.3f))
              .liquidDrop(interactionSource = plusInteraction, isPinkTint = true, maxRadius = 24.dp)
              .clickable(
                interactionSource = plusInteraction,
                indication = ripple(bounded = false, color = PinggoPinkPrimary)
              ) { showMediaPicker = true },
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
                color = LightSecondaryText,
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
                color = LightPrimaryText,
                fontSize = 15.sp
              ),
              cursorBrush = SolidColor(PinggoPinkPrimary),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("message_input")
            )
          }

          // Attachment icons
          val attachInteraction = remember { MutableInteractionSource() }
          IconButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier
              .size(34.dp)
              .liquidDrop(interactionSource = attachInteraction, isPinkTint = true, maxRadius = 20.dp),
            interactionSource = attachInteraction
          ) {
            Icon(Icons.Default.AttachFile, "Attach", tint = LightSecondaryText, modifier = Modifier.size(20.dp))
          }

          val camInteraction = remember { MutableInteractionSource() }
          IconButton(
            onClick = onOpenCamera,
            modifier = Modifier
              .size(34.dp)
              .liquidDrop(interactionSource = camInteraction, isPinkTint = true, maxRadius = 20.dp),
            interactionSource = camInteraction
          ) {
            Icon(Icons.Default.CameraAlt, "Camera", tint = LightSecondaryText, modifier = Modifier.size(20.dp))
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Send or Mic Action button
          if (inputText.trim().isNotEmpty()) {
            val sendInteraction = remember { MutableInteractionSource() }
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PinggoPinkPrimary)
                .liquidDrop(interactionSource = sendInteraction, isPinkTint = false, maxRadius = 24.dp)
                .clickable(
                  interactionSource = sendInteraction,
                  indication = ripple(bounded = false, color = Color.White)
                ) {
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
            val micInteraction = remember { MutableInteractionSource() }
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PinggoPinkPrimary)
                .liquidDrop(interactionSource = micInteraction, isPinkTint = false, maxRadius = 24.dp)
                .clickable(
                  interactionSource = micInteraction,
                  indication = ripple(bounded = false, color = Color.White)
                ) { viewModel.startVoiceRecording() }
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
    }

    // "Choose Media" Dialog
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
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                  onOpenCamera()
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

    // "Recording..." Liquid Glass overlay
    if (isRecording) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(SolidSurfaceWhite)
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
              Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LightPrimaryText)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Recording...",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = LightPrimaryText
            )
          }

          Spacer(modifier = Modifier.height(40.dp))

          AudioWaveformVisualizer()

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = String.format("%02d:%02d", recordingDuration / 60, recordingDuration % 60),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LightPrimaryText
          )

          Spacer(modifier = Modifier.height(60.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(DestructiveRed.copy(alpha = 0.2f))
                .border(1.dp, DestructiveRed, CircleShape)
                .clickable { viewModel.cancelVoiceRecording() },
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Close, "Cancel", tint = DestructiveRed, modifier = Modifier.size(26.dp))
            }

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

    // Subtle floating celebration toast for message score, streaks, and level-ups
    ScoreCelebrationToast(
      event = latestScoreEvent,
      onDismiss = { latestScoreEvent = null },
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
    )

    // User Profile Preview with Public Chat Score
    if (showUserProfileDialog && otherUser != null) {
      val user = otherUser!!
      Dialog(onDismissRequest = { showUserProfileDialog = false }) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp)),
          color = SolidSurfaceWhite,
          border = androidx.compose.foundation.BorderStroke(1.dp, SolidBorderColor),
          shadowElevation = 8.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            GlassAvatar(
              photoUrl = user.photoURL,
              name = user.displayName.ifEmpty { user.username },
              size = 72.dp,
              isOnline = canSeeLastSeen && user.isOnline
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = user.displayName.ifEmpty { user.username },
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = LightPrimaryText
            )
            Text(
              text = user.handle,
              fontSize = 13.sp,
              color = PinggoPinkPrimary,
              fontWeight = FontWeight.Medium
            )
            if (user.bio.isNotEmpty()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = user.bio,
                fontSize = 12.sp,
                color = LightSecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Authoritative public score, level & daily streaks
            PublicScoreCard(user = user)

            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(
              text = "Close",
              isPrimary = false,
              onClick = { showUserProfileDialog = false },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
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
  val tileInteraction = remember { MutableInteractionSource() }
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(110.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(SolidSurfaceWhite)
      .border(1.dp, SolidBorderColor, RoundedCornerShape(16.dp))
      .clickable(
        interactionSource = tileInteraction,
        indication = ripple(bounded = true, color = PinggoPinkPrimary)
      ) { onClick() }
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
      color = LightPrimaryText,
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
  onPlayClick: () -> Unit,
  readReceiptsEnabled: Boolean = true
) {
  val shape = RoundedCornerShape(20.dp)
  val contentColor = if (isSent) Color.White else LightPrimaryText
  val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

  Row(
    modifier = Modifier
      .clip(shape)
      .background(if (isSent) PinggoPinkPrimary else SolidSurfaceWhite)
      .border(1.dp, if (isSent) Color.Transparent else SolidBorderColor, shape)
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
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "${message.voiceDurationSec}s • $timeFormatted",
          fontSize = 11.sp,
          color = contentColor.copy(alpha = 0.8f)
        )
        if (isSent) {
          Spacer(modifier = Modifier.width(4.dp))
          val showRead = readReceiptsEnabled && message.read
          Text(
            text = if (showRead) "✓✓" else if (message.delivered) "✓✓" else "✓",
            color = if (showRead) Color(0xFFE0F2FE) else Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
