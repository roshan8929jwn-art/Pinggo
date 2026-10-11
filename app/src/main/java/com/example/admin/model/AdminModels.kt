package com.example.admin.model

import com.example.model.User

data class AdminDashboardStats(
  val totalUsers: Int = 0,
  val onlineUsers: Int = 0,
  val totalConversations: Int = 0,
  val totalGroups: Int = 0,
  val pendingReports: Int = 0,
  val totalBroadcasts: Int = 0,
  val bannedUsers: Int = 0,
  val verifiedUsers: Int = 0
)

data class AdminBroadcast(
  val id: String = "",
  val title: String = "",
  val message: String = "",
  val targetAudience: String = "all", // "all", "verified", "test_preview"
  val priority: String = "general", // "general", "maintenance", "update", "critical"
  val senderAdminEmail: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val recipientCount: Int = 0,
  val status: String = "sent" // "sent", "failed", "pending"
) {
  fun toMap(): Map<String, Any?> {
    return mapOf(
      "id" to id,
      "title" to title,
      "message" to message,
      "targetAudience" to targetAudience,
      "priority" to priority,
      "senderAdminEmail" to senderAdminEmail,
      "createdAt" to createdAt,
      "recipientCount" to recipientCount,
      "status" to status
    )
  }

  companion object {
    fun fromMap(id: String, map: Map<String, Any?>): AdminBroadcast {
      return AdminBroadcast(
        id = id,
        title = map["title"] as? String ?: "",
        message = map["message"] as? String ?: "",
        targetAudience = map["targetAudience"] as? String ?: "all",
        priority = map["priority"] as? String ?: "general",
        senderAdminEmail = map["senderAdminEmail"] as? String ?: "",
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        recipientCount = (map["recipientCount"] as? Number)?.toInt() ?: 0,
        status = map["status"] as? String ?: "sent"
      )
    }
  }
}

data class AdminAuditLog(
  val id: String = "",
  val actionType: String = "", // "ADMIN_LOGIN", "BROADCAST_CREATED", "USER_BANNED", "USER_UNBANNED", "USER_VERIFIED", "USER_DELETED", "MAINTENANCE_TOGGLED"
  val adminEmail: String = "",
  val description: String = "",
  val targetUserId: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val details: String = ""
) {
  fun toMap(): Map<String, Any?> {
    return mapOf(
      "id" to id,
      "actionType" to actionType,
      "adminEmail" to adminEmail,
      "description" to description,
      "targetUserId" to targetUserId,
      "timestamp" to timestamp,
      "details" to details
    )
  }

  companion object {
    fun fromMap(id: String, map: Map<String, Any?>): AdminAuditLog {
      return AdminAuditLog(
        id = id,
        actionType = map["actionType"] as? String ?: "",
        adminEmail = map["adminEmail"] as? String ?: "",
        description = map["description"] as? String ?: "",
        targetUserId = map["targetUserId"] as? String ?: "",
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        details = map["details"] as? String ?: ""
      )
    }
  }
}
