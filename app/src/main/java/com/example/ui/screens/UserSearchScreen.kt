package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Conversation
import com.example.model.User
import com.example.model.handle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PinggoViewModel

@Composable
fun UserSearchScreen(
  viewModel: PinggoViewModel,
  onBack: () -> Unit,
  onChatReady: (Conversation) -> Unit
) {
  val searchQuery by viewModel.searchQuery.collectAsState()
  val searchResults by viewModel.searchResults.collectAsState()
  val isSearching by viewModel.isSearching.collectAsState()
  val previewUser by viewModel.previewUser.collectAsState()
  val currentUser by viewModel.userProfile.collectAsState()
  val outgoingRequests by viewModel.outgoingRequests.collectAsState()
  val incomingRequests by viewModel.friendRequests.collectAsState()

  val myUid = currentUser?.uid ?: ""
  val myFriends = currentUser?.friends ?: emptyList()

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 16.dp)
    ) {
      // Header with Back button and search input
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val backInteraction = remember { MutableInteractionSource() }
        IconButton(
          onClick = onBack,
          interactionSource = backInteraction,
          modifier = Modifier
            .size(40.dp)
            .liquidDrop(interactionSource = backInteraction, isPinkTint = true, maxRadius = 22.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        PinggoBubbleIcon(size = 32.dp)
        Spacer(modifier = Modifier.width(8.dp))
        GlassSearchBar(
          query = searchQuery,
          onQueryChange = { viewModel.searchUsers(it) },
          placeholder = "Search by @username or name...",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (isSearching) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = PinggoPinkPrimary)
        }
      } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
          Text(
            text = "No registered Pinggo users found for \"$searchQuery\"",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(searchResults, key = { it.uid }) { user ->
            val isMe = user.uid == myUid
            val isFriend = myFriends.contains(user.uid)
            val isOutgoingPending = outgoingRequests.any { it.receiverId == user.uid && it.status == "pending" }
            val incomingReq = incomingRequests.firstOrNull { it.senderId == user.uid && it.status == "pending" }

            GlassCard(
              modifier = Modifier.fillMaxWidth(),
              onClick = { viewModel.setPreviewUser(user) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                GlassAvatar(
                  photoUrl = user.photoURL,
                  name = user.displayName,
                  size = 48.dp,
                  isOnline = user.isOnline
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = user.displayName.ifEmpty { user.username },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = user.handle,
                    fontSize = 13.sp,
                    color = PinggoPinkPrimary,
                    fontWeight = FontWeight.Medium
                  )
                }

                // Action buttons based on real friendship status
                when {
                  isMe -> {
                    Surface(
                      color = PinggoPinkLight.copy(alpha = 0.2f),
                      shape = RoundedCornerShape(12.dp)
                    ) {
                      Text(
                        text = "You",
                        color = PinggoPinkPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                      )
                    }
                  }
                  isFriend -> {
                    val chatBtnInteraction = remember { MutableInteractionSource() }
                    Button(
                      onClick = {
                        viewModel.startDirectChat(user) { conv ->
                          onChatReady(conv)
                        }
                      },
                      interactionSource = chatBtnInteraction,
                      colors = ButtonDefaults.buttonColors(containerColor = PinggoPinkPrimary),
                      shape = RoundedCornerShape(14.dp),
                      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                      modifier = Modifier
                        .height(36.dp)
                        .liquidDrop(interactionSource = chatBtnInteraction, isPinkTint = false, maxRadius = 24.dp)
                    ) {
                      Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                  isOutgoingPending -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Surface(
                        color = PinggoPinkLight.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                      ) {
                        Text(
                          text = "Requested",
                          color = PinggoPinkPrimary,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.SemiBold,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(6.dp))
                      val cancelInteraction = remember { MutableInteractionSource() }
                      IconButton(
                        onClick = { viewModel.cancelFriendRequest(user.uid) },
                        interactionSource = cancelInteraction,
                        modifier = Modifier
                          .size(32.dp)
                          .liquidDrop(interactionSource = cancelInteraction, isPinkTint = true, maxRadius = 18.dp)
                      ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Request", tint = Color.Gray, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                  incomingReq != null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      val acceptInteraction = remember { MutableInteractionSource() }
                      Button(
                        onClick = { viewModel.acceptFriendRequest(incomingReq.id) },
                        interactionSource = acceptInteraction,
                        colors = ButtonDefaults.buttonColors(containerColor = PinggoPinkPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                          .height(34.dp)
                          .liquidDrop(interactionSource = acceptInteraction, isPinkTint = false, maxRadius = 22.dp)
                      ) {
                        Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      }
                      val declineInteraction = remember { MutableInteractionSource() }
                      OutlinedButton(
                        onClick = { viewModel.declineFriendRequest(incomingReq.id) },
                        interactionSource = declineInteraction,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                          .height(34.dp)
                          .liquidDrop(interactionSource = declineInteraction, isPinkTint = true, maxRadius = 22.dp)
                      ) {
                        Text("Decline", fontSize = 11.sp)
                      }
                    }
                  }
                  else -> {
                    val addInteraction = remember { MutableInteractionSource() }
                    Button(
                      onClick = { viewModel.sendFriendRequest(user.uid) },
                      interactionSource = addInteraction,
                      colors = ButtonDefaults.buttonColors(containerColor = PinggoPinkPrimary),
                      shape = RoundedCornerShape(14.dp),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                      modifier = Modifier
                        .height(36.dp)
                        .liquidDrop(interactionSource = addInteraction, isPinkTint = false, maxRadius = 26.dp)
                    ) {
                      Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Add Friend", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // User Profile Preview Modal
    previewUser?.let { user ->
      val isMe = user.uid == myUid
      val isFriend = myFriends.contains(user.uid)
      val isOutgoingPending = outgoingRequests.any { it.receiverId == user.uid && it.status == "pending" }
      val incomingReq = incomingRequests.firstOrNull { it.senderId == user.uid && it.status == "pending" }

      Dialog(onDismissRequest = { viewModel.setPreviewUser(null) }) {
        GlassContainer(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(28.dp),
          elevation = 16.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            GlassAvatar(
              photoUrl = user.photoURL,
              name = user.displayName,
              size = 80.dp,
              isOnline = user.isOnline
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = user.displayName.ifEmpty { user.username },
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = user.handle,
              fontSize = 14.sp,
              color = PinggoPinkPrimary,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = user.bio,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons adhering to friend request status
            when {
              isMe -> {
                Surface(
                  color = PinggoPinkLight.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(16.dp)
                ) {
                  Text(
                    text = "This is your account",
                    color = PinggoPinkPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                  )
                }
              }
              isFriend -> {
                GlassButton(
                  text = "Chat with Friend",
                  onClick = {
                    viewModel.setPreviewUser(null)
                    viewModel.startDirectChat(user) { conv ->
                      onChatReady(conv)
                    }
                  },
                  modifier = Modifier.fillMaxWidth()
                )
              }
              isOutgoingPending -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "Friend request is pending acceptance",
                    color = PinggoPinkPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  GlassButton(
                    text = "Cancel Request",
                    isPrimary = false,
                    onClick = {
                      viewModel.cancelFriendRequest(user.uid)
                      viewModel.setPreviewUser(null)
                    },
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
              incomingReq != null -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  GlassButton(
                    text = "Accept",
                    isPrimary = true,
                    onClick = {
                      viewModel.acceptFriendRequest(incomingReq.id)
                      viewModel.setPreviewUser(null)
                    },
                    modifier = Modifier.weight(1f)
                  )
                  GlassButton(
                    text = "Decline",
                    isPrimary = false,
                    onClick = {
                      viewModel.declineFriendRequest(incomingReq.id)
                      viewModel.setPreviewUser(null)
                    },
                    modifier = Modifier.weight(1f)
                  )
                }
              }
              else -> {
                GlassButton(
                  text = "Add Friend",
                  isPrimary = true,
                  onClick = {
                    viewModel.sendFriendRequest(user.uid)
                    viewModel.setPreviewUser(null)
                  },
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            if (!isMe) {
              Spacer(modifier = Modifier.height(10.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassButton(
                  text = "Block",
                  isPrimary = false,
                  onClick = { viewModel.blockUser(user) },
                  modifier = Modifier.weight(1f)
                )
                GlassButton(
                  text = "Report",
                  isPrimary = false,
                  onClick = { viewModel.reportUser(user, "Spam or misconduct") },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }
      }
    }
  }
}
