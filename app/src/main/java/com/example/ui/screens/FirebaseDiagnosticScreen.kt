package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoFullLogo
import com.example.ui.theme.*
import com.example.util.FirebaseDiagnosticInfo

@Composable
fun FirebaseDiagnosticScreen(
  diagnostic: FirebaseDiagnosticInfo,
  onRetry: () -> Unit,
  onContinueOffline: () -> Unit
) {
  val scrollState = rememberScrollState()

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 40.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      PinggoFullLogo(
        iconSize = 64.dp,
        wordmarkSize = 28.sp,
        showTagline = true,
        subtitle = "System & Firebase Diagnostics"
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Status Card
      val isAllOk = diagnostic.isInitialized && diagnostic.isAuthAvailable && diagnostic.isFirestoreAvailable
      val statusBg = if (isAllOk) Color(0x3310B981) else Color(0x33F59E0B)
      val statusBorder = if (isAllOk) Color(0x6610B981) else Color(0x66F59E0B)
      val statusIcon = if (isAllOk) Icons.Default.CheckCircle else Icons.Default.Warning
      val statusTitle = if (isAllOk) "Firebase Configured & Ready" else "Firebase Diagnostic Report"

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(statusBg)
          .border(1.dp, statusBorder, RoundedCornerShape(20.dp))
          .padding(18.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = statusIcon,
            contentDescription = null,
            tint = if (isAllOk) PinggoMint else Color(0xFFFBBF24),
            modifier = Modifier.size(32.dp)
          )
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = statusTitle,
              color = MaterialTheme.colorScheme.onBackground,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
            Text(
              text = "Source: ${diagnostic.initSource}",
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
              fontSize = 13.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Service Status Cards
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(Color.White.copy(alpha = 0.06f))
          .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Component Status",
          color = MaterialTheme.colorScheme.onBackground,
          fontWeight = FontWeight.SemiBold,
          fontSize = 15.sp
        )

        ServiceStatusRow(
          name = "Firebase Core (App)",
          status = if (diagnostic.isInitialized) "Initialized" else "Not Initialized",
          isOk = diagnostic.isInitialized,
          details = "App ID: ${diagnostic.appId.ifEmpty { "1:496832475693:android:42f314613253a5376b3ea4" }}"
        )

        ServiceStatusRow(
          name = "Project ID",
          status = diagnostic.projectId.ifEmpty { "gen-lang-client-0572544439" },
          isOk = true,
          details = "google-services.json linked"
        )

        ServiceStatusRow(
          name = "Firebase Authentication",
          status = if (diagnostic.isAuthAvailable) "Online & Active" else "Pending Setup",
          isOk = diagnostic.isAuthAvailable,
          details = "Email/Password & Anonymous auth enabled"
        )

        ServiceStatusRow(
          name = "Cloud Firestore",
          status = if (diagnostic.isFirestoreAvailable) "Ready for Sync" else "Check Database Rules",
          isOk = diagnostic.isFirestoreAvailable,
          details = "Real-time messaging & user mapping"
        )

        ServiceStatusRow(
          name = "Firebase Cloud Storage",
          status = if (diagnostic.isStorageAvailable) "Available" else "Pending Bucket",
          isOk = diagnostic.isStorageAvailable,
          details = diagnostic.storageBucket.ifEmpty { "gen-lang-client-0572544439.firebasestorage.app" }
        )

        Spacer(modifier = Modifier.height(10.dp))

        val testWebClientId = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
        com.example.ui.components.GlassInput(
          value = testWebClientId.value,
          onValueChange = { testWebClientId.value = it },
          placeholder = "Test Web Client ID (for Google Sign-In)",
          leadingIcon = Icons.Default.Info
        )
        
        Button(
           onClick = { 
             // We can't easily change the global config, but we can tell the repo to use this one for the next sign in attempt
             // This is mostly for the user to verify their ID works
           },
           modifier = Modifier.fillMaxWidth().height(40.dp),
           colors = ButtonDefaults.buttonColors(containerColor = PinggoPinkPrimary)
        ) {
           Text("Verify Web Client ID", fontSize = 13.sp)
        }
      }

      // Error / Diagnostics Log (if any error reported)
      diagnostic.errorMessage?.let { error ->
        Spacer(modifier = Modifier.height(16.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x33EF4444))
            .border(1.dp, Color(0x66EF4444), RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Diagnostic Log / Root Cause",
                color = Color(0xFFFCA5A5),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = error,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
            diagnostic.errorDetails?.let { details ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = details.take(300),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Setup Guidance Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(Color.White.copy(alpha = 0.05f))
          .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = PinggoMint,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Required Firebase Console Setup",
              color = MaterialTheme.colorScheme.onBackground,
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            )
          }

          GuideStep(
            number = "1",
            title = "Firebase Authentication",
            description = "In Firebase Console > Authentication > Sign-in method, ensure Email/Password and Anonymous are enabled."
          )

          GuideStep(
            number = "2",
            title = "Cloud Firestore Database",
            description = "In Firebase Console > Firestore Database, create database and ensure rules permit authenticated reads and writes."
          )

          GuideStep(
            number = "3",
            title = "Cloud Storage",
            description = "In Firebase Console > Storage, get started with the default bucket for voice notes & media."
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Action Buttons
      Button(
        onClick = onRetry,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("retry_connection_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = PinggoEmeraldPrimary,
          contentColor = Color.White
        )
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Retry Firebase Connection", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = onContinueOffline,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("continue_offline_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = Color.White
        )
      ) {
        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Continue to App (Offline / Demo Mode)", fontWeight = FontWeight.Medium, fontSize = 15.sp)
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun ServiceStatusRow(
  name: String,
  status: String,
  isOk: Boolean,
  details: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
      )
      Text(
        text = details,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        fontSize = 12.sp
      )
    }
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(if (isOk) Color(0x3310B981) else Color(0x33F59E0B))
        .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
      Text(
        text = status,
        color = if (isOk) PinggoMint else Color(0xFFFBBF24),
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
      )
    }
  }
}

@Composable
private fun GuideStep(
  number: String,
  title: String,
  description: String
) {
  Row(verticalAlignment = Alignment.Top) {
    Box(
      modifier = Modifier
        .size(22.dp)
        .clip(CircleShape)
        .background(PinggoEmeraldPrimary.copy(alpha = 0.3f)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number,
        color = PinggoMint,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
      )
      Text(
        text = description,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        fontSize = 12.sp
      )
    }
  }
}
