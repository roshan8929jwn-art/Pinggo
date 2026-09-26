package com.example.model

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderUsername: String = "",
    val senderPhoto: String = "",
    val receiverId: String = "",
    val status: String = "pending", // "pending", "accepted", "declined"
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderUsername" to senderUsername,
            "senderPhoto" to senderPhoto,
            "receiverId" to receiverId,
            "status" to status,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): FriendRequest {
            return FriendRequest(
                id = id,
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                senderUsername = map["senderUsername"] as? String ?: "",
                senderPhoto = map["senderPhoto"] as? String ?: "",
                receiverId = map["receiverId"] as? String ?: "",
                status = map["status"] as? String ?: "pending",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
