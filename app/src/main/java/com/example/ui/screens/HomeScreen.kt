package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Conversation
import com.example.model.StatusUpdate
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.components.PinggoHeaderBrand
import com.example.model.User
import com.example.model.handle
import com.example.ui.components.GlassAvatar
import com.example.ui.components.GlassBottomBar
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassBorderSoft
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.ui.theme.PinggoMint
import com.example.ui.theme.PinggoMintUltraLight
import com.example.ui.theme.UnreadBadgeColor
import com.example.viewmodel.PinggoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: PinggoViewModel,
  onOpenChat: (Conversation) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenCreateGroup: () -> Unit,
  onOpenSettings: (String) -> Unit
) {
  var selectedTab by remember { mutableStateOf("Chats") } // "Chats", "Calls", "Updates", "Profile"

  LiquidGlassBackground {
    Scaffold(
      containerColor = Color.Transparent,
      bottomBar = {
        GlassBottomBar {
          HomeNavItem(
            icon = Icons.Default.Chat,
            label = "Chats",
            isSelected = selectedTab == "Chats",
            onClick = { selectedTab = "Chats" }
          )
          HomeNavItem(
            icon = Icons.Default.Call,
            label = "Calls",
            isSelected = selectedTab == "Calls",
            onClick = { selectedTab = "Calls" }
          )
          HomeNavItem(
            icon = Icons.Default.CameraAlt,
            label = "Updates",
            isSelected = selectedTab == "Updates",
            onClick = { selectedTab = "Updates" }
          )
          HomeNavItem(
            icon = Icons.Default.Person,
            label = "Profile",
            isSelected = selectedTab == "Profile",
            onClick = { selectedTab = "Profile" }
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (selectedTab) {
          "Chats" -> ChatsTab(
            viewModel = viewModel,
            onOpenChat = onOpenChat,
            onOpenSearch = onOpenSearch,
            onOpenCreateGroup = onOpenCreateGroup,
            onOpenSettings = onOpenSettings
          )
          "Calls" -> CallsTab(viewModel = viewModel, onOpenSearch = onOpenSearch)
          "Updates" -> UpdatesTab(viewModel = viewModel)
          "Profile" -> ProfileTab(viewModel = viewModel, onOpenSettings = onOpenSettings)
        }
      }
    }
  }
}

@Composable
fun HomeNavItem(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .testTag("nav_item_$label")
  ) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(if (isSelected) Color(0x3510B981) else Color.Transparent)
        .padding(horizontal = 16.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) PinggoMint else Color(0xAA94A3B8),
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) PinggoMint else Color(0xAA94A3B8)
    )
  }
}

