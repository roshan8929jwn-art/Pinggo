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
import android.net.Uri
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
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

  private val functions: FirebaseFunctions?
    get() = try {
      FirebaseFunctions.getInstance()
    } catch (t: Throwable) {
      Log.w("FirebaseAuthRepo", "FirebaseFunctions not available: ${t.message}")
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
    val clean = username.trim().lowercase().removePrefix("@")
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

  suspend fun resolveEmailForUsername(usernameOrEmail: String): String? {
    val input = usernameOrEmail.trim()
    if (input.contains("@") && input.contains(".")) {
      return input
    }
    val clean = input.lowercase().removePrefix("@")
    val db = firestore ?: return null
    return try {
      val doc = db.collection("usernames").document(clean).get().await()
      if (doc.exists() && !doc.getString("email").isNullOrBlank()) {
        doc.getString("email")
      } else {
        val userQuery = db.collection("users")
          .whereEqualTo("username", clean)
          .limit(1)
          .get()
          .await()
        userQuery.documents.firstOrNull()?.getString("email")
      }
    } catch (e: Exception) {
      Log.w("FirebaseAuthRepo", "resolveEmailForUsername error: ${e.message}")
      null
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

    val cleanUsername = username.trim().lowercase().removePrefix("@")
    val available = isUsernameAvailable(cleanUsername)
    if (!available) {
      return Result.failure(IllegalArgumentException("Username @$cleanUsername is already taken"))
    }

    val user = User(
      uid = currentFirebaseUser.uid,
      displayName = displayName.ifEmpty { currentFirebaseUser.displayName ?: "Pinggo User" },
      username = cleanUsername,
      usernameLowercase = cleanUsername,
      email = currentFirebaseUser.email ?: "",
      photoURL = photoUrl.ifEmpty { currentFirebaseUser.photoUrl?.toString() ?: "" },
      bio = bio.ifEmpty { "Hey there! I am using Pinggo 🐧" },
      createdAt = System.currentTimeMillis(),
      lastSeen = System.currentTimeMillis(),
      isOnline = true
    )

    return try {
      db.collection("users").document(user.uid).set(user.toMap()).await()
      db.collection("usernames").document(cleanUsername).set(
        mapOf("uid" to user.uid, "email" to user.email)
      ).await()
      _userProfile.value = user
      Result.success(user)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "createUserProfile failed", e)
      Result.failure(e)
    }
  }

  suspend fun updateUserProfileWithUsernameChange(
    displayName: String,
    newUsername: String,
    bio: String,
    photoUrl: String
  ): Result<User> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
    val currentFirebaseUser = a.currentUser
      ?: return Result.failure(IllegalStateException("User is not authenticated"))

    val currentProfile = _userProfile.value ?: loadUserProfile(currentFirebaseUser.uid)
    val oldUsername = currentProfile?.username?.lowercase()?.removePrefix("@") ?: ""
    val cleanNewUsername = newUsername.trim().lowercase().removePrefix("@")

    if (cleanNewUsername.length < 3 || cleanNewUsername.length > 20 || !Regex("^[a-z0-9_]+$").matches(cleanNewUsername)) {
      return Result.failure(IllegalArgumentException("Username must be 3-20 characters (a-z, 0-9, _)"))
    }

    if (cleanNewUsername != oldUsername) {
      val available = isUsernameAvailable(cleanNewUsername)
      if (!available) {
        return Result.failure(IllegalArgumentException("Username @$cleanNewUsername is already taken"))
      }
    }

    val updatedUser = (currentProfile ?: User(uid = currentFirebaseUser.uid)).copy(
      displayName = displayName.ifEmpty { currentProfile?.displayName ?: "Pinggo User" },
      username = cleanNewUsername,
      usernameLowercase = cleanNewUsername,
      bio = bio,
      photoURL = photoUrl.ifEmpty { currentProfile?.photoURL ?: "" }
    )

    return try {
      db.runTransaction { transaction ->
        if (cleanNewUsername != oldUsername) {
          if (oldUsername.isNotEmpty()) {
            transaction.delete(db.collection("usernames").document(oldUsername))
          }
          transaction.set(
            db.collection("usernames").document(cleanNewUsername),
            mapOf("uid" to currentFirebaseUser.uid, "email" to updatedUser.email)
          )
        }
        transaction.set(db.collection("users").document(currentFirebaseUser.uid), updatedUser.toMap())
      }.await()

      _userProfile.value = updatedUser
      Result.success(updatedUser)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "updateUserProfileWithUsernameChange error: ${e.message}")
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
      val secretId: String = "" // Fallback if BuildConfig field is missing
      // Attempt to get from BuildConfig if it exists, otherwise empty
      val resolvedSecretId = try {
          val field = com.example.BuildConfig::class.java.getField("GOOGLE_WEB_CLIENT_ID")
          field.get(null) as String
      } catch (e: Exception) { "" }

      val resolvedClientId: String? = webClientId?.takeIf { it.isNotBlank() }
        ?: resolvedSecretId.takeIf { it.isNotBlank() && !it.startsWith("YOUR_WEB") }
        ?: getWebClientIdFromResources()
        ?: "496832475693-2n35psfvke0hlq7v016btdq803d03bfe.apps.googleusercontent.com" // Hardcoded project-specific fallback

      Log.d("FirebaseAuthRepo", "Resolved Google Client ID: $resolvedClientId")

      if (resolvedClientId.isNullOrBlank()) {
        return Result.failure(
          IllegalStateException(
            "Google Sign-In configuration is missing. Project: gen-lang-client-0572544439. " +
            "Please ensure you have added the 'GOOGLE_WEB_CLIENT_ID' to your app secrets in AI Studio."
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
      loadUserProfile(user.uid)
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signInWithUsernameOrEmail(identifier: String, password: String): Result<FirebaseUser> {
    val trimmed = identifier.trim()
    val resolvedEmail = resolveEmailForUsername(trimmed)
      ?: return Result.failure(IllegalArgumentException("No Pinggo account found for \"$trimmed\""))
    return signInWithEmail(resolvedEmail, password)
  }

  suspend fun registerGuestWithEmailPassword(
    username: String,
    email: String,
    password: String,
    displayName: String
  ): Result<User> {
    val cleanUsername = username.trim().lowercase().removePrefix("@")
    if (cleanUsername.length < 3 || cleanUsername.length > 20 || !Regex("^[a-z0-9_]+$").matches(cleanUsername)) {
      return Result.failure(IllegalArgumentException("Username must be 3-20 characters with letters, numbers, or _"))
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
      return Result.failure(IllegalArgumentException("Please enter a valid email address"))
    }
    if (password.length < 6) {
      return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
    }
    if (!isUsernameAvailable(cleanUsername)) {
      return Result.failure(IllegalArgumentException("Username @$cleanUsername is already taken"))
    }

    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))

    return try {
      val res = a.createUserWithEmailAndPassword(email.trim(), password).await()
      val firebaseUser = res.user ?: throw IllegalStateException("Firebase user creation failed")
      _currentUser.value = firebaseUser

      val newUser = User(
        uid = firebaseUser.uid,
        displayName = displayName.ifEmpty { cleanUsername },
        username = cleanUsername,
        usernameLowercase = cleanUsername,
        email = email.trim(),
        photoURL = "",
        bio = "Hey there! I am using Pinggo 🐧",
        createdAt = System.currentTimeMillis(),
        lastSeen = System.currentTimeMillis(),
        isOnline = true
      )

      db.collection("users").document(newUser.uid).set(newUser.toMap()).await()
      db.collection("usernames").document(cleanUsername).set(
        mapOf("uid" to newUser.uid, "email" to email.trim())
      ).await()
      _userProfile.value = newUser

      try {
        firebaseUser.sendEmailVerification().await()
      } catch (ve: Exception) {
        Log.w("FirebaseAuthRepo", "Verification email send error: ${ve.message}")
      }

      Result.success(newUser)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun sendEmailVerification(): Result<Unit> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    val user = a.currentUser ?: return Result.failure(IllegalStateException("No authenticated user"))
    return try {
      user.sendEmailVerification().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun sendSignInLinkToEmail(email: String): Result<Unit> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    val actionCodeSettings = ActionCodeSettings.newBuilder()
      .setUrl("https://gen-lang-client-0572544439.firebaseapp.com/login?email=${Uri.encode(email.trim())}")
      .setHandleCodeInApp(true)
      .setAndroidPackageName("com.aistudio.pinggo.vuxowh", true, "1")
      .build()
    return try {
      a.sendSignInLinkToEmail(email.trim(), actionCodeSettings).await()
      val prefs = context.getSharedPreferences("pinggo_auth_prefs", Context.MODE_PRIVATE)
      prefs.edit().putString("email_for_sign_in", email.trim()).apply()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signInWithEmailLink(email: String, emailLink: String): Result<FirebaseUser> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
    return try {
      if (!a.isSignInWithEmailLink(emailLink)) {
        return Result.failure(IllegalArgumentException("Invalid or expired sign-in link"))
      }
      val res = a.signInWithEmailLink(email.trim(), emailLink).await()
      val user = res.user ?: throw IllegalStateException("Firebase user is null")
      _currentUser.value = user
      loadUserProfile(user.uid)
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateRingtoneSettings(
    userId: String,
    callRingtoneUri: String? = null,
    callRingtoneTitle: String? = null,
    notificationRingtoneUri: String? = null,
    notificationRingtoneTitle: String? = null
  ): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
    return try {
      val updates = mutableMapOf<String, Any>()
      callRingtoneUri?.let { updates["callRingtoneUri"] = it }
      callRingtoneTitle?.let { updates["callRingtoneTitle"] = it }
      notificationRingtoneUri?.let { updates["notificationRingtoneUri"] = it }
      notificationRingtoneTitle?.let { updates["notificationRingtoneTitle"] = it }

      if (updates.isNotEmpty()) {
        db.collection("users").document(userId).update(updates).await()
        val current = _userProfile.value
        if (current != null && current.uid == userId) {
          _userProfile.value = current.copy(
            callRingtoneUri = callRingtoneUri ?: current.callRingtoneUri,
            callRingtoneTitle = callRingtoneTitle ?: current.callRingtoneTitle,
            notificationRingtoneUri = notificationRingtoneUri ?: current.notificationRingtoneUri,
            notificationRingtoneTitle = notificationRingtoneTitle ?: current.notificationRingtoneTitle
          )
        }
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updatePrivacySettings(
    userId: String,
    lastSeen: String,
    readReceipts: Boolean,
    statusPrivacy: String
  ): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
    return try {
      db.collection("users").document(userId).update(
        mapOf(
          "privacyLastSeen" to lastSeen,
          "privacyReadReceipts" to readReceipts,
          "privacyStatus" to statusPrivacy
        )
      ).await()
      val current = _userProfile.value
      if (current != null && current.uid == userId) {
        _userProfile.value = current.copy(
          privacyLastSeen = lastSeen,
          privacyReadReceipts = readReceipts,
          privacyStatus = statusPrivacy
        )
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateNotificationSettings(
    userId: String,
    message: Boolean,
    group: Boolean,
    preview: Boolean,
    call: Boolean
  ): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
    return try {
      db.collection("users").document(userId).update(
        mapOf(
          "notificationMessage" to message,
          "notificationGroup" to group,
          "notificationPreview" to preview,
          "notificationCall" to call
        )
      ).await()
      val current = _userProfile.value
      if (current != null && current.uid == userId) {
        _userProfile.value = current.copy(
          notificationMessage = message,
          notificationGroup = group,
          notificationPreview = preview,
          notificationCall = call
        )
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun blockUser(currentUserId: String, targetUserId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
    return try {
      val userRef = db.collection("users").document(currentUserId)
      db.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)
        @Suppress("UNCHECKED_CAST")
        val blocked = (snapshot.get("blockedUsers") as? List<String>)?.toMutableList() ?: mutableListOf()
        if (!blocked.contains(targetUserId)) {
          blocked.add(targetUserId)
        }
        transaction.update(userRef, "blockedUsers", blocked)
      }.await()

      db.collection("blockedUsers").document(currentUserId)
        .collection("blocked").document(targetUserId)
        .set(mapOf("blockedAt" to System.currentTimeMillis())).await()

      loadUserProfile(currentUserId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun unblockUser(currentUserId: String, targetUserId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available"))
    return try {
      val userRef = db.collection("users").document(currentUserId)
      db.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)
        @Suppress("UNCHECKED_CAST")
        val blocked = (snapshot.get("blockedUsers") as? List<String>)?.toMutableList() ?: mutableListOf()
        blocked.remove(targetUserId)
        transaction.update(userRef, "blockedUsers", blocked)
      }.await()

      db.collection("blockedUsers").document(currentUserId)
        .collection("blocked").document(targetUserId)
        .delete().await()

      loadUserProfile(currentUserId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun fetchUsersByIds(uids: List<String>): List<User> {
    if (uids.isEmpty()) return emptyList()
    val db = firestore ?: return emptyList()
    return try {
      val results = mutableListOf<User>()
      for (uid in uids) {
        val doc = db.collection("users").document(uid).get().await()
        if (doc.exists() && doc.data != null) {
          results.add(User.fromMap(doc.data!!))
        }
      }
      results
    } catch (e: Exception) {
      emptyList()
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

  suspend fun linkEmailPassword(email: String, password: String): Result<Unit> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth not ready"))
    val user = a.currentUser ?: return Result.failure(IllegalStateException("No authenticated user"))
    return try {
      val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
      user.linkWithCredential(credential).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "linkEmailPassword failed", e)
      Result.failure(e)
    }
  }

  suspend fun sendOtp(email: String): Result<Unit> {
    val f = functions ?: return Result.failure(IllegalStateException("Firebase Functions not ready"))
    return try {
      val data = hashMapOf("email" to email)
      f.getHttpsCallable("sendOtp")
        .call(data)
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "sendOtp call failed", e)
      // Fallback for demo if functions are not deployed:
      // return Result.failure(e)
      
      // FOR DEMO PURPOSES ONLY: if the function is not found (not deployed), 
      // we'll log it and tell the user they need to deploy the provided functions.
      Result.failure(Exception("Cloud Function 'sendOtp' not found or failed. Please deploy the provided firebase_otp_functions.js to your Firebase project."))
    }
  }

  suspend fun verifyOtp(email: String, otp: String): Result<Unit> {
    val f = functions ?: return Result.failure(IllegalStateException("Firebase Functions not ready"))
    return try {
      val data = hashMapOf("email" to email, "otp" to otp)
      f.getHttpsCallable("verifyOtp")
        .call(data)
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "verifyOtp call failed", e)
      Result.failure(Exception("OTP Verification failed: ${e.message}. Ensure backend is deployed."))
    }
  }

  suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth not ready"))
    return try {
      a.sendPasswordResetEmail(email.trim()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "sendPasswordResetEmail failed", e)
      Result.failure(e)
    }
  }

  suspend fun updatePassword(password: String): Result<Unit> {
    val a = auth ?: return Result.failure(IllegalStateException("Firebase Auth not ready"))
    val user = a.currentUser ?: return Result.failure(IllegalStateException("No authenticated user"))
    return try {
      user.updatePassword(password).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("FirebaseAuthRepo", "updatePassword failed", e)
      Result.failure(e)
    }
  }
}
