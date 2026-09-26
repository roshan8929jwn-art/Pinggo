package com.example
// Forcing rebuild for preview

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import kotlinx.coroutines.launch
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
import com.example.ui.screens.FirebaseDiagnosticScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.OtpVerificationScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AppThemeMode
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
  SETTINGS,
  DIAGNOSTIC,
  SIGN_UP,
  PASSWORD_LOGIN,
  OTP_VERIFICATION
}

class MainActivity : ComponentActivity() {
  private val viewModel: PinggoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      enableEdgeToEdge()
    } catch (t: Throwable) {
      Log.w("MainActivity", "EdgeToEdge warning: ${t.message}")
    }

    setContent {
      val themeMode by viewModel.themeMode.collectAsState()
      val currentUser by viewModel.currentUser.collectAsState()
      val userProfile by viewModel.userProfile.collectAsState()
      val activeConversation by viewModel.activeConversation.collectAsState()
      val activeCall by viewModel.activeCall.collectAsState()
      val toastMessage by viewModel.toastMessage.collectAsState()
      val diagnostic by viewModel.firebaseDiagnostic.collectAsState()
      val isGuestMode by viewModel.isGuestMode.collectAsState()

      val glassDesign by viewModel.glassDesign.collectAsState()

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

      LaunchedEffect(currentUser, isGuestMode, userProfile) {
        if (currentUser == null && !isGuestMode && currentScreen != PinggoScreen.SPLASH && currentScreen != PinggoScreen.LOGIN && currentScreen != PinggoScreen.DIAGNOSTIC && currentScreen != PinggoScreen.SIGN_UP && currentScreen != PinggoScreen.PASSWORD_LOGIN) {
          currentScreen = PinggoScreen.LOGIN
        } else if (currentUser != null && !isGuestMode) {
          // Force OTP verification if not verified
          if (userProfile != null && !userProfile!!.otpVerified && currentScreen != PinggoScreen.OTP_VERIFICATION && currentScreen != PinggoScreen.DIAGNOSTIC) {
            currentScreen = PinggoScreen.OTP_VERIFICATION
          }
        }
      }

      PinggoTheme(
        isDarkTheme = when (themeMode) {
          AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
          AppThemeMode.LIGHT -> false
          AppThemeMode.DARK -> true
        },
        glassDesign = glassDesign
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
              PinggoScreen.SPLASH -> {
                SplashScreen(
                  onReady = {
                    currentScreen = when {
                      currentUser != null -> {
                        if (userProfile != null && !userProfile!!.otpVerified) {
                          PinggoScreen.OTP_VERIFICATION
                        } else if (userProfile == null || userProfile?.username.isNullOrEmpty()) {
                          PinggoScreen.PROFILE_SETUP
                        } else {
                          PinggoScreen.HOME
                        }
                      }
                      isGuestMode -> PinggoScreen.HOME
                      else -> PinggoScreen.LOGIN
                    }
                  }
                )
              }

              PinggoScreen.LOGIN -> {
                LoginScreen(
                  viewModel = viewModel,
                  onLoggedIn = {
                    currentScreen = if (userProfile != null && !userProfile!!.otpVerified) {
                      PinggoScreen.OTP_VERIFICATION
                    } else if (userProfile == null || userProfile?.username.isNullOrEmpty()) {
                      PinggoScreen.PROFILE_SETUP
                    } else {
                      PinggoScreen.HOME
                    }
                  },
                  onOpenDiagnostics = {
                    currentScreen = PinggoScreen.DIAGNOSTIC
                  },
                  onSignUpRequested = {
                    viewModel.clearAuthError()
                    currentScreen = PinggoScreen.SIGN_UP
                  },
                  onPasswordLoginRequested = {
                    viewModel.clearAuthError()
                    currentScreen = PinggoScreen.PASSWORD_LOGIN
                  }
                )
              }

              PinggoScreen.PASSWORD_LOGIN -> {
                BackHandler {
                  currentScreen = PinggoScreen.LOGIN
                }
                com.example.ui.screens.PasswordLoginScreen(
                  viewModel = viewModel,
                  onBack = { 
                    viewModel.clearAuthError()
                    currentScreen = PinggoScreen.LOGIN 
                  },
                  onLoggedIn = {
                    currentScreen = if (userProfile == null || userProfile?.username.isNullOrEmpty()) {
                      PinggoScreen.PROFILE_SETUP
                    } else {
                      PinggoScreen.HOME
                    }
                  },
                  onSignUpRequested = {
                    viewModel.clearAuthError()
                    currentScreen = PinggoScreen.SIGN_UP
                  }
                )
              }

              PinggoScreen.SIGN_UP -> {
                BackHandler {
                  currentScreen = PinggoScreen.LOGIN
                }
                SignUpScreen(
                  viewModel = viewModel,
                  onBack = { 
                    viewModel.clearAuthError()
                    currentScreen = PinggoScreen.LOGIN 
                  },
                  onSignedUp = {
                    currentScreen = PinggoScreen.PROFILE_SETUP
                  }
                )
              }

              PinggoScreen.PROFILE_SETUP -> {
                BackHandler {
                  currentScreen = PinggoScreen.LOGIN
                }
                ProfileSetupScreen(
                  viewModel = viewModel,
                  onProfileCreated = {
                    currentScreen = PinggoScreen.HOME
                  }
                )
              }

              PinggoScreen.OTP_VERIFICATION -> {
                val email = currentUser?.email ?: ""
                OtpVerificationScreen(
                  viewModel = viewModel,
                  email = email,
                  onBack = {
                    viewModel.signOut()
                    currentScreen = PinggoScreen.LOGIN
                  },
                  onVerified = {
                    // Refresh profile after verification
                    currentUser?.uid?.let { uid ->
                      lifecycleScope.launch {
                        viewModel.authRepo.loadUserProfile(uid)
                      }
                    }
                    currentScreen = if (userProfile == null || userProfile?.username.isNullOrEmpty()) {
                      PinggoScreen.PROFILE_SETUP
                    } else {
                      PinggoScreen.HOME
                    }
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
                BackHandler {
                  viewModel.closeConversation()
                  currentScreen = PinggoScreen.HOME
                }
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
                BackHandler {
                  currentScreen = PinggoScreen.HOME
                }
                com.example.ui.screens.UserSearchScreen(
                  viewModel = viewModel,
                  onBack = { currentScreen = PinggoScreen.HOME },
                  onChatReady = { conv ->
                    currentScreen = PinggoScreen.CHAT
                  }
                )
              }

              PinggoScreen.CREATE_GROUP -> {
                BackHandler {
                  currentScreen = PinggoScreen.HOME
                }
                CreateGroupScreen(
                  viewModel = viewModel,
                  onBack = { currentScreen = PinggoScreen.HOME },
                  onGroupCreated = { conv ->
                    currentScreen = PinggoScreen.CHAT
                  }
                )
              }

              PinggoScreen.SETTINGS -> {
                BackHandler {
                  currentScreen = PinggoScreen.HOME
                }
                SettingsScreen(
                  viewModel = viewModel,
                  section = settingsSection,
                  onBack = { currentScreen = PinggoScreen.HOME }
                )
              }

              PinggoScreen.DIAGNOSTIC -> {
                BackHandler {
                  currentScreen = if (currentUser != null || isGuestMode) PinggoScreen.HOME else PinggoScreen.LOGIN
                }
                FirebaseDiagnosticScreen(
                  diagnostic = diagnostic,
                  onRetry = {
                    viewModel.retryFirebaseInitialization()
                  },
                  onContinueOffline = {
                    viewModel.enterGuestMode()
                    currentScreen = PinggoScreen.HOME
                  }
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
