package com.example.admin.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.model.AdminAuditLog
import com.example.admin.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminAuditLogsScreen(
  auditLogs: List<AdminAuditLog>
) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Header
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(AdminPinkPrimary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.SecurityUpdateGood, null, tint = AdminPinkPrimary, modifier = Modifier.size(24.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "Security Audit Trail",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = AdminTextPrimary
        )
        Text(
          text = "Immutable server-protected records of administrative events",
          style = MaterialTheme.typography.bodySmall,
          color = AdminTextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (auditLogs.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.HistoryEdu, null, tint = AdminTextSecondary, modifier = Modifier.size(52.dp))
          Spacer(modifier = Modifier.height(10.dp))
          Text("No audit log events recorded yet", fontWeight = FontWeight.Bold, color = AdminTextPrimary)
          Text("Administrative operations will appear here as they occur.", color = AdminTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(auditLogs, key = { it.id }) { log ->
          val actionColor = when (log.actionType) {
            "USER_BANNED", "USER_DELETED" -> AdminRuby
            "USER_VERIFIED" -> AdminCobalt
            "BROADCAST_CREATED" -> AdminPinkPrimary
            "ADMIN_LOGIN" -> AdminEmerald
            else -> AdminAmber
          }

          AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  color = actionColor.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = log.actionType,
                    color = actionColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }

                Text(
                  text = dateFormat.format(Date(log.timestamp)),
                  fontSize = 11.sp,
                  color = AdminTextSecondary
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = log.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AdminTextPrimary
              )

              if (log.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = log.details,
                  style = MaterialTheme.typography.bodySmall,
                  color = AdminTextSecondary
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = AdminTextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Actor: ${log.adminEmail}",
                  fontSize = 11.sp,
                  color = AdminTextSecondary,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }
  }
}
