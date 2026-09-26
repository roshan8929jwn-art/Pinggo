package com.example.model

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderUsername: String = "",
    val senderPhoto: String = "",
    val type: String = "", // "friend_request", "request_accepted", "status_like", "login_alert"
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedId: String = "" // e.g. friend request ID or status ID
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "recipientId" to recipientId,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderUsername" to senderUsername,
            "senderPhoto" to senderPhoto,
            "type" to type,
            "content" to content,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "relatedId" to relatedId
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Notification {
            return Notification(
                id = id,
                recipientId = map["recipientId"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                senderUsername = map["senderUsername"] as? String ?: "",
                senderPhoto = map["senderPhoto"] as? String ?: "",
                type = map["type"] as? String ?: "",
                content = map["content"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                isRead = map["isRead"] as? Boolean ?: false,
                relatedId = map["relatedId"] as? String ?: ""
            )
        }
    }
}
