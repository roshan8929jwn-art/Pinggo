package com.example.admin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin.data.AdminRepository
import com.example.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminBroadcastScreen(
  repository: AdminRepository,
  currentAdminEmail: String,
  onViewHistory: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()

  var title by remember { mutableStateOf("") }
  var message by remember { mutableStateOf("") }
  var selectedAudience by remember { mutableStateOf("all") } // "all", "verified", "test_preview"
  var selectedPriority by remember { mutableStateOf("general") } // "general", "maintenance", "update", "critical"

  var isSending by remember { mutableStateOf(false) }
  var showPreviewModal by remember { mutableStateOf(false) }
  var showConfirmModal by remember { mutableStateOf(false) }
  var broadcastSuccessMessage by remember { mutableStateOf<String?>(null) }
  var broadcastErrorMessage by remember { mutableStateOf<String?>(null) }

  val maxTitleLength = 80
  val maxMessageLength = 500

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Info
    AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(AdminPinkPrimary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Campaign, null, tint = AdminPinkPrimary, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Platform Announcement Broadcast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AdminTextPrimary
          )
          Text(
            text = "Dispatches push alerts and notification records to user inboxes.",
            style = MaterialTheme.typography.bodySmall,
            color = AdminTextSecondary
          )
        }
      }
    }

    // Success / Error Alerts
    AnimatedVisibility(visible = broadcastSuccessMessage != null) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AdminEmerald.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminEmerald.copy(alpha = 0.3f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, null, tint = AdminEmerald, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(broadcastSuccessMessage ?: "", color = AdminEmerald, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.weight(1f))
          TextButton(onClick = onViewHistory) {
            Text("View History", color = AdminEmerald, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    AnimatedVisibility(visible = broadcastErrorMessage != null) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AdminRuby.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminRuby.copy(alpha = 0.3f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.ErrorOutline, null, tint = AdminRuby, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(broadcastErrorMessage ?: "", color = AdminRuby, style = MaterialTheme.typography.bodySmall)
        }
      }
    }

    // Broadcast Composition Card
    AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
          text = "Compose Announcement",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = AdminTextPrimary
        )

        // Title Input
        OutlinedTextField(
          value = title,
          onValueChange = {
            if (it.length <= maxTitleLength) title = it
            broadcastSuccessMessage = null
            broadcastErrorMessage = null
          },
          label = { Text("Announcement Title") },
          placeholder = { Text("e.g., Scheduled Maintenance / New Features") },
          supportingText = { Text("${title.length}/$maxTitleLength characters") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )

        // Message Input
        OutlinedTextField(
          value = message,
          onValueChange = {
            if (it.length <= maxMessageLength) message = it
            broadcastSuccessMessage = null
            broadcastErrorMessage = null
          },
          label = { Text("Announcement Body") },
          placeholder = { Text("Enter detailed notification message for Pinggo users...") },
          supportingText = { Text("${message.length}/$maxMessageLength characters") },
          singleLine = false,
          minLines = 4,
          maxLines = 8,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )

        // Target Audience Selection
        Text(
          text = "Target Audience",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = AdminTextPrimary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val audiences = listOf(
            "all" to "All Active Users",
            "verified" to "Verified Only",
            "test_preview" to "Admin Preview Only"
          )
          audiences.forEach { (key, label) ->
            val isSelected = selectedAudience == key
            FilterChip(
              selected = isSelected,
              onClick = { selectedAudience = key },
              label = { Text(label, fontSize = 12.sp) },
              shape = RoundedCornerShape(14.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AdminPinkPrimary.copy(alpha = 0.15f),
                selectedLabelColor = AdminPinkPrimary
              )
            )
          }
        }

        // Priority Level Selection
        Text(
          text = "Announcement Priority",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = AdminTextPrimary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val priorities = listOf(
            "general" to "General ℹ️",
            "update" to "Feature ✨",
            "maintenance" to "Maintenance ⚠️",
            "critical" to "Critical 🚨"
          )
          priorities.forEach { (key, label) ->
            val isSelected = selectedPriority == key
            FilterChip(
              selected = isSelected,
              onClick = { selectedPriority = key },
              label = { Text(label, fontSize = 12.sp) },
              shape = RoundedCornerShape(14.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AdminPinkPrimary.copy(alpha = 0.15f),
                selectedLabelColor = AdminPinkPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = { showPreviewModal = true },
            enabled = title.isNotBlank() && message.isNotBlank(),
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Preview")
          }

          Button(
            onClick = { showConfirmModal = true },
            enabled = title.isNotBlank() && message.isNotBlank() && !isSending,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminPinkPrimary)
          ) {
            if (isSending) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Send Broadcast", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Preview Modal
  if (showPreviewModal) {
    Dialog(onDismissRequest = { showPreviewModal = false }) {
      AdminGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(22.dp)) {
          Text("User Notification Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text("This is how your broadcast will appear in users' notification inbox:", style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
          Spacer(modifier = Modifier.height(16.dp))

          Surface(
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 4.dp
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AdminPinkPrimary.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Campaign, null, tint = AdminPinkPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("Pinggo Announcement", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AdminTextPrimary)
                  Text("Just now", fontSize = 11.sp, color = AdminTextSecondary)
                }
                Surface(
                  color = AdminPinkPrimary.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(selectedPriority.uppercase(), color = AdminPinkPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AdminTextPrimary)
              Spacer(modifier = Modifier.height(4.dp))
              Text(message, fontSize = 13.sp, color = AdminTextSecondary, lineHeight = 18.sp)
            }
          }

          Spacer(modifier = Modifier.height(18.dp))
          Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { showPreviewModal = false }) {
              Text("Close Preview")
            }
          }
        }
      }
    }
  }

  // Confirmation Modal
  if (showConfirmModal) {
    Dialog(onDismissRequest = { showConfirmModal = false }) {
      AdminGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(22.dp)) {
          Text("Confirm Broadcast Send", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Are you sure you want to broadcast '$title' to target audience '$selectedAudience'? This notification will be saved into users' inboxes immediately.",
            style = MaterialTheme.typography.bodyMedium,
            color = AdminTextSecondary
          )
          Spacer(modifier = Modifier.height(18.dp))
          Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { showConfirmModal = false }) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                showConfirmModal = false
                isSending = true
                coroutineScope.launch {
                  val result = repository.createAndSendBroadcast(
                    adminEmail = currentAdminEmail,
                    title = title,
                    message = message,
                    targetAudience = selectedAudience,
                    priority = selectedPriority
                  )
                  isSending = false
                  if (result.isSuccess) {
                    val count = result.getOrNull()?.recipientCount ?: 0
                    broadcastSuccessMessage = "Broadcast successfully delivered to $count users!"
                    title = ""
                    message = ""
                  } else {
                    broadcastErrorMessage = result.exceptionOrNull()?.message ?: "Failed to dispatch broadcast."
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = AdminPinkPrimary)
            ) {
              Text("Confirm & Blast 🚀")
            }
          }
        }
      }
    }
  }
}
