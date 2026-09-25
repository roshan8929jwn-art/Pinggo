package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassToast
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreateGroupScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.PinggoTheme
import com.example.viewmodel.PinggoViewModel

enum class PinggoScreen {
  SPLASH,
  LOGIN,
  PROFILE_SETUP,
  HOME,
  CHAT,
  USER_SEARCH,
  CREATE_GROUP,
  SETTINGS
}

class MainActivity : ComponentActivity() {
  private val viewModel: PinggoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val themeMode by viewModel.themeMode.collectAsState()
      val currentUser by viewModel.currentUser.collectAsState()
      val userProfile by viewModel.userProfile.collectAsState()
      val activeConversation by viewModel.activeConversation.collectAsState()
      val activeCall by viewModel.activeCall.collectAsState()
      val toastMessage by viewModel.toastMessage.collectAsState()

      var currentScreen by remember { mutableStateOf(PinggoScreen.SPLASH) }
      var settingsSection by remember { mutableStateOf("general") }

      // Audio & Notification permissions
      val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
      ) { /* permissions handled */ }

      LaunchedEffect(Unit) {
        val permissions = mutableListOf(
          Manifest.permission.RECORD_AUDIO,
          Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needed = permissions.filter {
          ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
          permissionLauncher.launch(needed.toTypedArray())
        }
      }

      PinggoTheme(themeMode = themeMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color.Transparent
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
              PinggoScreen.SPLASH -> {
                SplashScreen(
                  onReady = {
                    currentScreen = when {
                      currentUser == null -> PinggoScreen.LOGIN
                      userProfile == null || userProfile?.username.isNullOrEmpty() -> PinggoScreen.PROFILE_SETUP
                      else -> PinggoScreen.HOME
                    }
                  }
                )
              }

              PinggoScreen.LOGIN -> {
                LoginScreen(
                  viewModel = viewModel,
                  onLoggedIn = {
                    currentScreen = if (userProfile == null || userProfile?.username.isNullOrEmpty()) {
                      PinggoScreen.PROFILE_SETUP
                    } else {
                      PinggoScreen.HOME
                    }
                  }
                )
              }

              PinggoScreen.PROFILE_SETUP -> {
                ProfileSetupScreen(
                  viewModel = viewModel,
                  onProfileCreated = {
                    currentScreen = PinggoScreen.HOME
                  }
                )
              }

              PinggoScreen.HOME -> {
                HomeScreen(
                  viewModel = viewModel,
                  onOpenChat = { conv ->
                    currentScreen = PinggoScreen.CHAT
                  },
                  onOpenSearch = {
                    currentScreen = PinggoScreen.USER_SEARCH
                  },
                  onOpenCreateGroup = {
                    currentScreen = PinggoScreen.CREATE_GROUP
                  },
                  onOpenSettings = { section ->
                    settingsSection = section
                    currentScreen = PinggoScreen.SETTINGS
                  }
                )
              }

              PinggoScreen.CHAT -> {
                activeConversation?.let { conv ->
                  ChatScreen(
                    viewModel = viewModel,
                    conversation = conv,
                    onBack = {
                      viewModel.closeConversation()
                      currentScreen = PinggoScreen.HOME
                    },
                    onStartCall = { targetUser, callType ->
                      viewModel.startCall(targetUser, callType)
                    }
                  )
                } ?: run {
                  currentScreen = PinggoScreen.HOME
                }
              }

              PinggoScreen.USER_SEARCH -> {
                com.example.ui.screens.UserSearchScreen(
                  viewModel = viewModel,
                  onBack = { currentScreen = PinggoScreen.HOME },
                  onChatReady = { conv ->
                    currentScreen = PinggoScreen.CHAT
                  }
                )
              }

              PinggoScreen.CREATE_GROUP -> {
                CreateGroupScreen(
                  viewModel = viewModel,
                  onBack = { currentScreen = PinggoScreen.HOME },
                  onGroupCreated = { conv ->
                    currentScreen = PinggoScreen.CHAT
                  }
                )
              }

              PinggoScreen.SETTINGS -> {
                SettingsScreen(
                  viewModel = viewModel,
                  section = settingsSection,
                  onBack = { currentScreen = PinggoScreen.HOME }
                )
              }
            }

            // Real-Time Active Call Screen Overlay
            activeCall?.let { call ->
              CallScreen(
                viewModel = viewModel,
                call = call,
                onCallEnded = {
                  // call ended handled in viewModel
                }
              )
            }

            // In-App Toast
            AnimatedVisibility(
              visible = toastMessage != null,
              enter = fadeIn(),
              exit = fadeOut(),
              modifier = Modifier.align(Alignment.BottomCenter)
            ) {
              toastMessage?.let { msg ->
                GlassToast(message = msg)
              }
            }
          }
        }
      }
    }
  }
}
