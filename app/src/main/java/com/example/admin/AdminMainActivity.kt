package com.example.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.admin.auth.AdminAuthManager
import com.example.admin.data.AdminRepository
import com.example.admin.ui.AdminScreen
import com.example.admin.ui.screens.*
import com.example.admin.ui.theme.*

class AdminMainActivity : ComponentActivity() {
  private lateinit var authManager: AdminAuthManager
  private lateinit var repository: AdminRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      enableEdgeToEdge()
    } catch (t: Throwable) {
      Log.w("AdminMainActivity", "EdgeToEdge: ${t.message}")
    }

    authManager = AdminAuthManager(applicationContext)
    repository = AdminRepository()

    setContent {
      AdminTheme {
        AdminLiquidBackground {
          val isAuthorized by authManager.isAuthorized.collectAsState()
          val currentAdminUser by authManager.currentAdminUser.collectAsState()
          var currentScreen by remember { mutableStateOf(AdminScreen.DASHBOARD) }

          val allUsers by repository.getAllUsersFlow().collectAsState(initial = emptyList())
          val allReports by repository.getReportsFlow().collectAsState(initial = emptyList())
          val broadcasts by repository.getBroadcastHistoryFlow().collectAsState(initial = emptyList())
          val auditLogs by repository.getAuditLogsFlow().collectAsState(initial = emptyList())

          if (!isAuthorized || currentAdminUser == null) {
            AdminLoginScreen(
              authManager = authManager,
              onLoginSuccess = {
                currentScreen = AdminScreen.DASHBOARD
              }
            )
          } else {
            // Main Admin App Shell
            val adminEmail = currentAdminUser?.email ?: "Admin"

            BackHandler(enabled = currentScreen != AdminScreen.DASHBOARD) {
              currentScreen = AdminScreen.DASHBOARD
            }

            Column(
              modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
            ) {
              // Top Navigation Bar
              AdminAppTopBar(
                adminEmail = adminEmail,
                onSignOut = {
                  authManager.signOut()
                }
              )

              // Navigation Pill Selector
              AdminNavigationBar(
                currentScreen = currentScreen,
                onSelectScreen = { currentScreen = it },
                pendingReports = allReports.count { it.status == "pending" }
              )

              // Main Screen Content
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f)
              ) {
                AnimatedContent(
                  targetState = currentScreen,
                  label = "AdminScreenTransition"
                ) { screen ->
                  when (screen) {
                    AdminScreen.DASHBOARD -> AdminDashboardScreen(
                      repository = repository,
                      allUsers = allUsers,
                      allReports = allReports,
                      broadcasts = broadcasts,
                      auditLogs = auditLogs,
                      onNavigate = { currentScreen = it }
                    )
                    AdminScreen.USERS -> AdminUsersScreen(
                      repository = repository,
                      currentAdminEmail = adminEmail,
                      allUsers = allUsers
                    )
                    AdminScreen.BROADCAST -> AdminBroadcastScreen(
                      repository = repository,
                      currentAdminEmail = adminEmail,
                      onViewHistory = { currentScreen = AdminScreen.BROADCAST_HISTORY }
                    )
                    AdminScreen.BROADCAST_HISTORY -> AdminBroadcastHistoryScreen(
                      broadcasts = broadcasts,
                      onNewBroadcast = { currentScreen = AdminScreen.BROADCAST }
                    )
                    AdminScreen.AUDIT_LOGS -> AdminAuditLogsScreen(
                      auditLogs = auditLogs
                    )
                    AdminScreen.SETTINGS -> AdminSettingsScreen(
                      repository = repository,
                      currentAdminEmail = adminEmail,
                      onSignOut = { authManager.signOut() }
                    )
                    AdminScreen.LOGIN -> {}
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
fun AdminAppTopBar(
  adminEmail: String,
  onSignOut: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(AdminPinkPrimary.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Security, null, tint = AdminPinkPrimary, modifier = Modifier.size(22.dp))
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "Pinggo Admin",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = AdminTextPrimary
      )
      Text(
        text = adminEmail,
        style = MaterialTheme.typography.labelSmall,
        color = AdminTextSecondary,
        fontWeight = FontWeight.Medium
      )
    }

    IconButton(
      onClick = onSignOut,
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.8f))
    ) {
      Icon(Icons.AutoMirrored.Filled.Logout, "Log Out", tint = AdminRuby, modifier = Modifier.size(20.dp))
    }
  }
}

@Composable
fun AdminNavigationBar(
  currentScreen: AdminScreen,
  onSelectScreen: (AdminScreen) -> Unit,
  pendingReports: Int
) {
  val navItems = listOf(
    Triple(AdminScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
    Triple(AdminScreen.USERS, "Users", Icons.Default.People),
    Triple(AdminScreen.BROADCAST, "Broadcast", Icons.Default.Campaign),
    Triple(AdminScreen.BROADCAST_HISTORY, "History", Icons.Default.History),
    Triple(AdminScreen.AUDIT_LOGS, "Audit Logs", Icons.Default.Shield),
    Triple(AdminScreen.SETTINGS, "Settings", Icons.Default.Settings)
  )

  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(navItems) { (screen, title, icon) ->
      val isSelected = currentScreen == screen
      val interactionSource = remember { MutableInteractionSource() }

      Surface(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = AdminPinkPrimary)
          ) { onSelectScreen(screen) },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) AdminPinkPrimary.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isSelected) AdminPinkPrimary.copy(alpha = 0.5f) else Color.White
        ),
        shadowElevation = if (isSelected) 0.dp else 2.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AdminPinkPrimary else AdminTextSecondary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = title,
            color = if (isSelected) AdminPinkPrimary else AdminTextPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
          )
        }
      }
    }
  }
}
