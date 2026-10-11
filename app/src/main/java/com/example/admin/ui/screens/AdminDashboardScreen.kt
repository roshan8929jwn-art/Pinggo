package com.example.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.data.AdminRepository
import com.example.admin.model.AdminAuditLog
import com.example.admin.model.AdminBroadcast
import com.example.admin.ui.AdminScreen
import com.example.admin.ui.theme.*
import com.example.model.User
import com.example.model.UserReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
  repository: AdminRepository,
  allUsers: List<User>,
  allReports: List<UserReport>,
  broadcasts: List<AdminBroadcast>,
  auditLogs: List<AdminAuditLog>,
  onNavigate: (AdminScreen) -> Unit
) {
  val totalUsers = allUsers.size
  val onlineUsers = allUsers.count { it.isOnline }
  val bannedUsers = allUsers.count { it.isBanned }
  val verifiedUsers = allUsers.count { it.isVerified }
  val pendingReports = allReports.count { it.status == "pending" }
  val totalBroadcasts = broadcasts.size

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(16.dp)
  ) {
    // Top Welcome & Platform Status
    item {
      AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(AdminEmerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.CloudDone, null, tint = AdminEmerald, modifier = Modifier.size(28.dp))
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Pinggo Cloud Connected",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = AdminTextPrimary
            )
            Text(
              text = "Project: gen-lang-client-0572544439 • Firestore Live",
              style = MaterialTheme.typography.bodySmall,
              color = AdminTextSecondary
            )
          }
        }
      }
    }

    // Key Metrics Grid
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AdminStatSummaryCard(
          title = "Total Users",
          value = totalUsers.toString(),
          subtitle = "$onlineUsers Currently Online",
          icon = Icons.Default.People,
          accentColor = AdminPinkPrimary,
          modifier = Modifier.weight(1f),
          onClick = { onNavigate(AdminScreen.USERS) }
        )
        AdminStatSummaryCard(
          title = "Verified Accounts",
          value = verifiedUsers.toString(),
          subtitle = "$bannedUsers Banned Accounts",
          icon = Icons.Default.Verified,
          accentColor = AdminCobalt,
          modifier = Modifier.weight(1f),
          onClick = { onNavigate(AdminScreen.USERS) }
        )
      }
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AdminStatSummaryCard(
          title = "Pending Reports",
          value = pendingReports.toString(),
          subtitle = "${allReports.size} Total Tickets",
          icon = Icons.Default.ReportProblem,
          accentColor = if (pendingReports > 0) AdminRuby else AdminEmerald,
          modifier = Modifier.weight(1f),
          onClick = { /* View reports */ }
        )
        AdminStatSummaryCard(
          title = "Broadcasts Sent",
          value = totalBroadcasts.toString(),
          subtitle = "Delivered to inboxes",
          icon = Icons.Default.Campaign,
          accentColor = AdminAmber,
          modifier = Modifier.weight(1f),
          onClick = { onNavigate(AdminScreen.BROADCAST_HISTORY) }
        )
      }
    }

    // Quick Action Cards
    item {
      Text(
        text = "Quick Administrative Actions",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = AdminTextPrimary
      )
    }

    item {
      AdminGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigate(AdminScreen.BROADCAST) }
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(AdminPinkPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Send, null, tint = AdminPinkPrimary, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Create New Broadcast",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Bold,
              color = AdminTextPrimary
            )
            Text(
              text = "Compose & blast announcements to all active users",
              style = MaterialTheme.typography.bodySmall,
              color = AdminTextSecondary
            )
          }
          Icon(Icons.Default.ChevronRight, null, tint = AdminTextSecondary)
        }
      }
    }

    item {
      AdminGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigate(AdminScreen.USERS) }
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(AdminCobalt.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ManageAccounts, null, tint = AdminCobalt, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Moderate User Accounts",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Bold,
              color = AdminTextPrimary
            )
            Text(
              text = "Search, review, ban/unban, or verify users",
              style = MaterialTheme.typography.bodySmall,
              color = AdminTextSecondary
            )
          }
          Icon(Icons.Default.ChevronRight, null, tint = AdminTextSecondary)
        }
      }
    }

    item {
      AdminGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigate(AdminScreen.AUDIT_LOGS) }
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color(0xFF64748B).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.History, null, tint = Color(0xFF64748B), modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Audit Log Trail",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Bold,
              color = AdminTextPrimary
            )
            Text(
              text = "Inspect tamper-proof records of admin actions",
              style = MaterialTheme.typography.bodySmall,
              color = AdminTextSecondary
            )
          }
          Icon(Icons.Default.ChevronRight, null, tint = AdminTextSecondary)
        }
      }
    }

    // Recent Audit Logs Preview
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Security Events",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = AdminTextPrimary
        )
        TextButton(onClick = { onNavigate(AdminScreen.AUDIT_LOGS) }) {
          Text("View All (${auditLogs.size})", color = AdminPinkPrimary)
        }
      }
    }

    if (auditLogs.isEmpty()) {
      item {
        AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text("No recent security events recorded.", color = AdminTextSecondary)
          }
        }
      }
    } else {
      item {
        val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          auditLogs.take(3).forEach { log ->
            AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Shield, null, tint = AdminPinkPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AdminTextPrimary
                  )
                  Text(
                    text = "${log.adminEmail} • ${dateFormat.format(Date(log.timestamp))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AdminTextSecondary
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

@Composable
fun AdminStatSummaryCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  AdminGlassCard(
    modifier = modifier.clickable { onClick() }
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
        }
        Icon(Icons.Default.ArrowOutward, null, tint = AdminTextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = AdminTextPrimary
      )
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = AdminTextPrimary
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = AdminTextSecondary
      )
    }
  }
}

@Composable
fun AdminGlassCard(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  content: @Composable () -> Unit
) {
  AdminGlassCard(
    modifier = modifier.clickable { onClick() }
  ) {
    content()
  }
}
