package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Conversation
import com.example.model.StatusUpdate
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.components.PinggoHeaderBrand
import com.example.model.User
import com.example.ui.components.GlassAvatar
import com.example.ui.components.GlassBottomBar
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.LiquidGlassBackground
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.*
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
  onOpenSettings: (String) -> Unit,
  initialTab: String = "Chats"
) {
  var selectedTab by rememberSaveable { mutableStateOf(initialTab) } // "Chats", "Calls", "Updates", "Profile"

  // Android system Back button handler:
  // When on Calls, Updates, or Profile screen, pressing Back returns directly to the main Chat screen (Chat List) in one press.
  // When on Chats screen (main destination), BackHandler is disabled to allow normal activity lifecycle.
  BackHandler(enabled = selectedTab != "Chats") {
    selectedTab = "Chats"
  }

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
        .background(if (isSelected) PinggoPinkLight.copy(alpha = 0.3f) else Color.Transparent)
        .padding(horizontal = 16.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) PinggoPinkPrimary else Color.Gray,
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) PinggoPinkPrimary else Color.Gray
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .testTag("chats_tab_screen")
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
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
          color = if (isActive) PinggoPinkPrimary else Color.White.copy(alpha = 0.1f),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color.White.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.2f)
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

    // Floating Add Username button near bottom-right corner
    Surface(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 18.dp, bottom = 18.dp)
        .clip(RoundedCornerShape(26.dp))
        .clickable { onOpenSearch() }
        .testTag("add_username_button"),
      shape = RoundedCornerShape(26.dp),
      color = Color.White,
      border = androidx.compose.foundation.BorderStroke(1.2.dp, PinggoPinkPrimary),
      shadowElevation = 10.dp
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.PersonAdd,
          contentDescription = "Add Username",
          tint = PinggoPinkPrimary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Add Username",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color.White
        )
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
      .testTag("calls_tab_screen")
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
            .background(PinggoPinkLight.copy(alpha = 0.3f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Phone, null, tint = PinggoPinkPrimary, modifier = Modifier.size(22.dp))
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

    val callHistory by viewModel.callHistory.collectAsState()
    val myUid = user?.uid ?: ""

    if (callHistory.isEmpty()) {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = null,
            tint = PinggoPinkPrimary,
            modifier = Modifier.size(44.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No calls yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Voice and video calls with your contacts will appear here",
            fontSize = 13.sp,
            color = PinggoMintUltraLight,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(callHistory, key = { it.id }) { call ->
          val isOutgoing = call.callerId == myUid
          val otherName = if (isOutgoing) call.receiverName else call.callerName
          val otherPhoto = if (isOutgoing) call.receiverPhoto else call.callerPhoto
          val isVideo = call.type == "video"
          val statusText = when (call.status) {
            "missed" -> "Missed call"
            "rejected" -> "Declined"
            "completed", "ended" -> if (call.durationSec > 0) "${call.durationSec / 60}m ${call.durationSec % 60}s" else "Completed"
            else -> if (isOutgoing) "Outgoing" else "Incoming"
          }
          val timeText = formatTimestamp(call.createdAt)

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              GlassAvatar(photoUrl = otherPhoto, name = otherName, size = 48.dp)
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = otherName.ifEmpty { "Pinggo Contact" },
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = null,
                    tint = if (call.status == "missed") Color(0xFFFF6B6B) else PinggoMint,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$statusText • $timeText",
                    fontSize = 12.sp,
                    color = Color(0xCCFFFFFF)
                  )
                }
              }
              IconButton(onClick = onOpenSearch) {
                Icon(
                  imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                  contentDescription = "Call",
                  tint = PinggoPinkPrimary
                )
              }
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

  // Status view / viewers state
  var viewingStatus by remember { mutableStateOf<StatusUpdate?>(null) }
  var viewingStatusViewersFor by remember { mutableStateOf<StatusUpdate?>(null) }
  var viewersList by remember { mutableStateOf<List<com.example.model.StatusViewer>>(emptyList()) }
  var isLoadingViewers by remember { mutableStateOf(false) }

  val myUid = user?.uid ?: ""
  val myUpdates = remember(updates, myUid) { updates.filter { it.userId == myUid } }
  val otherUpdates = remember(updates, myUid) { updates.filter { it.userId != myUid } }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp)
      .testTag("updates_tab_screen")
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

    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
      if (uri != null) {
        selectedMediaUri = uri
        val mimeType = viewModel.getApplication<android.app.Application>().contentResolver.getType(uri)
        isVideo = mimeType?.startsWith("video") == true
        showPostBox = true
      }
    }

    // My Status Card
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
          hasStatusUpdate = myUpdates.isNotEmpty()
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "My Status",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
          )
          Text(
            text = if (myUpdates.isNotEmpty()) "Tap to update or view status" else "Tap to add status update",
            fontSize = 13.sp,
            color = Color.Gray
          )
        }
        Row {
          IconButton(onClick = {
            mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
          }) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Post Status",
              tint = PinggoPinkPrimary
            )
          }
        }
      }
    }

    AnimatedVisibility(visible = showPostBox) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp)
      ) {
        if (selectedMediaUri != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color.Black.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            if (isVideo) {
              Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
              Text("Video Selected", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
            } else {
              AsyncImage(
                model = selectedMediaUri,
                contentDescription = "Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
              )
            }
            IconButton(
              onClick = { selectedMediaUri = null },
              modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
              Icon(Icons.Default.Close, null, tint = Color.White)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        com.example.ui.components.GlassInput(
          value = newStatusText,
          onValueChange = { newStatusText = it },
          placeholder = "What's on your mind?",
          testTag = "status_input"
        )
        Spacer(modifier = Modifier.height(8.dp))
        com.example.ui.components.GlassButton(
          text = if (isUploading) "Uploading..." else "Share Status",
          isLoading = isUploading,
          onClick = {
            if (selectedMediaUri != null) {
              viewModel.uploadStatusMedia(selectedMediaUri!!, isVideo, newStatusText, { uploading ->
                isUploading = uploading
              }, { success ->
                if (success) {
                  newStatusText = ""
                  selectedMediaUri = null
                  showPostBox = false
                }
              })
            } else if (newStatusText.isNotEmpty()) {
              viewModel.postStatus(newStatusText)
              newStatusText = ""
              showPostBox = false
            }
          },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // If user has status updates, show Status Seen & Viewers affordance
    if (myUpdates.isNotEmpty()) {
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "My active status updates",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = PinggoMintUltraLight,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        myUpdates.forEach { myUpdate ->
          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = myUpdate.text,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.White,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = formatTimestamp(myUpdate.timestamp),
                  fontSize = 11.sp,
                  color = PinggoMintUltraLight
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Real Status Seen Viewers Button
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    viewingStatusViewersFor = myUpdate
                    isLoadingViewers = true
                    viewModel.loadStatusViewers(myUpdate.id) { list ->
                      viewersList = list
                      isLoadingViewers = false
                    }
                  }
                  .testTag("view_status_viewers_button"),
                shape = RoundedCornerShape(16.dp),
                color = PinggoPinkLight.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PinggoPinkPrimary.copy(alpha = 0.4f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Viewers",
                    tint = PinggoPinkPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${myUpdate.viewers.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
            }
          }
        }
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

    if (otherUpdates.isEmpty()) {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No recent updates from contacts",
            fontSize = 13.sp,
            color = Color(0xCCFFFFFF)
          )
        }
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(otherUpdates, key = { it.id }) { update ->
          GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              viewModel.recordStatusView(update.id)
              viewingStatus = update
            }
          ) {
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
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = update.userName,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
                Text(
                  text = update.text,
                  fontSize = 13.sp,
                  color = Color(0xCCFFFFFF),
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = formatTimestamp(update.timestamp),
                  fontSize = 11.sp,
                  color = PinggoMintUltraLight
                )
              }
            }
          }
        }
      }
    }
  }

  // Viewing someone else's status detail dialog
  viewingStatus?.let { status ->
    StatusDetailDialog(
      status = status,
      onDismiss = { viewingStatus = null }
    )
  }

  // Viewing real Status Seen Viewers dialog
  viewingStatusViewersFor?.let { status ->
    StatusViewersDialog(
      status = status,
      viewers = viewersList,
      isLoading = isLoadingViewers,
      onDismiss = { viewingStatusViewersFor = null }
    )
  }
}

