package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassInput
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.theme.PinggoPinkPrimary
import com.example.ui.theme.PinggoPinkLight
import com.example.viewmodel.PinggoViewModel

@Composable
fun ProfileSetupScreen(
  viewModel: PinggoViewModel,
  onProfileCreated: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val usernameCheckState by viewModel.usernameCheckState.collectAsState()

  var currentStep by remember { mutableStateOf(1) } // 1: Info, 2: Password, 3: Verification

  var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
  var username by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("Hey! I'm using Pinggo 🐧") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    selectedImageUri = uri
  }

  val focusManager = LocalFocusManager.current

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
          .padding(top = 10.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = { 
          if (currentStep > 1) currentStep--
        }, modifier = Modifier.size(40.dp)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(6.dp))
        PinggoBubbleIcon(size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = when(currentStep) {
              1 -> "Create Your Profile"
              2 -> "Security Setup"
              else -> "Email Verification"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
          )
          Text(
            text = when(currentStep) {
              1 -> "Tell us about yourself"
              2 -> "Set your password"
              else -> "Verify your Gmail address"
            },
            fontSize = 13.sp,
            color = Color.Gray
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      when (currentStep) {
        1 -> {
          // Profile Info Step
          Box(
            modifier = Modifier
              .size(110.dp)
              .clip(CircleShape)
              .background(
                brush = Brush.radialGradient(
                  colors = listOf(PinggoPinkLight.copy(alpha = 0.3f), Color.Transparent)
                )
              )
              .border(2.dp, Color.LightGray, CircleShape)
              .clickable { photoPickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
          ) {
            if (selectedImageUri != null) {
              AsyncImage(
                model = selectedImageUri,
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            } else if (!currentUser?.photoUrl.toString().isNullOrEmpty()) {
              AsyncImage(
                model = currentUser?.photoUrl.toString(),
                contentDescription = "Google photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            } else {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Add photo",
                tint = Color.Gray,
                modifier = Modifier.size(36.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(30.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Display Name", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            GlassInput(
              value = displayName,
              onValueChange = { displayName = it },
              placeholder = "e.g. Roshan Verma",
              testTag = "display_name_input"
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Username", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            GlassInput(
              value = username,
              onValueChange = { input ->
                val allowed = input.filter { it.isLetterOrDigit() || it == '_' }
                username = allowed.take(21).lowercase()
              },
              placeholder = "username",
              leadingIcon = Icons.Default.Person,
              trailingIcon = {
                if (username.length >= 3) {
                  when (usernameCheckState) {
                    true -> Icon(Icons.Default.Check, "Available", tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                    false -> Icon(Icons.Default.Close, "Taken", tint = Color.Red, modifier = Modifier.size(20.dp))
                    null -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PinggoPinkPrimary)
                  }
                }
              },
              testTag = "username_input"
            )
            Text(
              text = "Lowercase, numbers, and underscores only.",
              fontSize = 11.sp,
              color = Color.Gray,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Bio (optional)", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            GlassInput(
              value = bio,
              onValueChange = { bio = it },
              placeholder = "Hey! I'm using Pinggo",
              testTag = "bio_input"
            )
          }

          Spacer(modifier = Modifier.height(36.dp))

          GlassButton(
            text = "Next",
            onClick = {
              if (displayName.isBlank()) {
                viewModel.showToast("Please enter your name")
                return@GlassButton
              }
              if (username.length < 3) {
                viewModel.showToast("Username must be at least 3 characters")
                return@GlassButton
              }
              if (usernameCheckState == false) {
                viewModel.showToast("Username is already taken")
                return@GlassButton
              }
              currentStep = 2
            },
            modifier = Modifier.fillMaxWidth()
          )
        }

        2 -> {
          // Password Setup Step
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Set Password", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
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
              testTag = "setup_password_input"
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Confirm Password", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            GlassInput(
              value = confirmPassword,
              onValueChange = { confirmPassword = it },
              placeholder = "Confirm your password",
              leadingIcon = Icons.Default.Lock,
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              testTag = "setup_confirm_password_input"
            )
          }

          Spacer(modifier = Modifier.height(36.dp))

          GlassButton(
            text = "Continue",
            onClick = {
              if (password.length < 8) {
                viewModel.showToast("Password must be at least 8 characters")
                return@GlassButton
              }
              if (password != confirmPassword) {
                viewModel.showToast("Passwords do not match")
                return@GlassButton
              }
              
              // Proceed to verification or creation
              if (currentUser?.isEmailVerified == true) {
                 createProfile(viewModel, displayName, username, bio, selectedImageUri, password, onProfileCreated)
              } else {
                currentStep = 3
              }
            },
            modifier = Modifier.fillMaxWidth()
          )
        }

        3 -> {
          // Email Verification Step
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Email, null, tint = PinggoPinkPrimary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Verification email sent to ${currentUser?.email}. Please check your Gmail inbox and tap the link to continue.",
              textAlign = TextAlign.Center,
              color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            GlassButton(
              text = "Refresh Status",
              onClick = {
                viewModel.currentUser.value?.reload()?.addOnCompleteListener {
                  if (viewModel.currentUser.value?.isEmailVerified == true) {
                    createProfile(viewModel, displayName, username, bio, selectedImageUri, password, onProfileCreated)
                  } else {
                    viewModel.showToast("Email not yet verified. Please check your inbox.")
                  }
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(onClick = { viewModel.resendVerificationEmail() }) {
              Text("Resend Verification Email", color = PinggoPinkPrimary)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

private fun createProfile(
  viewModel: PinggoViewModel,
  displayName: String,
  username: String,
  bio: String,
  photoUri: Uri?,
  password: String,
  onComplete: () -> Unit
) {
  val photo = photoUri?.toString() ?: (viewModel.currentUser.value?.photoUrl?.toString() ?: "")
  viewModel.createProfile(displayName, username, bio, photo, password) {
    onComplete()
  }
}
