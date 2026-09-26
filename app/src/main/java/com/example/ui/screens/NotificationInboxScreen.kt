package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Notification
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PinggoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationInboxScreen(
    viewModel: PinggoViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()

    LiquidGlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearNotifications() }) {
                            Icon(Icons.Default.DeleteSweep, "Clear all", tint = PinggoPinkPrimary)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (notifications.isEmpty() && friendRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = PinggoPinkPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Inbox Empty",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "You'll see requests and alerts here",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Friend Requests Section
                        if (friendRequests.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Friend Requests")
                            }
                            items(friendRequests, key = { "req_${it.id}" }) { request ->
                                FriendRequestItem(
                                    request = request,
                                    onAccept = { viewModel.acceptFriendRequest(request.id) },
                                    onDecline = { viewModel.declineFriendRequest(request.id) }
                                )
                            }
                        }

                        // Notifications Section
                        if (notifications.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SectionHeader(title = "General")
                            }
                            items(notifications, key = { "notif_${it.id}" }) { notif ->
                                NotificationItem(
                                    notification = notif,
                                    onClick = { viewModel.markNotificationAsRead(notif.id) }
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
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = PinggoPinkPrimary,
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
    )
}

@Composable
fun FriendRequestItem(
    request: com.example.model.FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassAvatar(photoUrl = request.senderPhoto, name = request.senderName, size = 52.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.senderName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@${request.senderUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PinggoPinkPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = PinggoPinkPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Accept", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Decline", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val alpha = if (notification.isRead) 0.6f else 1.0f
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                GlassAvatar(photoUrl = notification.senderPhoto, name = notification.senderName, size = 44.dp)
                val icon = when (notification.type) {
                    "request_accepted" -> Icons.Default.CheckCircle
                    "status_like" -> Icons.Default.Favorite
                    "login_alert" -> Icons.Default.Security
                    else -> Icons.Default.Info
                }
                val iconColor = when (notification.type) {
                    "request_accepted" -> PinggoMint
                    "status_like" -> PinggoPinkPrimary
                    "login_alert" -> Color.Red
                    else -> Color.Gray
                }
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PinggoPinkPrimary)
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { time = date }
    
    return if (now.get(Calendar.DATE) == time.get(Calendar.DATE)) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    }
}
