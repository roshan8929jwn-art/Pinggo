package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassInput
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.PinggoBubbleIcon
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PinggoEmeraldPrimary
import com.example.ui.theme.PinggoMint
import com.example.ui.theme.PinggoMintUltraLight
import com.example.viewmodel.PinggoViewModel

@Composable
fun ProfileSetupScreen(
  viewModel: PinggoViewModel,
  onProfileCreated: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val isLoading by viewModel.isAuthLoading.collectAsState()
  val usernameCheckState by viewModel.usernameCheckState.collectAsState()

  var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
  var username by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("Hey! I'm using Pinggo 🐧") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    selectedImageUri = uri
  }

  val focusManager = LocalFocusManager.current

  LaunchedEffect(username) {
    val bare = username.trim().removePrefix("@")
    if (bare.length >= 3 && username.matches(Regex("^@?[a-zA-Z0-9_]+$"))) {
      viewModel.checkUsername(username)
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
      // Header matching Screen 3
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = { /* back handled by navigation */ }, modifier = Modifier.size(40.dp)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(6.dp))
        PinggoBubbleIcon(size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Create Your Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Make it yours",
            fontSize = 13.sp,
            color = PinggoMintUltraLight
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Glowing circular camera container matching screenshot
      Box(
        modifier = Modifier
          .size(110.dp)
          .clip(CircleShape)
          .background(
            brush = Brush.radialGradient(
              colors = listOf(Color(0x5534D399), Color(0x2210B981), Color(0x00000000))
            )
          )
          .border(2.dp, DarkGlassBorder, CircleShape)
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
            tint = Color.White,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(30.dp))

      // Display Name section
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Display Name",
          fontSize = 13.sp,
          color = Color(0xCCFFFFFF),
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 6.dp)
        )
        GlassInput(
          value = displayName,
          onValueChange = { displayName = it },
          placeholder = "e.g. Roshan Verma",
          testTag = "display_name_input"
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Username section with validation status
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Username",
          fontSize = 13.sp,
          color = Color(0xCCFFFFFF),
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 6.dp)
        )
        GlassInput(
          value = username,
          onValueChange = { input ->
            val allowed = input.filter { it.isLetterOrDigit() || it == '_' || it == '@' }
            username = allowed.take(21).lowercase()
          },
          placeholder = "@username",
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next
          ),
          keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
          ),
          trailingIcon = {
            val bareLength = username.trim().removePrefix("@").length
            if (bareLength >= 3 && username.matches(Regex("^@?[a-zA-Z0-9_]+$"))) {
              when (usernameCheckState) {
                true -> Icon(Icons.Default.Check, "Available", tint = OnlineGreen, modifier = Modifier.size(20.dp))
                false -> Icon(Icons.Default.Close, "Taken", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                null -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PinggoMint)
              }
            }
          },
          testTag = "username_input"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Username must be 3-20 characters (@, letters, numbers, underscore)",
          fontSize = 11.sp,
          color = PinggoMintUltraLight.copy(alpha = 0.8f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Bio section
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Bio (optional)",
          fontSize = 13.sp,
          color = Color(0xCCFFFFFF),
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 6.dp)
        )
        GlassInput(
          value = bio,
          onValueChange = { bio = it },
          placeholder = "Hey! I'm using Pinggo",
          testTag = "bio_input"
        )
      }

      Spacer(modifier = Modifier.height(36.dp))

      // "Continue" pill button
      GlassButton(
        text = "Continue",
        onClick = {
          if (displayName.trim().isEmpty()) {
            viewModel.showToast("Please enter your name")
            return@GlassButton
          }
          val cleanUsername = username.trim().lowercase()
          val bare = cleanUsername.removePrefix("@")
          if (bare.length < 3) {
            viewModel.showToast("Username must be at least 3 characters")
            return@GlassButton
          }
          if (!cleanUsername.matches(Regex("^@?[a-z0-9_]+$"))) {
            viewModel.showToast("Username can only contain @, letters, numbers, and underscore")
            return@GlassButton
          }
          val photo = selectedImageUri?.toString() ?: (currentUser?.photoUrl?.toString() ?: "")
          viewModel.createProfile(displayName, cleanUsername, bio, photo) {
            onProfileCreated()
          }
        },
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}
