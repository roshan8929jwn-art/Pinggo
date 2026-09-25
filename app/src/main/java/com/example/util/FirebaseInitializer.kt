package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FirebaseDiagnosticInfo(
  val isInitialized: Boolean = false,
  val isAuthAvailable: Boolean = false,
  val isFirestoreAvailable: Boolean = false,
  val isStorageAvailable: Boolean = false,
  val projectId: String = "",
  val appId: String = "",
  val storageBucket: String = "",
  val errorMessage: String? = null,
  val errorDetails: String? = null,
  val initSource: String = "Pending"
)

object FirebaseInitializer {
  private const val TAG = "FirebaseInitializer"

  private val _diagnostic = MutableStateFlow(FirebaseDiagnosticInfo())
  val diagnostic: StateFlow<FirebaseDiagnosticInfo> = _diagnostic.asStateFlow()

  @Synchronized
  fun initialize(context: Context): FirebaseDiagnosticInfo {
    try {
      Log.d(TAG, "Starting Firebase initialization...")
      val existingApps = FirebaseApp.getApps(context)
      var app: FirebaseApp? = if (existingApps.isNotEmpty()) {
        FirebaseApp.getInstance()
      } else {
        null
      }

      var initSource = if (app != null) "Existing Instance" else "None"

      if (app == null) {
        // Attempt 1: Standard automatic initialization from google-services resources
        try {
          app = FirebaseApp.initializeApp(context)
          if (app != null) {
            initSource = "Automatic (google-services.json)"
            Log.d(TAG, "Initialized via standard Google Services provider")
          }
        } catch (e: Exception) {
          Log.w(TAG, "Standard Firebase initialization encountered issue: ${e.message}")
        }
      }

      // Attempt 2: Programmatic fallback with explicit FirebaseOptions if standard init didn't produce an app
      if (app == null || FirebaseApp.getApps(context).isEmpty()) {
        try {
          val fallbackOptions = FirebaseOptions.Builder()
            .setApplicationId("1:496832475693:android:42f314613253a5376b3ea4")
            .setApiKey("AIzaSyCKkYOm2yFDvLpiN5h8oo9o9wjaNOZR3vo")
            .setProjectId("gen-lang-client-0572544439")
            .setStorageBucket("gen-lang-client-0572544439.firebasestorage.app")
            .setGcmSenderId("496832475693")
            .build()

          app = FirebaseApp.initializeApp(context, fallbackOptions)
          initSource = "Explicit FirebaseOptions"
          Log.d(TAG, "Initialized via explicit FirebaseOptions fallback")
        } catch (e: Exception) {
          Log.e(TAG, "Explicit FirebaseOptions initialization failed: ${e.message}", e)
        }
      }

      if (app != null) {
        val options = app.options
        var authOk = false
        var firestoreOk = false
        var storageOk = false
        var serviceError: String? = null

        try {
          FirebaseAuth.getInstance(app)
          authOk = true
        } catch (e: Exception) {
          Log.w(TAG, "FirebaseAuth service test: ${e.message}")
          serviceError = "Auth: ${e.localizedMessage}"
        }

        try {
          FirebaseFirestore.getInstance(app)
          firestoreOk = true
        } catch (e: Exception) {
          Log.w(TAG, "FirebaseFirestore service test: ${e.message}")
          serviceError = (serviceError?.let { "$it | " } ?: "") + "Firestore: ${e.localizedMessage}"
        }

        try {
          FirebaseStorage.getInstance(app)
          storageOk = true
        } catch (e: Exception) {
          Log.w(TAG, "FirebaseStorage service test: ${e.message}")
          serviceError = (serviceError?.let { "$it | " } ?: "") + "Storage: ${e.localizedMessage}"
        }

        val info = FirebaseDiagnosticInfo(
          isInitialized = true,
          isAuthAvailable = authOk,
          isFirestoreAvailable = firestoreOk,
          isStorageAvailable = storageOk,
          projectId = options.projectId ?: "gen-lang-client-0572544439",
          appId = options.applicationId,
          storageBucket = options.storageBucket ?: "gen-lang-client-0572544439.firebasestorage.app",
          errorMessage = serviceError,
          initSource = initSource
        )
        _diagnostic.value = info
        return info
      } else {
        val errorInfo = FirebaseDiagnosticInfo(
          isInitialized = false,
          errorMessage = "FirebaseApp could not be initialized.",
          errorDetails = "Both automatic and programmatic fallback initializations failed. Check google-services.json and internet connectivity.",
          initSource = "Failed"
        )
        _diagnostic.value = errorInfo
        return errorInfo
      }
    } catch (t: Throwable) {
      Log.e(TAG, "Unexpected error during Firebase initialization", t)
      val errorInfo = FirebaseDiagnosticInfo(
        isInitialized = false,
        errorMessage = t.localizedMessage ?: "Unknown initialization error",
        errorDetails = Log.getStackTraceString(t),
        initSource = "Exception"
      )
      _diagnostic.value = errorInfo
      return errorInfo
    }
  }

  fun isReady(): Boolean = _diagnostic.value.isInitialized
}
