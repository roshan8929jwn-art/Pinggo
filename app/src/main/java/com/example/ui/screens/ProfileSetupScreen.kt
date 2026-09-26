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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.focus.focusRequester
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
import kotlinx.coroutines.delay

@Composable
fun ProfileSetupScreen(
  viewModel: PinggoViewModel,
  onProfileCreated: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val usernameCheckState by viewModel.usernameCheckState.collectAsState()

  var currentStep by remember { mutableStateOf(1) } // 1: Info, 2: Password, 3: OTP

  var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
  var username by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("Hey! I'm using Pinggo 🐧") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var otpValue by remember { mutableStateOf("") }
  var isOtpVerified by remember { mutableStateOf(false) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    selectedImageUri = uri
  }

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
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(modifier = Modifier.width(6.dp))
        PinggoBubbleIcon(size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = when(currentStep) {
              1 -> "Create Your Profile"
              2 -> "Security Setup"
              else -> "Email OTP Verification"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = when(currentStep) {
              1 -> "Tell us about yourself"
              2 -> "Set your password"
              else -> "Enter the 6-digit code sent to your email"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
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
              .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
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
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(30.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Display Name", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            GlassInput(
              value = displayName,
              onValueChange = { displayName = it },
              placeholder = "e.g. Roshan Verma",
              testTag = "display_name_input"
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Username", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
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
                    false -> Icon(Icons.Default.Close, "Taken", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    null -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PinggoPinkPrimary)
                  }
                }
              },
              testTag = "username_input"
            )
            Text(
              text = "Lowercase, numbers, and underscores only.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Bio (optional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
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
            Text(text = "Set Password", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
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
            Text(text = "Confirm Password", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
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
            text = "Continue to OTP",
            onClick = {
              if (password.length < 8) {
                viewModel.showToast("Password must be at least 8 characters")
                return@GlassButton
              }
              if (password != confirmPassword) {
                viewModel.showToast("Passwords do not match")
                return@GlassButton
              }
              
              val email = currentUser?.email
              if (email != null) {
                viewModel.sendOtp(email)
                currentStep = 3
              } else {
                viewModel.showToast("No email associated with this account")
              }
            },
            modifier = Modifier.fillMaxWidth()
          )
        }

        3 -> {
          // OTP Entry Step
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Email, null, tint = PinggoPinkPrimary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Check your email ${currentUser?.email} for a 6-digit code.",
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OtpInputFields(
              otpValue = otpValue,
              onOtpChange = { otpValue = it }
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            GlassButton(
              text = "Verify & Finish",
              isLoading = isLoading,
              onClick = {
                if (otpValue.length != 6) {
                  viewModel.showToast("Please enter the 6-digit OTP")
                  return@GlassButton
                }
                val email = currentUser?.email ?: return@GlassButton
                viewModel.verifyOtp(email, otpValue) { success ->
                  if (success) {
                    createProfile(viewModel, displayName, username, bio, selectedImageUri, password, onProfileCreated)
                  }
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            var resendCooldown by remember { mutableStateOf(0) }
            LaunchedEffect(resendCooldown) {
              if (resendCooldown > 0) {
                delay(1000)
                resendCooldown--
              }
            }

            TextButton(
              enabled = resendCooldown == 0,
              onClick = { 
                currentUser?.email?.let { viewModel.sendOtp(it) }
                resendCooldown = 60
              }
            ) {
              Text(
                text = if (resendCooldown > 0) "Resend OTP in ${resendCooldown}s" else "Resend OTP Code",
                color = if (resendCooldown > 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else PinggoPinkPrimary
              )
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

@Composable
fun OtpInputFields(
  otpValue: String,
  onOtpChange: (String) -> Unit
) {
  val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
  val focusRequesters = remember { List(6) { androidx.compose.ui.focus.FocusRequester() } }

  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    (0 until 6).forEach { index ->
      val char = otpValue.getOrNull(index)?.toString() ?: ""
      
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .border(
            width = 1.dp,
            color = if (otpValue.length == index) PinggoPinkPrimary else Color.Transparent,
            shape = RoundedCornerShape(12.dp)
          )
          .clickable { focusRequesters[index].requestFocus() },
        contentAlignment = Alignment.Center
      ) {
        androidx.compose.foundation.text.BasicTextField(
          value = char,
          onValueChange = { newValue ->
            if (newValue.length <= 1) {
              val currentOtp = otpValue.toCharArray().toMutableList()
              if (newValue.isEmpty()) {
                if (index < currentOtp.size) currentOtp.removeAt(index)
                if (index > 0) focusRequesters[index - 1].requestFocus()
              } else {
                if (index < currentOtp.size) {
                  currentOtp[index] = newValue[0]
                } else {
                  currentOtp.add(newValue[0])
                }
                if (index < 5) focusRequesters[index + 1].requestFocus()
              }
              onOtpChange(currentOtp.joinToString(""))
            }
          },
          modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .focusRequester(focusRequesters[index])
            .testTag("otp_digit_$index"),
          textStyle = MaterialTheme.typography.headlineSmall.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          ),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
          ),
          singleLine = true
        )
        
        if (char.isEmpty()) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
          )
        }
      }
    }
  }
  
  // Invisible field to handle pasting
  BasicTextField(
    value = "",
    onValueChange = { pasted ->
      if (pasted.length == 6 && pasted.all { it.isDigit() }) {
        onOtpChange(pasted)
        focusManager.clearFocus()
      }
    },
    modifier = Modifier.size(0.dp)
  )
}
