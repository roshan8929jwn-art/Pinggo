package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassInput
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoFullLogo
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.ui.theme.PinggoMint
import com.example.ui.theme.PinggoMintUltraLight
import com.example.viewmodel.PinggoViewModel

@Composable
fun LoginScreen(
  viewModel: PinggoViewModel,
  onLoggedIn: () -> Unit,
  onOpenDiagnostics: () -> Unit = {}
) {
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val authError by viewModel.authError.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()

  LaunchedEffect(currentUser) {
    if (currentUser != null) {
      onLoggedIn()
    }
  }

  var emailInput by remember { mutableStateOf("") }
  var isLinkSent by remember { mutableStateOf(false) }
  var showEmailOption by remember { mutableStateOf(false) }

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
          text = "Connect with your friends with real Google and email authentication",
          fontSize = 14.sp,
          color = PinggoMintUltraLight,
          textAlign = TextAlign.Center,
          lineHeight = 20.sp
        )
      }

      // Middle: Real Google Sign-In & Passwordless Email Link
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
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF))
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            if (isLoading && !showEmailOption) {
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

        Spacer(modifier = Modifier.height(16.dp))

        // Divider with "or continue with"
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(Color(0x33FFFFFF))
          )
          Text(
            text = "OR",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0x88FFFFFF),
            modifier = Modifier.padding(horizontal = 12.dp)
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(Color(0x33FFFFFF))
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Continue with Email (Passwordless Link)"
        GlassButton(
          text = if (showEmailOption) "Hide Email Link" else "Continue with Email",
          icon = Icons.Default.Email,
          isPrimary = false,
          onClick = { showEmailOption = !showEmailOption },
          modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = showEmailOption) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "Passwordless Email Verification",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "We'll send a genuine Firebase sign-in link to your Gmail or email address.",
                  fontSize = 12.sp,
                  color = PinggoMintUltraLight,
                  textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassInput(
                  value = emailInput,
                  onValueChange = {
                    emailInput = it
                    isLinkSent = false
                  },
                  placeholder = "Enter your Gmail or email address",
                  leadingIcon = Icons.Default.Email,
                  testTag = "email_signin_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassButton(
                  text = if (isLinkSent) "Resend Sign-In Link" else "Send Sign-In Link",
                  icon = if (isLinkSent) Icons.Default.MarkEmailRead else Icons.Default.Email,
                  isPrimary = true,
                  isLoading = isLoading,
                  onClick = {
                    if (emailInput.isNotBlank()) {
                      viewModel.sendSignInLinkToEmail(emailInput) { success, _ ->
                        if (success) {
                          isLinkSent = true
                        }
                      }
                    } else {
                      viewModel.showToast("Please enter your email address")
                    }
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_email_link_button")
                )

                if (isLinkSent) {
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "✓ Verification link sent! Check your inbox at $emailInput and tap the link to complete sign-in.",
                    fontSize = 12.sp,
                    color = PinggoMint,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
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
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Open Firebase Diagnostic & Setup Guide",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PinggoMint,
            modifier = Modifier.clickable { onOpenDiagnostics() }
          )
        }
      }

      // Footer
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🛠️ Diagnostics & Setup Guide",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color.White.copy(alpha = 0.7f),
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
