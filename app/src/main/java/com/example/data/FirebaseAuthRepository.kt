package com.example.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.model.User
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
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
  }

  private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
  private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
  private val credentialManager: CredentialManager = CredentialManager.create(context)

  private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _userProfile = MutableStateFlow<User?>(null)
  val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

  init {
    try {
      _currentUser.value = auth.currentUser
      auth.addAuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      trySend(firebaseAuth.currentUser)
    }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
  }

  suspend fun loadUserProfile(uid: String): User? {
    return try {
      val doc = firestore.collection("users").document(uid).get().await()
      if (doc.exists() && doc.data != null) {
        val user = User.fromMap(doc.data!!)
        _userProfile.value = user
        user
      } else {
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  suspend fun isUsernameAvailable(username: String): Boolean {
    val clean = username.trim().lowercase()
    val bare = clean.removePrefix("@")
    if (bare.length < 3 || bare.length > 20) return false
    val validRegex = Regex("^@?[a-z0-9_]+$")
    if (!validRegex.matches(clean)) return false

    return try {
      val docClean = firestore.collection("usernames").document(clean).get().await()
      val docBare = if (clean != bare) firestore.collection("usernames").document(bare).get().await() else null
      val currentUid = auth.currentUser?.uid
      val cleanOk = !docClean.exists() || docClean.getString("uid") == currentUid
      val bareOk = docBare == null || !docBare.exists() || docBare.getString("uid") == currentUid
      cleanOk && bareOk
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun createUserProfile(
    displayName: String,
    username: String,
    bio: String,
    photoUrl: String
  ): Result<User> {
    val currentFirebaseUser = auth.currentUser
      ?: return Result.failure(IllegalStateException("User is not authenticated"))

    val cleanUsername = username.trim().lowercase()
    val bare = cleanUsername.removePrefix("@")
    val available = isUsernameAvailable(cleanUsername)
    if (!available) {
      return Result.failure(IllegalArgumentException("Username @$bare is already taken"))
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
      // 1. Save to users collection
      firestore.collection("users").document(user.uid).set(user.toMap()).await()
      // 2. Reserve username in usernames collection (both clean and bare)
      firestore.collection("usernames").document(cleanUsername).set(mapOf("uid" to user.uid)).await()
      if (cleanUsername != bare) {
        firestore.collection("usernames").document(bare).set(mapOf("uid" to user.uid)).await()
      }
      _userProfile.value = user
      Result.success(user)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  suspend fun updateUserProfile(updated: User): Result<Unit> {
    return try {
      firestore.collection("users").document(updated.uid).update(updated.toMap()).await()
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

      val result = credentialManager.getCredential(
        context = context,
        request = request
      )

      when (val credential = result.credential) {
        is CustomCredential -> {
          if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = auth.signInWithCredential(authCredential).await()
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
    return try {
      val res = auth.signInWithEmailAndPassword(email.trim(), password).await()
      val user = res.user ?: throw IllegalStateException("Firebase user is null")
      _currentUser.value = user
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
    return try {
      val res = auth.createUserWithEmailAndPassword(email.trim(), password).await()
      val user = res.user ?: throw IllegalStateException("Firebase user is null")
      _currentUser.value = user
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signInAnonymously(): Result<FirebaseUser> {
    return try {
      val res = auth.signInAnonymously().await()
      val user = res.user ?: throw IllegalStateException("Firebase user is null")
      _currentUser.value = user
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signOut() {
    try {
      auth.currentUser?.let { user ->
        firestore.collection("users").document(user.uid).update(
          mapOf("isOnline" to false, "lastSeen" to System.currentTimeMillis())
        ).await()
      }
      credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
    } catch (e: Exception) {
      // ignore
    } finally {
      auth.signOut()
      _currentUser.value = null
      _userProfile.value = null
    }
  }
}
