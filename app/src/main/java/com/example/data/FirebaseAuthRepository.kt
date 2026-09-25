package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.model.User
import com.example.util.FirebaseInitializer
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(private val context: Context) {
  init {
    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseInitializer.initialize(context)
      }
    } catch (t: Throwable) {
      Log.e("FirebaseAuthRepo", "Error during Firebase init in repo", t)
    }
  }

  private val auth: FirebaseAuth?
    get() = try {
      FirebaseAuth.getInstance()
    } catch (t: Throwable) {
      Log.w("FirebaseAuthRepo", "FirebaseAuth not available: ${t.message}")
      null
    }

  private val firestore: FirebaseFirestore?
    get() = try {
      FirebaseFirestore.getInstance()
    } catch (t: Throwable) {
      Log.w("FirebaseAuthRepo", "FirebaseFirestore not available: ${t.message}")
      null
    }

  private val credentialManager: CredentialManager? by lazy {
    try {
      CredentialManager.create(context)
    } catch (t: Throwable) {
      Log.w("FirebaseAuthRepo", "CredentialManager not supported on this device/runtime", t)
      null
    }
  }

  private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _userProfile = MutableStateFlow<User?>(null)
  val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

  init {
    try {
      auth?.let { a ->
        _currentUser.value = a.currentUser
        a.addAuthStateListener { firebaseAuth ->
          _currentUser.value = firebaseAuth.currentUser
        }
      }
    } catch (e: Throwable) {
      Log.w("FirebaseAuthRepo", "Failed to register auth state listener", e)
    }
  }

  fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val a = auth
    if (a == null) {
      trySend(null)
      awaitClose { }
      return@callbackFlow
    }
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      trySend(firebaseAuth.currentUser)
    }
    a.addAuthStateListener(listener)
    awaitClose {
      try {
        a.removeAuthStateListener(listener)
      } catch (e: Throwable) {
        // ignore
      }
    }
  }

  suspend fun loadUserProfile(uid: String): User? {
    val db = firestore ?: return null
    return try {
      val doc = db.collection("users").document(uid).get().await()
      if (doc.exists() && doc.data != null) {
        val user = User.fromMap(doc.data!!)
        _userProfile.value = user
        user
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w("FirebaseAuthRepo", "loadUserProfile error: ${e.message}")
      null
    }
  }

  suspend fun isUsernameAvailable(username: String): Boolean {
    val clean = username.trim().lowercase()
    if (clean.length < 3 || clean.length > 20) return false
    val validRegex = Regex("^[a-z0-9_]+$")
    if (!validRegex.matches(clean)) return false

    val db = firestore ?: return true
    val a = auth
    return try {
      val doc = db.collection("usernames").document(clean).get().await()
      !doc.exists() || doc.getString("uid") == a?.currentUser?.uid
    } catch (e: Exception) {
      Log.w("FirebaseAuthRepo", "isUsernameAvailable check error: ${e.message}")
      true
    }
  }

  suspend fun createUserProfile(
    displayName: String,
    username: String,
    bio: String,
    photoUrl: String
  ): Result<User> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Authentication is not available"))
    val db = firestore ?: return Result.failure(IllegalStateException("Cloud Firestore is not available"))
    val currentFirebaseUser = a.currentUser
      ?: return Result.failure(IllegalStateException("User is not authenticated"))

    val cleanUsername = username.trim().lowercase()
    val available = isUsernameAvailable(cleanUsername)
    if (!available) {
      return Result.failure(IllegalArgumentException("Username @$cleanUsername is already taken"))
    }

    val user = User(
      uid = currentFirebaseUser.uid,
      displayName = displayName.ifEmpty { currentFirebaseUser.displayName ?: "Pinggo User" },
      username = cleanUsername,
      email = currentFirebaseUser.email ?: "",
      photoURL = photoUrl.ifEmpty { currentFirebaseUser.photoUrl?.toString() ?: "" },
      bio = bio.ifEmpty { "Hey there! I am using Pinggo 🐧" },
      createdAt = System.currentTimeMillis(),
      lastSeen = System.currentTimeMillis(),
      isOnline = true
    )

    return try {
      db.collection("users").document(user.uid).set(user.toMap()).await()
      db.collection("usernames").document(cleanUsername).set(mapOf("uid" to user.uid)).await()
      _userProfile.value = user
      Result.success(user)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "createUserProfile failed", e)
      Result.failure(e)
    }
  }

  suspend fun updateUserProfile(updated: User): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
    return try {
      db.collection("users").document(updated.uid).update(updated.toMap()).await()
      _userProfile.value = updated
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun getWebClientIdFromResources(): String? {
    return try {
      val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
      if (resId != 0) context.getString(resId).takeIf { it.isNotBlank() } else null
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Real Google Sign-In using Jetpack Credential Manager
   */
  suspend fun signInWithGoogle(webClientId: String? = null): Result<FirebaseUser> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    val cm = credentialManager ?: return Result.failure(
      IllegalStateException("Credential Manager is not available on this device or Google Play Services is missing.")
    )

    return try {
      val resolvedClientId = webClientId?.takeIf { it.isNotBlank() }
        ?: getWebClientIdFromResources()

      if (resolvedClientId.isNullOrBlank() || resolvedClientId.contains("pinggo.apps")) {
        return Result.failure(
          IllegalStateException(
            "CONFIGURATION_NOT_FOUND: Google Sign-In requires an OAuth 2.0 Web Client ID in the Firebase project. Please enable Google provider in Firebase Console and ensure google-services.json includes the web client ID."
          )
        )
      }

      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(resolvedClientId)
        .setAutoSelectEnabled(false)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = cm.getCredential(
        context = context,
        request = request
      )

      when (val credential = result.credential) {
        is CustomCredential -> {
          if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = a.signInWithCredential(authCredential).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user is null")
            _currentUser.value = user
            Result.success(user)
          } else {
            Result.failure(IllegalArgumentException("Unsupported credential type: ${credential.type}"))
          }
        }
        else -> {
          Result.failure(IllegalArgumentException("Unexpected credential type: ${credential.javaClass.name}"))
        }
      }
    } catch (e: GetCredentialException) {
      Result.failure(e)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Alternative authentication method (Email or Direct Pinggo Account)
   * allowing users to immediately log into real Firebase backend
   */
  suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    return try {
      val res = a.signInWithEmailAndPassword(email.trim(), password).await()
      val user = res.user ?: throw IllegalStateException("Firebase user is null")
      _currentUser.value = user
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    return try {
      val res = a.createUserWithEmailAndPassword(email.trim(), password).await()
      val user = res.user ?: throw IllegalStateException("Firebase user is null")
      _currentUser.value = user
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signInAnonymously(): Result<FirebaseUser> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    return try {
      val res = a.signInAnonymously().await()
      val user = res.user ?: throw IllegalStateException("Firebase user is null")
      _currentUser.value = user
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signOut() {
    try {
      val a = auth
      val db = firestore
      a?.currentUser?.let { user ->
        db?.collection("users")?.document(user.uid)?.update(
          mapOf("isOnline" to false, "lastSeen" to System.currentTimeMillis())
        )?.await()
      }
      credentialManager?.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      // ignore
    } finally {
      auth?.signOut()
      _currentUser.value = null
      _userProfile.value = null
    }
  }
}