@Composable
fun StatusDetailDialog(
  status: StatusUpdate,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    com.example.ui.components.GlassContainer(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(28.dp),
      elevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          GlassAvatar(
            photoUrl = status.userPhoto,
            name = status.userName,
            size = 46.dp,
            hasStatusUpdate = true
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = status.userName,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = formatTimestamp(status.timestamp),
              fontSize = 12.sp,
              color = PinggoMintUltraLight
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (status.imageUrl.isNotEmpty()) {
          AsyncImage(
            model = status.imageUrl,
            contentDescription = null,
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 240.dp)
              .clip(RoundedCornerShape(18.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
          )
          Spacer(modifier = Modifier.height(14.dp))
        }

        Text(
          text = status.text,
          fontSize = 15.sp,
          color = Color.White,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        com.example.ui.components.GlassButton(
          text = "Close",
          isPrimary = false,
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun StatusViewersDialog(
  status: StatusUpdate,
  viewers: List<com.example.model.StatusViewer>,
  isLoading: Boolean,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    com.example.ui.components.GlassContainer(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(28.dp),
      elevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Visibility,
              contentDescription = null,
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Viewed by ${viewers.size}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = PinggoPinkPrimary, modifier = Modifier.size(32.dp))
          }
        } else if (viewers.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No one has viewed this status yet",
              fontSize = 13.sp,
              color = Color(0xCCFFFFFF)
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(viewers, key = { it.uid }) { viewer ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(PinggoPinkLight.copy(alpha = 0.2f))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                GlassAvatar(
                  photoUrl = viewer.photoURL,
                  name = viewer.displayName,
                  size = 40.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = viewer.displayName.ifEmpty { viewer.username },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                  )
                  Text(
                    text = "@${viewer.username}",
                    fontSize = 12.sp,
                    color = PinggoMint
                  )
                }
                Text(
                  text = formatTimestamp(viewer.viewedAt),
                  fontSize = 11.sp,
                  color = PinggoMintUltraLight
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        com.example.ui.components.GlassButton(
          text = "Done",
          isPrimary = false,
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth()
        )
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
  var showEditProfileDialog by remember { mutableStateOf(false) }

  var showSignOutDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 20.dp)
      .testTag("profile_tab_screen")
  ) {
    // ... (rest of the profile header)
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
          text = "@${user?.username ?: "username"}",
          fontSize = 14.sp,
          color = PinggoPinkPrimary,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = user?.bio?.ifEmpty { "Living the best version of myself ✨" } ?: "Living the best version of myself ✨",
          fontSize = 13.sp,
          color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Working Edit Profile Button
        com.example.ui.components.GlassButton(
          text = "Edit Profile",
          icon = Icons.Default.Edit,
          isPrimary = true,
          onClick = { showEditProfileDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_profile_button")
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val settingsOptions = listOf(
      Triple("Account (Edit Profile)", Icons.Default.Person, "account"),
      Triple("Privacy", Icons.Default.Lock, "privacy"),
      Triple("Notifications", Icons.Default.Notifications, "notifications"),
      Triple("Appearance", Icons.Default.Settings, "appearance"),
      Triple("About Pinggo", Icons.Default.Chat, "about")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(settingsOptions) { (title, icon, key) ->
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            if (key == "account") {
              showEditProfileDialog = true
            } else {
              onOpenSettings(key)
            }
          }
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
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
              text = title,
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
              color = Color.Black,
              modifier = Modifier.weight(1f)
            )
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = Color.Gray,
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
          onClick = { showSignOutDialog = true },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  if (showEditProfileDialog) {
    EditProfileDialog(
      viewModel = viewModel,
      user = user,
      onDismiss = { showEditProfileDialog = false }
    )
  }

  if (showSignOutDialog) {
    SignOutConfirmationDialog(
      onConfirm = {
        showSignOutDialog = false
        viewModel.signOut()
      },
      onDismiss = { showSignOutDialog = false }
    )
  }
}

@Composable
fun SignOutConfirmationDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    com.example.ui.components.GlassContainer(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(28.dp),
      elevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Are you sure you want to log out your account?",
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = Color.Black,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          com.example.ui.components.GlassButton(
            text = "NO",
            isPrimary = false,
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
          )
          com.example.ui.components.GlassButton(
            text = "YES",
            isPrimary = true,
            onClick = onConfirm,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}


@Composable
fun EditProfileDialog(
  viewModel: PinggoViewModel,
  user: User?,
  onDismiss: () -> Unit
) {
  val isLoading by viewModel.isAuthLoading.collectAsState()
  var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
  var username by remember(user) { mutableStateOf(user?.username ?: "") }
  var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
  var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val usernameCheckState by viewModel.usernameCheckState.collectAsState()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      pickedImageUri = uri
    }
  }

  Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
    com.example.ui.components.GlassContainer(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp),
      shape = RoundedCornerShape(28.dp),
      elevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Title Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Edit Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Picture with change overlay
        Box(contentAlignment = Alignment.BottomEnd) {
          if (pickedImageUri != null) {
            AsyncImage(
              model = pickedImageUri,
              contentDescription = "Profile Picture",
              modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .border(2.dp, PinggoMint, CircleShape),
              contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
          } else {
            GlassAvatar(
              photoUrl = user?.photoURL,
              name = displayName.ifEmpty { "Pinggo" },
              size = 90.dp,
              isOnline = true
            )
          }

          // Camera badge button
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(PinggoPinkPrimary)
              .clickable {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }
              .testTag("change_profile_photo_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Change Profile Picture",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Change Profile Picture",
          fontSize = 12.sp,
          color = PinggoMint,
          fontWeight = FontWeight.Medium,
          modifier = Modifier
            .clickable {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
            .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display Name input
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Display Name",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PinggoMintUltraLight,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
          )
          com.example.ui.components.GlassInput(
            value = displayName,
            onValueChange = { displayName = it },
            placeholder = "Enter your display name",
            testTag = "edit_display_name_input"
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Username input with validation
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Username",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PinggoMintUltraLight,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
          )
          com.example.ui.components.GlassInput(
            value = username,
            onValueChange = { input ->
              val sanitized = input.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' }
              username = sanitized
              if (sanitized != (user?.username ?: "") && sanitized.length in 3..30) {
                viewModel.checkUsername(sanitized)
              }
            },
            placeholder = "username (lowercase, numbers, _)",
            testTag = "edit_username_input"
          )

          // Username availability feedback
          val currentUname = user?.username ?: ""
          if (username.isNotEmpty() && username != currentUname) {
            val isValidFormat = username.matches(Regex("^[a-z0-9_]{3,30}$"))
            if (!isValidFormat) {
              Text(
                text = "3-30 chars: lowercase letters, numbers, and _ only",
                fontSize = 11.sp,
                color = Color(0xFFFF6B6B),
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
              )
            } else if (usernameCheckState == true) {
              Text(
                text = "✓ @$username is available",
                fontSize = 11.sp,
                color = PinggoMint,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
              )
            } else if (usernameCheckState == false) {
              Text(
                text = "✗ @$username is already taken",
                fontSize = 11.sp,
                color = Color(0xFFFF6B6B),
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bio input
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Bio",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PinggoMintUltraLight,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
          )
          com.example.ui.components.GlassInput(
            value = bio,
            onValueChange = { bio = it },
            placeholder = "Living the best version of myself ✨",
            testTag = "edit_bio_input"
          )
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = errorMessage ?: "",
            fontSize = 12.sp,
            color = Color(0xFFFF6B6B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Liquid Glass Save Changes button
        com.example.ui.components.GlassButton(
          text = if (isLoading) "Saving Changes..." else "Save Changes",
          isPrimary = true,
          isLoading = isLoading,
          onClick = {
            if (displayName.trim().isEmpty()) {
              errorMessage = "Display name cannot be empty"
              return@GlassButton
            }
            if (username.trim().isEmpty() || !username.matches(Regex("^[a-z0-9_]{3,30}$"))) {
              errorMessage = "Username must be 3-30 characters (letters, numbers, _)"
              return@GlassButton
            }
            if (username != (user?.username ?: "") && usernameCheckState == false) {
              errorMessage = "Username is already taken by another user"
              return@GlassButton
            }
            errorMessage = null
            viewModel.updateProfile(
              displayName = displayName,
              username = username,
              bio = bio,
              photoUri = pickedImageUri
            ) { success, err ->
              if (success) {
                onDismiss()
              } else {
                errorMessage = err ?: "Failed to save profile changes"
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_profile_button")
        )
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
