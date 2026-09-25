package com.example.model

data class User(
  val uid: String = "",
  val displayName: String = "",
  val username: String = "",
  val email: String = "",
  val photoURL: String = "",
  val bio: String = "Hey there! I am using Pinggo 🐧",
  val createdAt: Long = System.currentTimeMillis(),
  val lastSeen: Long = System.currentTimeMillis(),
  val isOnline: Boolean = false,
  val fcmToken: String = "",
  val privacyLastSeen: String = "everyone", // "everyone", "nobody"
  val privacyReadReceipts: Boolean = true,
  val privacyOnlineStatus: Boolean = true
) {
  fun toMap(): Map<String, Any?> {
    return mapOf(
      "uid" to uid,
      "displayName" to displayName,
      "username" to username.lowercase(),
      "email" to email,
      "photoURL" to photoURL,
      "bio" to bio,
      "createdAt" to createdAt,
      "lastSeen" to lastSeen,
      "isOnline" to isOnline,
      "fcmToken" to fcmToken,
      "privacyLastSeen" to privacyLastSeen,
      "privacyReadReceipts" to privacyReadReceipts,
      "privacyOnlineStatus" to privacyOnlineStatus
    )
  }

  companion object {
    fun fromMap(map: Map<String, Any?>): User {
      return User(
        uid = map["uid"] as? String ?: "",
        displayName = map["displayName"] as? String ?: "",
        username = (map["username"] as? String ?: "").lowercase(),
        email = map["email"] as? String ?: "",
        photoURL = map["photoURL"] as? String ?: "",
        bio = map["bio"] as? String ?: "Hey there! I am using Pinggo 🐧",
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        lastSeen = (map["lastSeen"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        isOnline = map["isOnline"] as? Boolean ?: false,
        fcmToken = map["fcmToken"] as? String ?: "",
        privacyLastSeen = map["privacyLastSeen"] as? String ?: "everyone",
        privacyReadReceipts = map["privacyReadReceipts"] as? Boolean ?: true,
        privacyOnlineStatus = map["privacyOnlineStatus"] as? Boolean ?: true
      )
    }
  }
}

val User.handle: String
  get() = if (username.startsWith("@")) username else if (username.isNotEmpty()) "@$username" else "@username"

