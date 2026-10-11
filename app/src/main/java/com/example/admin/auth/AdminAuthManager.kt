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

  private val _isAuthorized = MutableStateFlow(false)
  val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

  init {
    val current = auth?.currentUser
    if (current != null) {
      val email = current.email?.lowercase() ?: ""
      if (isEmailAuthorized(email)) {
        _currentAdminUser.value = current
        _isAuthorized.value = true
      } else {
        auth?.signOut()
        _currentAdminUser.value = null
        _isAuthorized.value = false
      }
    }
  }

  fun isEmailAuthorized(email: String): Boolean {
    val clean = email.trim().lowercase()
    return trustedAdminEmails.contains(clean)
  }

  suspend fun signInAdmin(email: String, password: String): Result<FirebaseUser> {
    val cleanEmail = email.trim().lowercase()
    val cleanPassword = password.trim()

    if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
      return Result.failure(IllegalArgumentException("Email and password cannot be empty."))
    }

    // Pre-flight authorization check
    if (!isEmailAuthorized(cleanEmail)) {
      return Result.failure(
        SecurityException("Access Denied: '$cleanEmail' is not an authorized Pinggo Administrator.")
      )
    }

    val firebase = auth ?: return Result.failure(IllegalStateException("Firebase Auth service is unavailable."))

    return try {
      val authResult = firebase.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
      val user = authResult.user

      if (user == null) {
        return Result.failure(IllegalStateException("Failed to retrieve user session."))
      }

      // Verify token email
      val userEmail = user.email?.lowercase() ?: ""
      if (!isEmailAuthorized(userEmail)) {
        firebase.signOut()
        _currentAdminUser.value = null
        _isAuthorized.value = false
        return Result.failure(
          SecurityException("Access Denied: Your account does not possess administrator privileges.")
        )
      }

      _currentAdminUser.value = user
      _isAuthorized.value = true
      Result.success(user)
    } catch (e: Exception) {
      _currentAdminUser.value = null
      _isAuthorized.value = false
      val message = when {
        e.message?.contains("no user record", ignoreCase = true) == true ->
          "No administrator account found for $cleanEmail. Please create your admin account or request setup."
        e.message?.contains("password is invalid", ignoreCase = true) == true ||
        e.message?.contains("wrong-password", ignoreCase = true) == true ->
          "Invalid administrator password. Please try again."
        e.message?.contains("network", ignoreCase = true) == true ->
          "Network connection issue. Please check your internet connection."
        else -> e.message ?: "Authentication failed."
      }
      Result.failure(Exception(message))
    }
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
