package com.example.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin.model.AdminBroadcast
import com.example.admin.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminBroadcastHistoryScreen(
  broadcasts: List<AdminBroadcast>,
  onNewBroadcast: () -> Unit
) {
  var selectedBroadcast by remember { mutableStateOf<AdminBroadcast?>(null) }
  val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Header Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Broadcast History",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = AdminTextPrimary
        )
        Text(
          text = "${broadcasts.size} total announcements dispatched",
          style = MaterialTheme.typography.bodySmall,
          color = AdminTextSecondary
        )
      }

      Button(
        onClick = onNewBroadcast,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AdminPinkPrimary)
      ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Compose")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (broadcasts.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Campaign, null, tint = AdminTextSecondary, modifier = Modifier.size(54.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text("No broadcasts dispatched yet", fontWeight = FontWeight.Bold, color = AdminTextPrimary)
          Text("Announcements you send to users will be logged here.", color = AdminTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(broadcasts, key = { it.id }) { b ->
          val priorityColor = when (b.priority) {
            "critical" -> AdminRuby
            "maintenance" -> AdminAmber
            "update" -> AdminEmerald
            else -> AdminPinkPrimary
          }

          AdminGlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { selectedBroadcast = b }
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  color = priorityColor.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = b.priority.uppercase(),
                    color = priorityColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }

                Text(
                  text = dateFormat.format(Date(b.createdAt)),
                  fontSize = 11.sp,
                  color = AdminTextSecondary
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = b.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AdminTextPrimary
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = b.message,
                style = MaterialTheme.typography.bodyMedium,
                color = AdminTextSecondary,
                maxLines = 2
              )

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Audience: ${b.targetAudience} • ${b.recipientCount} inboxes delivered",
                  fontSize = 11.sp,
                  color = AdminTextSecondary,
                  fontWeight = FontWeight.Medium
                )

                Icon(Icons.Default.ChevronRight, null, tint = AdminTextSecondary, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }

  // Broadcast Detail Dialog
  selectedBroadcast?.let { b ->
    Dialog(onDismissRequest = { selectedBroadcast = null }) {
      AdminGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(22.dp)) {
          Text("Broadcast Dispatch Record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
          Spacer(modifier = Modifier.height(12.dp))

          Text("Title:", fontSize = 11.sp, color = AdminTextSecondary, fontWeight = FontWeight.Bold)
          Text(b.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AdminTextPrimary)
          Spacer(modifier = Modifier.height(8.dp))

          Text("Body Message:", fontSize = 11.sp, color = AdminTextSecondary, fontWeight = FontWeight.Bold)
          Text(b.message, fontSize = 14.sp, color = AdminTextPrimary, lineHeight = 20.sp)
          Spacer(modifier = Modifier.height(12.dp))

          Text("Dispatched By: ${b.senderAdminEmail}", fontSize = 12.sp, color = AdminTextSecondary)
          Text("Timestamp: ${dateFormat.format(Date(b.createdAt))}", fontSize = 12.sp, color = AdminTextSecondary)
          Text("Audience Scope: ${b.targetAudience}", fontSize = 12.sp, color = AdminTextSecondary)
          Text("Confirmed Inboxes: ${b.recipientCount}", fontSize = 12.sp, color = AdminTextSecondary)
          Text("Status: ${b.status.uppercase()}", fontSize = 12.sp, color = AdminEmerald, fontWeight = FontWeight.Bold)

          Spacer(modifier = Modifier.height(18.dp))
          Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { selectedBroadcast = null }) {
              Text("Close")
            }
          }
        }
      }
    }
  }
}
