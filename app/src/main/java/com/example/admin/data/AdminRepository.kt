package com.example.admin.data

import android.util.Log
import com.example.admin.model.AdminAuditLog
import com.example.admin.model.AdminBroadcast
import com.example.admin.model.AdminDashboardStats
import com.example.model.User
import com.example.model.UserReport
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminRepository {
  private val firestore: FirebaseFirestore?
    get() = try {
      FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
      Log.w("AdminRepository", "FirebaseFirestore not ready: ${e.message}")
      null
    }

  /**
   * Real-time stream of all registered normal users from existing Pinggo /users collection
   */
  fun getAllUsersFlow(): Flow<List<User>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      listener = db.collection("users")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("AdminRepository", "getAllUsersFlow error: ${error.message}")
            return@addSnapshotListener
          }
          val users = snapshot?.documents?.mapNotNull { doc ->
            try {
              val data = doc.data ?: return@mapNotNull null
              User.fromMap(data).copy(uid = doc.id)
            } catch (t: Throwable) {
              null
            }
          } ?: emptyList()
          trySend(users)
        }
    } catch (e: Throwable) {
      Log.w("AdminRepository", "getAllUsersFlow exception: ${e.message}")
    }

    awaitClose { listener?.remove() }
  }

  /**
   * Real-time stream of reports from /reports
   */
  fun getReportsFlow(): Flow<List<UserReport>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      listener = db.collection("reports")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("AdminRepository", "getReportsFlow error: ${error.message}")
            return@addSnapshotListener
          }
          val reports = snapshot?.documents?.mapNotNull { doc ->
            try {
              val data = doc.data ?: return@mapNotNull null
              UserReport.fromMap(doc.id, data)
            } catch (t: Throwable) {
              null
            }
          } ?: emptyList()
          trySend(reports)
        }
    } catch (e: Throwable) {
      Log.w("AdminRepository", "getReportsFlow exception: ${e.message}")
    }

    awaitClose { listener?.remove() }
  }

  /**
   * Real-time stream of broadcasts from /admin_broadcasts
   */
  fun getBroadcastHistoryFlow(): Flow<List<AdminBroadcast>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      listener = db.collection("admin_broadcasts")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("AdminRepository", "getBroadcastHistoryFlow error: ${error.message}")
            return@addSnapshotListener
          }
          val list = snapshot?.documents?.mapNotNull { doc ->
            try {
              val data = doc.data ?: return@mapNotNull null
              AdminBroadcast.fromMap(doc.id, data)
            } catch (t: Throwable) {
              null
            }
          } ?: emptyList()
          trySend(list)
        }
    } catch (e: Throwable) {
      Log.w("AdminRepository", "getBroadcastHistoryFlow exception: ${e.message}")
    }

    awaitClose { listener?.remove() }
  }

  /**
   * Real-time stream of audit logs from /admin_audit_logs
   */
  fun getAuditLogsFlow(): Flow<List<AdminAuditLog>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      listener = db.collection("admin_audit_logs")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("AdminRepository", "getAuditLogsFlow error: ${error.message}")
            return@addSnapshotListener
          }
          val list = snapshot?.documents?.mapNotNull { doc ->
            try {
              val data = doc.data ?: return@mapNotNull null
              AdminAuditLog.fromMap(doc.id, data)
            } catch (t: Throwable) {
              null
            }
          } ?: emptyList()
          trySend(list)
        }
    } catch (e: Throwable) {
      Log.w("AdminRepository", "getAuditLogsFlow exception: ${e.message}")
    }

    awaitClose { listener?.remove() }
  }

  /**
   * Real-time stream of Maintenance Mode settings from /admin_settings/config
   */
  fun getMaintenanceModeFlow(): Flow<Pair<Boolean, String>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(Pair(false, ""))
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      listener = db.collection("admin_settings").document("config")
        .addSnapshotListener { snapshot, error ->
          if (error != null) return@addSnapshotListener
          val data = snapshot?.data
          val enabled = data?.get("maintenanceMode") as? Boolean ?: false
          val msg = data?.get("maintenanceMessage") as? String
            ?: "Pinggo is undergoing scheduled maintenance. Please check back shortly."
          trySend(Pair(enabled, msg))
        }
    } catch (e: Throwable) {
      Log.w("AdminRepository", "getMaintenanceModeFlow error: ${e.message}")
    }

    awaitClose { listener?.remove() }
  }

  /**
   * Record an immutable administrative action into /admin_audit_logs
   */
  suspend fun recordAuditLog(
    actionType: String,
    adminEmail: String,
    description: String,
    targetUserId: String = "",
    details: String = ""
  ): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore not ready"))
      val logId = UUID.randomUUID().toString()
      val log = AdminAuditLog(
        id = logId,
        actionType = actionType,
        adminEmail = adminEmail,
        description = description,
        targetUserId = targetUserId,
        timestamp = System.currentTimeMillis(),
        details = details
      )
      db.collection("admin_audit_logs").document(logId).set(log.toMap()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w("AdminRepository", "Failed to record audit log: ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Ban or unban a user with audit logging
   */
  suspend fun toggleBanUser(
    adminEmail: String,
    targetUser: User,
    isBanned: Boolean,
    reason: String = ""
  ): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore not ready"))
      db.collection("users").document(targetUser.uid).update(
        mapOf(
          "isBanned" to isBanned,
          "banReason" to reason
        )
      ).await()

      // Record audit log
      recordAuditLog(
        actionType = if (isBanned) "USER_BANNED" else "USER_UNBANNED",
        adminEmail = adminEmail,
        description = if (isBanned) "Banned user @${targetUser.username}. Reason: $reason"
                      else "Unbanned user @${targetUser.username}",
        targetUserId = targetUser.uid,
        details = "Username: @${targetUser.username}, Email: ${targetUser.email}"
      )

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Toggle verified checkmark badge with audit logging
   */
  suspend fun toggleVerifyUser(
    adminEmail: String,
    targetUser: User,
    isVerified: Boolean
  ): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore not ready"))
      db.collection("users").document(targetUser.uid).update("isVerified", isVerified).await()

      recordAuditLog(
        actionType = "USER_VERIFIED",
        adminEmail = adminEmail,
        description = if (isVerified) "Granted Verified Badge to @${targetUser.username}"
                      else "Revoked Verified Badge from @${targetUser.username}",
        targetUserId = targetUser.uid,
        details = "Username: @${targetUser.username}"
      )

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Delete user account with audit logging
   */
  suspend fun deleteUser(
    adminEmail: String,
    targetUser: User
  ): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore not ready"))
      db.collection("users").document(targetUser.uid).delete().await()
      if (targetUser.username.isNotEmpty()) {
        db.collection("usernames").document(targetUser.username.lowercase()).delete().await()
      }

      recordAuditLog(
        actionType = "USER_DELETED",
        adminEmail = adminEmail,
        description = "Permanently deleted user account @${targetUser.username} (${targetUser.uid})",
        targetUserId = targetUser.uid,
        details = "Deleted profile and released username"
      )

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Send a broadcast announcement and deliver notifications to user inboxes
   */
  suspend fun createAndSendBroadcast(
    adminEmail: String,
    title: String,
    message: String,
    targetAudience: String = "all", // "all", "verified", "test_preview"
    priority: String = "general"
  ): Result<AdminBroadcast> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore not ready"))
      val broadcastId = UUID.randomUUID().toString()

      // Fetch recipient users based on audience
      val usersSnapshot = db.collection("users").get().await()
      val eligibleUsers = usersSnapshot.documents.mapNotNull { doc ->
        try {
          val data = doc.data ?: return@mapNotNull null
          User.fromMap(data).copy(uid = doc.id)
        } catch (t: Throwable) { null }
      }.filter { user ->
        when (targetAudience) {
          "verified" -> user.isVerified
          "test_preview" -> user.email.equals(adminEmail, ignoreCase = true)
          else -> !user.isBanned // "all" eligible users
        }
      }

      val broadcast = AdminBroadcast(
        id = broadcastId,
        title = title,
        message = message,
        targetAudience = targetAudience,
        priority = priority,
        senderAdminEmail = adminEmail,
        createdAt = System.currentTimeMillis(),
        recipientCount = eligibleUsers.size,
        status = "sent"
      )

      // 1. Write to /admin_broadcasts
      db.collection("admin_broadcasts").document(broadcastId).set(broadcast.toMap()).await()

      // 2. Deliver into users' /notifications inbox in batches
      var batch = db.batch()
      var count = 0
      for (user in eligibleUsers) {
        val notifId = UUID.randomUUID().toString()
        val notifRef = db.collection("notifications").document(notifId)
        val notifData = mapOf(
          "id" to notifId,
          "recipientId" to user.uid,
          "senderId" to "admin_system",
          "senderName" to "Pinggo Announcement",
          "senderAvatar" to "",
          "type" to "admin_broadcast",
          "title" to title,
          "content" to message,
          "timestamp" to System.currentTimeMillis(),
          "isRead" to false
        )
        batch.set(notifRef, notifData)
        count++

        if (count % 400 == 0) {
          batch.commit().await()
          batch = db.batch()
        }
      }
      if (count % 400 != 0) {
        batch.commit().await()
      }

      // 3. Record immutable audit log
      recordAuditLog(
        actionType = "BROADCAST_CREATED",
        adminEmail = adminEmail,
        description = "Dispatched broadcast '$title' to $count users (Audience: $targetAudience, Priority: $priority)",
        details = "Broadcast ID: $broadcastId, Recipient Count: $count"
      )

      Result.success(broadcast)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Update platform maintenance mode
   */
  suspend fun updateMaintenanceMode(
    adminEmail: String,
    enabled: Boolean,
    message: String
  ): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore not ready"))
      val data = mapOf(
        "maintenanceMode" to enabled,
        "maintenanceMessage" to message,
        "updatedAt" to System.currentTimeMillis(),
        "updatedBy" to adminEmail
      )
      db.collection("admin_settings").document("config").set(data).await()

      recordAuditLog(
        actionType = "MAINTENANCE_TOGGLED",
        adminEmail = adminEmail,
        description = if (enabled) "Enabled Platform Maintenance Mode" else "Disabled Maintenance Mode",
        details = "Banner Message: $message"
      )

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Resolve or dismiss user report
   */
  suspend fun resolveReport(
    adminEmail: String,
    reportId: String,
    status: String
  ): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore not ready"))
      db.collection("reports").document(reportId).update(
        mapOf(
          "status" to status,
          "resolvedAt" to System.currentTimeMillis(),
          "resolvedBy" to adminEmail
        )
      ).await()

      recordAuditLog(
        actionType = "REPORT_RESOLVED",
        adminEmail = adminEmail,
        description = "Marked report $reportId as '$status'",
        details = "Report ID: $reportId"
      )

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
