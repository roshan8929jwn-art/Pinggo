package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassInput
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.theme.PinggoPinkPrimary
import com.example.viewmodel.PinggoViewModel
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
  viewModel: PinggoViewModel,
  email: String,
  onBack: () -> Unit,
  onVerified: () -> Unit
) {
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val authError by viewModel.authError.collectAsState()
  
  var otpValue by remember { mutableStateOf("") }
  var resendTimer by remember { mutableStateOf(120) } // 2 minutes cooldown matching backend
  
  LaunchedEffect(key1 = true) {
    while (resendTimer > 0) {
      delay(1000)
      resendTimer--
    }
  }

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(modifier = Modifier.width(6.dp))
        PinggoBubbleIcon(size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Verify Email",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      Text(
        text = "We sent a 6-digit verification code to:",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
      )
      
      Text(
        text = email,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 8.dp)
      )

      Spacer(modifier = Modifier.height(32.dp))

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
          Text(
            text = "Enter Code",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold
          )

          GlassInput(
            value = otpValue,
            onValueChange = { if (it.length <= 6) otpValue = it.filter { c -> c.isDigit() } },
            placeholder = "000000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textAlign = TextAlign.Center,
            testTag = "otp_input"
          )

          GlassButton(
            text = "Verify Code",
            isLoading = isLoading,
            isPrimary = true,
            onClick = {
              if (otpValue.length != 6) {
                viewModel.showToast("Please enter the 6-digit code")
              } else {
                viewModel.verifyOtp(email, otpValue) { success ->
                  if (success) {
                    onVerified()
                  }
                }
              }
            },
            modifier = Modifier.fillMaxWidth()
          )

          if (resendTimer > 0) {
            Text(
              text = "Resend code in ${resendTimer}s",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          } else {
            TextButton(
              onClick = {
                viewModel.sendOtp(email)
                resendTimer = 120
              }
            ) {
              Text(
                text = "Resend Verification Code",
                color = PinggoPinkPrimary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      if (authError != null) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = authError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}
