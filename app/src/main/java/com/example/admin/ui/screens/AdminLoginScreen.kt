package com.example.admin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin.auth.AdminAuthManager
import com.example.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminLoginScreen(
  authManager: AdminAuthManager,
  onLoginSuccess: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showForgotPasswordDialog by remember { mutableStateOf(false) }
  var resetMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .imePadding(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Shield Logo & Branding
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(AdminPinkPrimary.copy(alpha = 0.12f))
          .border(1.5.dp, AdminPinkPrimary.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Admin Shield",
          tint = AdminPinkPrimary,
          modifier = Modifier.size(42.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Pinggo Admin",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = AdminTextPrimary,
        letterSpacing = (-0.5).sp
      )

      Text(
        text = "Secure Administration Portal",
        style = MaterialTheme.typography.bodyMedium,
        color = AdminTextSecondary,
        fontWeight = FontWeight.Medium
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Frosted Glass Login Card
      AdminGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = 12.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Administrator Sign In",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AdminTextPrimary
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Enter your verified administrator credentials to access the console.",
            style = MaterialTheme.typography.bodySmall,
            color = AdminTextSecondary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Email Field
          OutlinedTextField(
            value = email,
            onValueChange = {
              email = it
              errorMessage = null
            },
            label = { Text("Admin Email Address") },
            placeholder = { Text("roshan8929jwn@gmail.com") },
            leadingIcon = {
              Icon(Icons.Default.Email, "Email", tint = AdminPinkPrimary)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AdminPinkPrimary,
              unfocusedBorderColor = Color(0xFFCBD5E1),
              focusedContainerColor = Color.White.copy(alpha = 0.9f),
              unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
            )
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Password Field
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              errorMessage = null
            },
            label = { Text("Master Password") },
            placeholder = { Text("Enter your password") },
            leadingIcon = {
              Icon(Icons.Default.Lock, "Password", tint = AdminPinkPrimary)
            },
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = "Toggle password",
                  tint = AdminTextSecondary
                )
              }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                focusManager.clearFocus()
                if (email.isNotBlank() && password.isNotBlank()) {
                  isLoading = true
                  coroutineScope.launch {
                    val result = authManager.signInAdmin(email, password)
                    isLoading = false
                    if (result.isSuccess) {
                      onLoginSuccess()
                    } else {
                      errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed."
                    }
                  }
                }
              }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AdminPinkPrimary,
              unfocusedBorderColor = Color(0xFFCBD5E1),
              focusedContainerColor = Color.White.copy(alpha = 0.9f),
              unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
            )
          )

          // Error Message Alert
          AnimatedVisibility(visible = errorMessage != null) {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
              color = AdminRuby.copy(alpha = 0.12f),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, AdminRuby.copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.ErrorOutline, null, tint = AdminRuby, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = errorMessage ?: "",
                  color = AdminRuby,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Sign In Button
          Button(
            onClick = {
              focusManager.clearFocus()
              if (email.isBlank() || password.isBlank()) {
                errorMessage = "Please enter both administrator email and password."
                return@Button
              }
              isLoading = true
              errorMessage = null
              coroutineScope.launch {
                val res = authManager.signInAdmin(email, password)
                isLoading = false
                if (res.isSuccess) {
                  onLoginSuccess()
                } else {
                  errorMessage = res.exceptionOrNull()?.message ?: "Authentication failed."
                }
              }
            },
            enabled = !isLoading,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AdminPinkPrimary,
              contentColor = Color.White
            )
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
              )
            } else {
              Icon(Icons.Default.Key, null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Authenticate Administrator",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Forgot Password Option
          TextButton(onClick = { showForgotPasswordDialog = true }) {
            Text(
              text = "Forgot Administrator Password?",
              color = AdminPinkPrimary,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Security Notice Footer
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.GppGood,
          contentDescription = null,
          tint = AdminEmerald,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Protected by Firebase Security Rules & Admin Token Claims",
          style = MaterialTheme.typography.labelSmall,
          color = AdminTextSecondary,
          textAlign = TextAlign.Center
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Password Reset Dialog
  if (showForgotPasswordDialog) {
    var resetEmail by remember { mutableStateOf(email) }
    var isResetting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
      AdminGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text(
            text = "Reset Admin Password",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AdminTextPrimary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "A password reset link will be sent to your registered administrator email address.",
            style = MaterialTheme.typography.bodySmall,
            color = AdminTextSecondary
          )
          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = resetEmail,
            onValueChange = { resetEmail = it },
            label = { Text("Administrator Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
          )

          resetMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(msg, color = AdminCobalt, style = MaterialTheme.typography.bodySmall)
          }

          Spacer(modifier = Modifier.height(18.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showForgotPasswordDialog = false }) {
              Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (resetEmail.isNotBlank()) {
                  isResetting = true
                  coroutineScope.launch {
                    val r = authManager.sendPasswordReset(resetEmail)
                    isResetting = false
                    if (r.isSuccess) {
                      resetMessage = "Password reset email sent! Please check your inbox."
                    } else {
                      resetMessage = r.exceptionOrNull()?.message ?: "Failed to send reset link."
                    }
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = AdminPinkPrimary),
              enabled = !isResetting
            ) {
              Text("Send Reset Link")
            }
          }
        }
      }
    }
  }
}
