package com.example.model

data class AdminStats(
  val totalUsers: Int = 0,
  val onlineUsers: Int = 0,
  val totalConversations: Int = 0,
  val totalGroups: Int = 0,
  val bannedUsers: Int = 0,
  val verifiedUsers: Int = 0,
  val totalReports: Int = 0
)

data class SystemBroadcast(
  val id: String = "",
  val title: String = "",
  val message: String = "",
  val type: String = "info", // "info", "update", "warning", "critical"
  val senderName: String = "Pinggo Team",
  val createdAt: Long = System.currentTimeMillis()
) {
  fun toMap(): Map<String, Any?> {
    return mapOf(
      "title" to title,
      "message" to message,
      "type" to type,
      "senderName" to senderName,
      "createdAt" to createdAt
    )
  }

  companion object {
    fun fromMap(id: String, map: Map<String, Any?>): SystemBroadcast {
      return SystemBroadcast(
        id = id,
        title = map["title"] as? String ?: "",
        message = map["message"] as? String ?: "",
        type = map["type"] as? String ?: "info",
        senderName = map["senderName"] as? String ?: "Pinggo Team",
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
      )
    }
  }
}

data class UserReport(
  val id: String = "",
  val reporterId: String = "",
  val reporterName: String = "User",
  val reportedUserId: String = "",
  val reportedUserName: String = "User",
  val reason: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val status: String = "pending" // "pending", "resolved", "dismissed"
) {
  companion object {
    fun fromMap(id: String, map: Map<String, Any?>): UserReport {
      return UserReport(
        id = id,
        reporterId = map["reporterId"] as? String ?: "",
        reporterName = map["reporterName"] as? String ?: "User",
        reportedUserId = map["reportedUserId"] as? String ?: "",
        reportedUserName = map["reportedUserName"] as? String ?: "User",
        reason = map["reason"] as? String ?: "",
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        status = map["status"] as? String ?: "pending"
      )
    }
  }
}

data class AdminSettings(
  val maintenanceMode: Boolean = false,
  val maintenanceMessage: String = "Pinggo is undergoing routine server maintenance. Please check back shortly!",
  val allowNewRegistrations: Boolean = true
)
