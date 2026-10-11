package com.example.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.admin.data.AdminRepository
import com.example.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminSettingsScreen(
  repository: AdminRepository,
  currentAdminEmail: String,
  onSignOut: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val maintenanceState by repository.getMaintenanceModeFlow().collectAsState(initial = Pair(false, ""))
  var isMaintenanceEnabled by remember { mutableStateOf(maintenanceState.first) }
  var maintenanceBannerMsg by remember { mutableStateOf(maintenanceState.second) }
  var updateSuccessMsg by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(maintenanceState) {
    isMaintenanceEnabled = maintenanceState.first
    maintenanceBannerMsg = maintenanceState.second.ifEmpty { "Pinggo is undergoing routine server maintenance. Please check back shortly!" }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Admin Profile Card
    AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(AdminPinkPrimary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.VerifiedUser, null, tint = AdminPinkPrimary, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Super Administrator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AdminTextPrimary
          )
          Text(
            text = currentAdminEmail,
            style = MaterialTheme.typography.bodySmall,
            color = AdminPinkPrimary,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Pinggo Core Administration Suite v3.0",
            fontSize = 11.sp,
            color = AdminTextSecondary
          )
        }
      }
    }

    // Platform Maintenance Mode Switch
    AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Maintenance Mode",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = AdminTextPrimary
            )
            Text(
              text = "Displays an alert banner to normal users in the Pinggo app.",
              style = MaterialTheme.typography.bodySmall,
              color = AdminTextSecondary
            )
          }
          Switch(
            checked = isMaintenanceEnabled,
            onCheckedChange = { isMaintenanceEnabled = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = AdminPinkPrimary
            )
          )
        }

        OutlinedTextField(
          value = maintenanceBannerMsg,
          onValueChange = { maintenanceBannerMsg = it },
          label = { Text("Maintenance Banner Message") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        )

        Button(
          onClick = {
            coroutineScope.launch {
              val r = repository.updateMaintenanceMode(currentAdminEmail, isMaintenanceEnabled, maintenanceBannerMsg)
              if (r.isSuccess) {
                updateSuccessMsg = "Maintenance settings updated successfully!"
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AdminPinkPrimary)
        ) {
          Text("Save Maintenance Settings")
        }

        updateSuccessMsg?.let {
          Text(it, color = AdminEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Security & Architecture Setup Guide
    AdminGlassCard(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Info, null, tint = AdminCobalt, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Administrator Authorization Specs",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AdminTextPrimary
          )
        }

        Text(
          text = "• Firebase Project: gen-lang-client-0572544439\n" +
                 "• Authorization Mechanism: Firebase Server Token Claims & Allowlist\n" +
                 "• Primary Super Admin: roshan8929jwn@gmail.com\n" +
                 "• Audit Collection: /admin_audit_logs (Immutable)\n" +
                 "• Broadcast Collection: /admin_broadcasts\n" +
                 "• Client Tamper Resistance: Role values in client documents are never trusted for admin privileges.",
          fontSize = 12.sp,
          color = AdminTextSecondary,
          lineHeight = 18.sp
        )
      }
    }

    // Sign Out Button
    Button(
      onClick = onSignOut,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AdminRuby)
    ) {
      Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Terminate Admin Session & Log Out", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
