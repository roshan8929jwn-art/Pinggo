package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassContainer
import com.example.ui.components.GlassInput
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoFullLogo
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.ui.theme.PinggoMint
import com.example.ui.theme.PinggoMintUltraLight
import com.example.viewmodel.PinggoViewModel

@Composable
fun LoginScreen(
  viewModel: PinggoViewModel,
  onLoggedIn: () -> Unit,
  onOpenDiagnostics: () -> Unit = {},
  onExploreDemo: () -> Unit = {}
) {
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val authError by viewModel.authError.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()

  LaunchedEffect(currentUser) {
    if (currentUser != null) {
      onLoggedIn()
    }
  }

  var showEmailForm by remember { mutableStateOf(false) }
  var isRegisterMode by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

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
      Spacer(modifier = Modifier.height(20.dp))

      // Top Penguin Logo with ambient glowing glass aura and official brand
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        PinggoFullLogo(
          iconSize = 90.dp,
          wordmarkSize = 32.sp,
          showTagline = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "Welcome to Pinggo",
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Connect with your friends and start chatting",
          fontSize = 14.sp,
          color = PinggoMintUltraLight,
          textAlign = TextAlign.Center,
          lineHeight = 20.sp
        )
      }

      // Middle / Action Buttons matching Screen 2
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // "Continue with Google" White Liquid Glass Pill Button
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
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF))
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            if (isLoading && !showEmailForm) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PinggoEmeraldPrimary,
                strokeWidth = 2.5.dp
              )
            } else {
              GoogleLogoIcon()
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "Continue with Google",
                color = Color(0xFF1E293B),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "Continue as Guest" Translucent Glass Pill Button matching screenshot
        GlassButton(
          text = "Continue as Guest",
          isPrimary = false,
          onClick = {
            viewModel.continueAsDirectUser("Pinggo User", "guest@pinggo.app")
          },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Email option
        Text(
          text = if (showEmailForm) "Hide email sign in" else "Or sign in with email",
          fontSize = 13.sp,
          color = PinggoMint,
          fontWeight = FontWeight.Medium,
          modifier = Modifier
            .clickable { showEmailForm = !showEmailForm }
            .padding(6.dp)
        )

        AnimatedVisibility(visible = showEmailForm) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            GlassInput(
              value = email,
              onValueChange = { email = it },
              placeholder = "name@example.com",
              leadingIcon = Icons.Default.Email,
              testTag = "email_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            GlassInput(
              value = password,
              onValueChange = { password = it },
              placeholder = "Password",
              leadingIcon = Icons.Default.Lock,
              testTag = "password_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            GlassButton(
              text = if (isRegisterMode) "Create Account" else "Sign In",
              onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                  if (isRegisterMode) {
                    viewModel.signUpWithEmail(email, password)
                  } else {
                    viewModel.signInWithEmail(email, password)
                  }
                } else {
                  viewModel.showToast("Please enter email and password")
                }
              },
              isLoading = isLoading,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                fontSize = 12.sp,
                color = Color(0xCCFFFFFF)
              )
              Text(
                text = if (isRegisterMode) "Sign In" else "Sign Up",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PinggoMint,
                modifier = Modifier.clickable { isRegisterMode = !isRegisterMode }
              )
            }
          }
        }

        if (authError != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = authError ?: "",
            color = Color(0xFFFF6B6B),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Open Firebase Diagnostic & Setup Guide",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PinggoMint,
            modifier = Modifier.clickable { onOpenDiagnostics() }
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Demo / Diagnostics Footer Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "✨ Explore Demo Mode",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PinggoMintUltraLight,
            modifier = Modifier.clickable {
              viewModel.enterGuestMode()
              onExploreDemo()
            }
          )

          Text(
            text = "🛠️ Diagnostics",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.clickable { onOpenDiagnostics() }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
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
