package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.GlassAvatar
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassContainer
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoBubbleIcon
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
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
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
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = PinggoPinkPrimary)
        }
      } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
          Text(
            text = "No registered Pinggo users found for \"$searchQuery\"",
            color = Color(0xCCFFFFFF),
            fontSize = 14.sp
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(searchResults, key = { it.uid }) { user ->
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
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = user.displayName.ifEmpty { user.username },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                  )
                  Text(
                    text = user.handle,
                    fontSize = 13.sp,
                    color = PinggoPinkPrimary,
                    fontWeight = FontWeight.Medium
                  )
                }
                Icon(
                  imageVector = Icons.Default.Chat,
                  contentDescription = "Message",
                  tint = PinggoPinkPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }

    // User Profile Preview Modal
    previewUser?.let { user ->
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
              color = Color.White
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
              color = Color(0xCCFFFFFF)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            GlassButton(
              text = "Message",
              onClick = {
                viewModel.setPreviewUser(null)
                viewModel.startDirectChat(user) { conv ->
                  onChatReady(conv)
                }
              },
              modifier = Modifier.fillMaxWidth()
            )

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
