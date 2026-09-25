package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.Conversation
import com.example.model.User
import com.example.model.handle
import com.example.ui.components.GlassAvatar
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassInput
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.viewmodel.PinggoViewModel

@Composable
fun CreateGroupScreen(
  viewModel: PinggoViewModel,
  onBack: () -> Unit,
  onGroupCreated: (Conversation) -> Unit
) {
  var groupName by remember { mutableStateOf("") }
  var groupDesc by remember { mutableStateOf("") }
  var memberSearch by remember { mutableStateOf("") }

  val selectedMembers = remember { mutableStateListOf<User>() }
  val searchResults by viewModel.searchResults.collectAsState()

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 16.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(6.dp))
        PinggoBubbleIcon(size = 30.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "New Group",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      GlassInput(
        value = groupName,
        onValueChange = { groupName = it },
        placeholder = "Group Name (e.g. Travel Buddies ✈️)",
        leadingIcon = Icons.Default.Group,
        testTag = "group_name_input"
      )

      Spacer(modifier = Modifier.height(10.dp))

      GlassInput(
        value = groupDesc,
        onValueChange = { groupDesc = it },
        placeholder = "Group Description",
        testTag = "group_desc_input"
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Selected Members Chips
      if (selectedMembers.isNotEmpty()) {
        Text(
          text = "Selected Members (${selectedMembers.size})",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xCCFFFFFF)
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(selectedMembers, key = { it.uid }) { user ->
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(PinggoEmeraldPrimary)
                .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = user.displayName.ifEmpty { user.username },
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier
                  .size(16.dp)
                  .clickable { selectedMembers.remove(user) }
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Member Search
      GlassSearchBar(
        query = memberSearch,
        onQueryChange = {
          memberSearch = it
          viewModel.searchUsers(it)
        },
        placeholder = "Search members to add..."
      )

      Spacer(modifier = Modifier.height(10.dp))

      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(searchResults, key = { it.uid }) { user ->
          val isSelected = selectedMembers.any { it.uid == user.uid }
          GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              if (isSelected) {
                selectedMembers.removeAll { it.uid == user.uid }
              } else {
                selectedMembers.add(user)
              }
            }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              GlassAvatar(photoUrl = user.photoURL, name = user.displayName, size = 44.dp)
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = user.displayName.ifEmpty { user.username },
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
                Text(
                  text = user.handle,
                  fontSize = 12.sp,
                  color = PinggoEmeraldPrimary
                )
              }
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(PinggoEmeraldPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      GlassButton(
        text = "Create Group",
        onClick = {
          viewModel.createGroup(groupName, groupDesc, selectedMembers.toList()) { conv ->
            onGroupCreated(conv)
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      )
    }
  }
}