@Composable
fun ChatsTab(
  viewModel: PinggoViewModel,
  onOpenChat: (Conversation) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenCreateGroup: () -> Unit,
  onOpenSettings: (String) -> Unit
) {
  val conversations by viewModel.conversations.collectAsState()
  val currentUser by viewModel.userProfile.collectAsState()
  val selectedFilter by viewModel.selectedFilter.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var showMenu by remember { mutableStateOf(false) }

  val myUid = currentUser?.uid ?: ""

  val filteredConversations = remember(conversations, selectedFilter, searchQuery, myUid) {
    var list = when (selectedFilter) {
      "Unread" -> conversations.filter { (it.unreadCounts[myUid] ?: 0) > 0 }
      "Groups" -> conversations.filter { it.type == "group" }
      else -> conversations
    }
    if (searchQuery.isNotEmpty()) {
      list = list.filter {
        it.getTitle(myUid).contains(searchQuery, ignoreCase = true) ||
          it.lastMessage.contains(searchQuery, ignoreCase = true)
      }
    }
    list
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
  ) {
    // Top Bar matching Screen 4
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      PinggoHeaderBrand(
        iconSize = 38.dp,
        fontSize = 24.sp
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        GlassIconButton(
          icon = Icons.Default.Call,
          contentDescription = "Calls",
          onClick = onOpenSearch,
          size = 40.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        GlassIconButton(
          icon = Icons.Default.CameraAlt,
          contentDescription = "Camera",
          onClick = onOpenSearch,
          size = 40.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box {
          GlassIconButton(
            icon = Icons.Default.MoreVert,
            contentDescription = "More",
            onClick = { showMenu = true },
            size = 40.dp
          )
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("New Group") },
              leadingIcon = { Icon(Icons.Default.Group, null) },
              onClick = {
                showMenu = false
                onOpenCreateGroup()
              }
            )
            DropdownMenuItem(
              text = { Text("Search Users") },
              leadingIcon = { Icon(Icons.Default.Search, null) },
              onClick = {
                showMenu = false
                onOpenSearch()
              }
            )
            DropdownMenuItem(
              text = { Text("Settings") },
              leadingIcon = { Icon(Icons.Default.Settings, null) },
              onClick = {
                showMenu = false
                onOpenSettings("general")
              }
            )
          }
        }
      }
    }

    // Search bar matching Screen 4
    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
      GlassSearchBar(
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        placeholder = "Search users, chats..."
      )
    }

    // Filter pills matching Screen 4 ("All", "Unread", "Groups")
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      val filters = listOf("All", "Unread", "Groups")
      items(filters) { filter ->
        val isActive = selectedFilter == filter
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { viewModel.setFilter(filter) },
          shape = RoundedCornerShape(20.dp),
          color = if (isActive) PinggoEmeraldPrimary else Color(0x660E342B),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color(0x80FFFFFF) else DarkGlassBorderSoft
          )
        ) {
          Text(
            text = filter,
            color = if (isActive) Color.White else Color(0xCCFFFFFF),
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Chat list items matching Screen 4
    if (filteredConversations.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Image(
            painter = painterResource(id = R.drawable.ic_pinggo_logo),
            contentDescription = null,
            modifier = Modifier.size(76.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "No conversations yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Tap search or '+' to find registered Pinggo friends!",
            fontSize = 13.sp,
            color = PinggoMintUltraLight
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filteredConversations, key = { it.id }) { conv ->
          val title = conv.getTitle(myUid)
          val avatar = conv.getAvatarUrl(myUid)
          val unread = conv.unreadCounts[myUid] ?: 0

          GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              viewModel.openConversation(conv)
              onOpenChat(conv)
            }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              GlassAvatar(
                photoUrl = avatar,
                name = title,
                size = 52.dp,
                isOnline = conv.type == "direct"
              )

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = formatTimestamp(conv.lastMessageTimestamp),
                    fontSize = 11.sp,
                    color = PinggoMintUltraLight.copy(alpha = 0.8f)
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = conv.lastMessage.ifEmpty { "Start a conversation" },
                    fontSize = 13.sp,
                    color = Color(0xCCFFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                  )

                  if (unread > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                      modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(UnreadBadgeColor),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = if (unread > 9) "9+" else unread.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CallsTab(viewModel: PinggoViewModel, onOpenSearch: () -> Unit) {
  val user by viewModel.userProfile.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      PinggoBubbleIcon(size = 32.dp)
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Calls",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    GlassCard(
      modifier = Modifier.fillMaxWidth(),
      onClick = onOpenSearch
    ) {
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
            .background(Color(0x3510B981)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Phone, null, tint = PinggoMint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Start a Call",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
          Text(
            text = "Select a contact to audio or video call",
            fontSize = 13.sp,
            color = PinggoMintUltraLight
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Recent calls",
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = PinggoMintUltraLight,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    val sampleCalls = listOf(
      Triple("Priya Singh", "Incoming • 5m", false),
      Triple("Aman Kumar", "Outgoing • 12m", true),
      Triple("Family Group", "Missed • 16m", false),
      Triple("Rohit Sharma", "Outgoing • 4m", true)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(sampleCalls) { (name, info, isVideo) ->
        GlassCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            GlassAvatar(photoUrl = null, name = name, size = 48.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                  contentDescription = null,
                  tint = PinggoMint,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = info,
                  fontSize = 12.sp,
                  color = Color(0xCCFFFFFF)
                )
              }
            }
            IconButton(onClick = onOpenSearch) {
              Icon(
                imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Call",
                tint = PinggoMint
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun UpdatesTab(viewModel: PinggoViewModel) {
  val user by viewModel.userProfile.collectAsState()
  val updates by viewModel.statusUpdates.collectAsState()
  var newStatusText by remember { mutableStateOf("") }
  var showPostBox by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      PinggoBubbleIcon(size = 32.dp)
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Updates",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    GlassCard(
      modifier = Modifier.fillMaxWidth(),
      onClick = { showPostBox = !showPostBox }
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        GlassAvatar(
          photoUrl = user?.photoURL,
          name = user?.displayName ?: "Me",
          size = 54.dp,
          hasStatusUpdate = true
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "My Status",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
          Text(
            text = "Tap to add status update",
            fontSize = 13.sp,
            color = PinggoMintUltraLight
          )
        }
        Icon(
          imageVector = Icons.Default.CameraAlt,
          contentDescription = "Post Status",
          tint = PinggoMint
        )
      }
    }

    AnimatedVisibility(visible = showPostBox) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp)
      ) {
        com.example.ui.components.GlassInput(
          value = newStatusText,
          onValueChange = { newStatusText = it },
          placeholder = "What's on your mind?",
          testTag = "status_input"
        )
        Spacer(modifier = Modifier.height(8.dp))
        com.example.ui.components.GlassButton(
          text = "Share Status",
          onClick = {
            if (newStatusText.isNotEmpty()) {
              viewModel.postStatus(newStatusText)
              newStatusText = ""
              showPostBox = false
            }
          },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Recent updates",
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = PinggoMintUltraLight,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(updates, key = { it.id }) { update ->
        GlassCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            GlassAvatar(
              photoUrl = update.userPhoto,
              name = update.userName,
              size = 50.dp,
              hasStatusUpdate = true
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = update.userName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
              Text(
                text = update.text,
                fontSize = 13.sp,
                color = Color(0xCCFFFFFF)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ProfileTab(
  viewModel: PinggoViewModel,
  onOpenSettings: (String) -> Unit
) {
  val user by viewModel.userProfile.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      PinggoBubbleIcon(size = 32.dp)
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Profile",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        GlassAvatar(
          photoUrl = user?.photoURL,
          name = user?.displayName ?: "Pinggo User",
          size = 80.dp,
          isOnline = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = user?.displayName?.ifEmpty { "Pinggo User" } ?: "Pinggo User",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = user?.handle ?: "@username",
          fontSize = 14.sp,
          color = PinggoMint,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = user?.bio?.ifEmpty { "Living the best version of myself ✨" } ?: "Living the best version of myself ✨",
          fontSize = 13.sp,
          color = PinggoMintUltraLight
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val settingsOptions = listOf(
      Triple("Account", Icons.Default.Person, "account"),
      Triple("Privacy", Icons.Default.Lock, "privacy"),
      Triple("Notifications", Icons.Default.Notifications, "notifications"),
      Triple("Appearance", Icons.Default.Settings, "appearance"),
      Triple("About Pinggo", Icons.Default.Chat, "about")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(settingsOptions) { (title, icon, key) ->
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          onClick = { onOpenSettings(key) }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = icon,
              contentDescription = title,
              tint = PinggoMint,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
              text = title,
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White,
              modifier = Modifier.weight(1f)
            )
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = Color(0x88FFFFFF),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(10.dp))
        com.example.ui.components.GlassButton(
          text = "Sign Out",
          isPrimary = false,
          onClick = { viewModel.signOut() },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

fun formatTimestamp(millis: Long): String {
  if (millis <= 0) return ""
  val now = System.currentTimeMillis()
  val diff = now - millis
  return when {
    diff < 60_000 -> "Just now"
    diff < 3600_000 -> "${diff / 60_000}m ago"
    diff < 86400_000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
  }
}
