package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassInput
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoFullLogo
import com.example.ui.theme.PinggoPinkPrimary
import com.example.ui.theme.PinggoPinkLight
import com.example.viewmodel.PinggoViewModel

@Composable
fun LoginScreen(
  viewModel: PinggoViewModel,
  onLoggedIn: () -> Unit,
  onOpenDiagnostics: () -> Unit = {},
  onSignUpRequested: () -> Unit = {},
  onPasswordLoginRequested: () -> Unit = {}
) {
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val authError by viewModel.authError.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()

  LaunchedEffect(currentUser) {
    if (currentUser != null) {
      onLoggedIn()
    }
  }

  LiquidGlassBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Top Penguin Logo
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PinggoFullLogo(
          iconSize = 90.dp,
          wordmarkSize = 32.sp,
          showTagline = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "Welcome to Pinggo",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Connect with your friends with real Google and password authentication",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
          textAlign = TextAlign.Center,
          lineHeight = 22.sp
        )
      }

      // Middle: Real Google Sign-In & Login
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // "Continue with Google" Official Google Account Pill Button
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(enabled = !isLoading) {
              viewModel.signInWithGoogle()
            }
            .testTag("google_login_button"),
          shape = RoundedCornerShape(28.dp),
          color = Color.White,
          shadowElevation = 8.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PinggoPinkPrimary,
                strokeWidth = 2.5.dp
              )
            } else {
              GoogleLogoIcon()
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "Continue with Google",
                color = Color(0xFF1E293B),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFEEEEEE)))
          Text(
            text = "OR",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 12.dp)
          )
          Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFEEEEEE)))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login with Username/Password
        GlassButton(
          text = "Login with Password",
          icon = Icons.Default.Lock,
          isPrimary = false,
          onClick = onPasswordLoginRequested,
          modifier = Modifier.fillMaxWidth()
        )

        if (authError != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = authError ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Don't have an account?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
          )
          TextButton(onClick = onSignUpRequested) {
            Text(
              text = "Create New Account",
              color = PinggoPinkPrimary,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Footer
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "\uD83D\uDEE0️ Diagnostics",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
          modifier = Modifier.clickable { onOpenDiagnostics() }
        )
        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }
}

@Composable
fun GoogleLogoIcon() {
  Box(
    modifier = Modifier
      .size(24.dp)
      .clip(CircleShape)
      .background(Color.White),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "G",
      fontSize = 17.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Color(0xFF4285F4)
    )
  }
}
