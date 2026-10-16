package com.example.admin.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AdminAuthManager(private val context: Context) {
  private val auth: FirebaseAuth?
    get() = try {
      FirebaseAuth.getInstance()
    } catch (e: Throwable) {
      Log.w("AdminAuthManager", "FirebaseAuth not ready: ${e.message}")
      null
    }

  // Trusted Administrator Allowlist (Authorized Server-Side via Security Rules)
  private val trustedAdminEmails = setOf(
    "roshan8929jwn@gmail.com"
  )

  private val _currentAdminUser = MutableStateFlow<FirebaseUser?>(null)
  val currentAdminUser: StateFlow<FirebaseUser?> = _currentAdminUser.asStateFlow()

  private val _adminEmail = MutableStateFlow("roshan8929jwn@gmail.com")
  val adminEmail: StateFlow<String> = _adminEmail.asStateFlow()

  private val _isAuthorized = MutableStateFlow(false)
  val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

  init {
    val current = auth?.currentUser
    if (current != null) {
      val email = current.email?.lowercase() ?: ""
      if (isEmailAuthorized(email)) {
        _currentAdminUser.value = current
        _adminEmail.value = email
        _isAuthorized.value = true
      }
    }
  }

  fun isEmailAuthorized(email: String): Boolean {
    val clean = email.trim().lowercase()
    return trustedAdminEmails.contains(clean)
  }

  suspend fun signInWithMasterAccess(email: String = "roshan8929jwn@gmail.com"): Result<Unit> {
    val cleanEmail = email.trim().lowercase()
    if (!isEmailAuthorized(cleanEmail)) {
      return Result.failure(SecurityException("Access Denied: '$cleanEmail' is not an authorized administrator."))
    }
    _adminEmail.value = cleanEmail
    _isAuthorized.value = true

    auth?.currentUser?.let { user ->
      _currentAdminUser.value = user
    }
    return Result.success(Unit)
  }

  suspend fun signInAdmin(email: String, password: String): Result<Unit> {
    val cleanEmail = email.trim().lowercase()
    val cleanPassword = password.trim()

    if (cleanEmail.isEmpty()) {
      return Result.failure(IllegalArgumentException("Please enter the administrator email."))
    }

    if (!isEmailAuthorized(cleanEmail)) {
      return Result.failure(
        SecurityException("Access Denied: '$cleanEmail' is not an authorized Pinggo Administrator.")
      )
    }

    // Master Key check for instant owner access
    if (cleanPassword == "admin777" || cleanPassword == "pinggo777" || cleanPassword == "7777" || cleanPassword == "roshan777") {
      _adminEmail.value = cleanEmail
      _isAuthorized.value = true
      auth?.currentUser?.let { _currentAdminUser.value = it }
      return Result.success(Unit)
    }

    val firebase = auth
    if (firebase != null) {
      // 1. Try standard email/password authentication
      try {
        val authResult = firebase.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
        val user = authResult.user
        if (user != null) {
          _currentAdminUser.value = user
          _adminEmail.value = cleanEmail
          _isAuthorized.value = true
          return Result.success(Unit)
        }
      } catch (e: Exception) {
        // 2. If password account does not exist yet, register it with this password
        try {
          val createResult = firebase.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
          val user = createResult.user
          if (user != null) {
            _currentAdminUser.value = user
            _adminEmail.value = cleanEmail
            _isAuthorized.value = true
            return Result.success(Unit)
          }
        } catch (createEx: Exception) {
          Log.w("AdminAuthManager", "Create attempt info: ${createEx.message}")
        }
      }
    }

    // If password is entered for owner roshan8929jwn@gmail.com, grant access
    if (cleanEmail == "roshan8929jwn@gmail.com" && cleanPassword.isNotEmpty()) {
      _adminEmail.value = cleanEmail
      _isAuthorized.value = true
      auth?.currentUser?.let { _currentAdminUser.value = it }
      return Result.success(Unit)
    }

    return Result.failure(Exception("Incorrect password. Master password is: admin777"))
  }

  suspend fun sendPasswordReset(email: String): Result<Unit> {
    val cleanEmail = email.trim().lowercase()
    if (!isEmailAuthorized(cleanEmail)) {
      return Result.failure(SecurityException("Password reset is only available for authorized administrator emails."))
    }

    val firebase = auth ?: return Result.failure(IllegalStateException("Firebase Auth unavailable."))
    return try {
      firebase.sendPasswordResetEmail(cleanEmail).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun signOut() {
    try {
      auth?.signOut()
    } catch (e: Throwable) {
      Log.w("AdminAuthManager", "SignOut error: ${e.message}")
    }
    _currentAdminUser.value = null
    _isAuthorized.value = false
  }
}
