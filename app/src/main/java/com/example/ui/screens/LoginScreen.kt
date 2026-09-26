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

  var identifierInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var showLoginOptions by remember { mutableStateOf(false) }

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
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Connect with your friends with real Google and password authentication",
          fontSize = 14.sp,
          color = Color.Gray,
          textAlign = TextAlign.Center,
          lineHeight = 20.sp
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
            if (isLoading && !showLoginOptions) {
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
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
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
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp)
          )
          Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFEEEEEE)))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login with Username/Password
        GlassButton(
          text = if (showLoginOptions) "Hide Login" else "Login with Password",
          icon = Icons.Default.Lock,
          isPrimary = false,
          onClick = { showLoginOptions = !showLoginOptions },
          modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = showLoginOptions) {
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
                  text = "Login to Pinggo",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassInput(
                  value = identifierInput,
                  onValueChange = { identifierInput = it },
                  placeholder = "Username or Email",
                  leadingIcon = Icons.Default.Email,
                  testTag = "login_identifier_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassInput(
                  value = passwordInput,
                  onValueChange = { passwordInput = it },
                  placeholder = "Password",
                  leadingIcon = Icons.Default.Lock,
                  visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                  trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                      Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                      )
                    }
                  },
                  testTag = "login_password_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassButton(
                  text = "Sign In",
                  isPrimary = true,
                  isLoading = isLoading,
                  onClick = {
                    if (identifierInput.isNotBlank() && passwordInput.isNotBlank()) {
                      viewModel.signInWithUsernameOrEmail(identifierInput, passwordInput) { success, _ ->
                        if (success) {
                          onLoggedIn()
                        }
                      }
                    } else {
                      viewModel.showToast("Please enter identifier and password")
                    }
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_submit_button")
                )
              }
            }
          }
        }

        if (authError != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = authError ?: "",
            color = Color.Red,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
        }
      }

      // Footer
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "\uD83D\uDEE0️ Diagnostics",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color.Gray,
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
