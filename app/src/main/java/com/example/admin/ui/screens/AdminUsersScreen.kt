package com.example.admin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.admin.data.AdminRepository
import com.example.admin.ui.theme.*
import com.example.model.User
import com.example.model.handle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminUsersScreen(
  repository: AdminRepository,
  currentAdminEmail: String,
  allUsers: List<User>
) {
  val coroutineScope = rememberCoroutineScope()
  var searchQuery by remember { mutableStateOf("") }
  var filterStatus by remember { mutableStateOf("All") } // "All", "Online", "Verified", "Banned"
  var selectedUser by remember { mutableStateOf<User?>(null) }
  var actionStatusMessage by remember { mutableStateOf<String?>(null) }

  val filteredUsers = remember(allUsers, searchQuery, filterStatus) {
    allUsers.filter { user ->
      val matchesQuery = searchQuery.isEmpty() ||
        user.displayName.contains(searchQuery, ignoreCase = true) ||
        user.username.contains(searchQuery, ignoreCase = true) ||
        user.email.contains(searchQuery, ignoreCase = true) ||
        user.uid.contains(searchQuery, ignoreCase = true)

      val matchesFilter = when (filterStatus) {
        "Online" -> user.isOnline
        "Verified" -> user.isVerified
        "Banned" -> user.isBanned
        else -> true
      }
      matchesQuery && matchesFilter
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search ${allUsers.size} users by name, @username, or email...") },
      leadingIcon = { Icon(Icons.Default.Search, null, tint = AdminPinkPrimary) },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, "Clear search", tint = AdminTextSecondary)
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier.fillMaxWidth(),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AdminPinkPrimary,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedContainerColor = Color.White.copy(alpha = 0.9f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filter Chips Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("All", "Online", "Verified", "Banned").forEach { status ->
        val isSelected = filterStatus == status
        FilterChip(
          selected = isSelected,
          onClick = { filterStatus = status },
          label = { Text(status) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AdminPinkPrimary.copy(alpha = 0.15f),
            selectedLabelColor = AdminPinkPrimary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) AdminPinkPrimary else Color(0xFFCBD5E1)
          ),
          shape = RoundedCornerShape(16.dp)
        )
      }
    }

    // Status Notification Banner
    AnimatedVisibility(visible = actionStatusMessage != null) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        color = AdminEmerald.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, null, tint = AdminEmerald, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(actionStatusMessage ?: "", color = AdminEmerald, style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.weight(1f))
          IconButton(onClick = { actionStatusMessage = null }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, null, tint = AdminEmerald, modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Users List
    if (filteredUsers.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.PersonSearch, null, tint = AdminTextSecondary, modifier = Modifier.size(48.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("No registered users match your filter", color = AdminTextSecondary, fontWeight = FontWeight.Medium)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
      ) {
        items(filteredUsers, key = { it.uid }) { user ->
          AdminGlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { selectedUser = user }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // User Avatar with Online Indicator
              Box(modifier = Modifier.size(48.dp)) {
                if (user.photoURL.isNotEmpty()) {
                  AsyncImage(
                    model = user.photoURL,
                    contentDescription = user.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .size(48.dp)
                      .clip(CircleShape)
                      .border(1.dp, Color.White, CircleShape)
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .size(48.dp)
                      .clip(CircleShape)
                      .background(AdminPinkPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = user.displayName.take(1).uppercase().ifEmpty { "U" },
                      fontWeight = FontWeight.Bold,
                      color = AdminPinkPrimary,
                      fontSize = 18.sp
                    )
                  }
                }

                if (user.isOnline) {
                  Box(
                    modifier = Modifier
                      .size(12.dp)
                      .clip(CircleShape)
                      .background(AdminEmerald)
                      .border(1.5.dp, Color.White, CircleShape)
                      .align(Alignment.BottomEnd)
                  )
                }
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = user.displayName.ifEmpty { "Pinggo User" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AdminTextPrimary
                  )
                  if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, "Verified", tint = AdminPinkPrimary, modifier = Modifier.size(16.dp))
                  }
                  if (user.isBanned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      color = AdminRuby.copy(alpha = 0.15f),
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = "BANNED",
                        color = AdminRuby,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
                Text(
                  text = user.handle,
                  style = MaterialTheme.typography.bodySmall,
                  color = AdminTextSecondary
                )
                if (user.email.isNotEmpty()) {
                  Text(
                    text = user.email,
                    fontSize = 11.sp,
                    color = AdminTextSecondary.copy(alpha = 0.8f)
                  )
                }
              }

              Icon(Icons.Default.ChevronRight, null, tint = AdminTextSecondary)
            }
          }
        }
      }
    }
  }

  // User Details & Moderation Modal
  selectedUser?.let { user ->
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    var showBanPrompt by remember { mutableStateOf(false) }
    var banReason by remember { mutableStateOf("Violation of Pinggo Community Safety Guidelines") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { selectedUser = null }) {
      AdminGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
        Column(modifier = Modifier.padding(22.dp)) {
          // Header
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(AdminPinkPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              if (user.photoURL.isNotEmpty()) {
                AsyncImage(
                  model = user.photoURL,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.size(54.dp).clip(CircleShape)
                )
              } else {
                Text(user.displayName.take(1).uppercase().ifEmpty { "U" }, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AdminPinkPrimary)
              }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(user.displayName.ifEmpty { "Pinggo User" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = AdminTextPrimary)
              Text(user.handle, style = MaterialTheme.typography.bodySmall, color = AdminTextSecondary)
            }
            IconButton(onClick = { selectedUser = null }) {
              Icon(Icons.Default.Close, null, tint = AdminTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = Color(0xFFE2E8F0))
          Spacer(modifier = Modifier.height(14.dp))

          // Metadata Fields
          Text("Account Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Email: ${user.email.ifEmpty { "N/A" }}", fontSize = 12.sp, color = AdminTextSecondary)
          Text("UID: ${user.uid}", fontSize = 11.sp, color = AdminTextSecondary)
          Text("Registered: ${dateFormat.format(Date(user.createdAt))}", fontSize = 12.sp, color = AdminTextSecondary)
          Text("Status: ${if (user.isBanned) "Banned (${user.banReason})" else if (user.isOnline) "Active Online" else "Offline"}", fontSize = 12.sp, color = AdminTextSecondary)

          Spacer(modifier = Modifier.height(18.dp))
          Text("Moderation Actions", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
          Spacer(modifier = Modifier.height(8.dp))

          // Action Buttons
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Verify Toggle
            Button(
              onClick = {
                coroutineScope.launch {
                  val newStatus = !user.isVerified
                  val r = repository.toggleVerifyUser(currentAdminEmail, user, newStatus)
                  if (r.isSuccess) {
                    actionStatusMessage = if (newStatus) "Granted verification badge to @${user.username}" else "Removed badge"
                    selectedUser = user.copy(isVerified = newStatus)
                  }
                }
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = if (user.isVerified) Color(0xFF64748B) else AdminCobalt)
            ) {
              Icon(Icons.Default.Verified, null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(if (user.isVerified) "Remove Verification Badge" else "Grant Pink Verified Badge ✨")
            }

            // Ban Toggle
            if (user.isBanned) {
              Button(
                onClick = {
                  coroutineScope.launch {
                    val r = repository.toggleBanUser(currentAdminEmail, user, false, "")
                    if (r.isSuccess) {
                      actionStatusMessage = "Unbanned @${user.username}"
                      selectedUser = user.copy(isBanned = false)
                    }
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminEmerald)
              ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unban User Account")
              }
            } else {
              Button(
                onClick = { showBanPrompt = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AdminRuby)
              ) {
                Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ban User Account")
              }
            }

            // Delete Account
            OutlinedButton(
              onClick = { showDeleteConfirm = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminRuby)
            ) {
              Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Delete User Account Permanently")
            }
          }
        }
      }
    }

    // Ban Prompt Dialog
    if (showBanPrompt) {
      Dialog(onDismissRequest = { showBanPrompt = false }) {
        AdminGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text("Ban @${user.username}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = banReason,
              onValueChange = { banReason = it },
              label = { Text("Reason for Ban") },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
              TextButton(onClick = { showBanPrompt = false }) { Text("Cancel") }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  coroutineScope.launch {
                    val r = repository.toggleBanUser(currentAdminEmail, user, true, banReason)
                    if (r.isSuccess) {
                      actionStatusMessage = "Banned @${user.username}"
                      selectedUser = user.copy(isBanned = true, banReason = banReason)
                      showBanPrompt = false
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdminRuby)
              ) {
                Text("Confirm Ban")
              }
            }
          }
        }
      }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
      Dialog(onDismissRequest = { showDeleteConfirm = false }) {
        AdminGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text("Delete Account @${user.username}?", fontWeight = FontWeight.Bold, color = AdminRuby, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("This will permanently remove the user record and unregister the username. This action cannot be reversed.", color = AdminTextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
              TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  coroutineScope.launch {
                    val r = repository.deleteUser(currentAdminEmail, user)
                    if (r.isSuccess) {
                      actionStatusMessage = "Permanently deleted user @${user.username}"
                      showDeleteConfirm = false
                      selectedUser = null
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdminRuby)
              ) {
                Text("Delete Account")
              }
            }
          }
        }
      }
    }
  }
}
