package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.theme.PinggoPinkPrimary
import com.example.viewmodel.PinggoViewModel

@Composable
fun SignUpScreen(
  viewModel: PinggoViewModel,
  onBack: () -> Unit,
  onSignedUp: () -> Unit
) {
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val authError by viewModel.authError.collectAsState()
  val usernameCheckState by viewModel.usernameCheckState.collectAsState()

  var fullName by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  LaunchedEffect(username) {
    val bare = username.trim().removePrefix("@")
    if (bare.length >= 3) {
      viewModel.checkUsername(bare)
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
          text = "Create Account",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      Text(
        text = "Join Pinggo today and experience the new liquid glass messaging vibes.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 32.dp)
      )

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Full Name
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Full Name", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            GlassInput(
              value = fullName,
              onValueChange = { fullName = it },
              placeholder = "Enter your full name",
              leadingIcon = Icons.Default.Person,
              testTag = "signup_name_input"
            )
          }

          // Username
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Username", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            GlassInput(
              value = username,
              onValueChange = { input ->
                val allowed = input.filter { it.isLetterOrDigit() || it == '_' }
                username = allowed.take(21).lowercase()
              },
              placeholder = "unique_username",
              leadingIcon = Icons.Default.Person,
              trailingIcon = {
                if (username.length >= 3) {
                  when (usernameCheckState) {
                    true -> Icon(Icons.Default.Check, "Available", tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                    false -> Icon(Icons.Default.Close, "Taken", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    null -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PinggoPinkPrimary)
                  }
                }
              },
              testTag = "signup_username_input"
            )
          }

          // Email
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Email Address", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            GlassInput(
              value = email,
              onValueChange = { email = it },
              placeholder = "name@gmail.com",
              leadingIcon = Icons.Default.Email,
              testTag = "signup_email_input"
            )
          }

          // Password
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Password", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            GlassInput(
              value = password,
              onValueChange = { password = it },
              placeholder = "Minimum 8 characters",
              leadingIcon = Icons.Default.Lock,
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
              },
              testTag = "signup_password_input"
            )
          }

          // Confirm Password
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Confirm Password", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            GlassInput(
              value = confirmPassword,
              onValueChange = { confirmPassword = it },
              placeholder = "Confirm your password",
              leadingIcon = Icons.Default.Lock,
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              testTag = "signup_confirm_password_input"
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          GlassButton(
            text = "Create Account",
            isLoading = isLoading,
            isPrimary = true,
            onClick = {
              if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
                viewModel.showToast("Please fill all fields")
                return@GlassButton
              }
              if (usernameCheckState == false) {
                viewModel.showToast("Username is already taken")
                return@GlassButton
              }
              if (password != confirmPassword) {
                viewModel.showToast("Passwords do not match")
                return@GlassButton
              }
              if (password.length < 8) {
                viewModel.showToast("Password must be at least 8 characters")
                return@GlassButton
              }

              viewModel.registerGuestAccount(
                username = username,
                email = email,
                password = password,
                confirmPass = confirmPassword,
                displayName = fullName
              ) { success, error ->
                if (success) {
                   onSignedUp()
                } else {
                   viewModel.showToast(error ?: "Signup failed")
                }
              }
            },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      if (authError != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = authError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Already have an account?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
        TextButton(onClick = onBack) {
          Text(text = "Sign In", color = PinggoPinkPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}
