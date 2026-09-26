package com.example.model

data class User(
  val uid: String = "",
  val displayName: String = "",
  val username: String = "",
  val usernameLowercase: String = "",
  val email: String = "",
  val photoURL: String = "",
  val bio: String = "Hey there! I am using Pinggo 🐧",
  val createdAt: Long = System.currentTimeMillis(),
  val lastSeen: Long = System.currentTimeMillis(),
  val isOnline: Boolean = false,
  val fcmToken: String = "",
  val privacyLastSeen: String = "everyone", // "everyone", "friends", "nobody"
  val privacyReadReceipts: Boolean = true,
  val privacyOnlineStatus: Boolean = true,
  val privacyStatus: String = "everyone", // "everyone", "friends"
  val blockedUsers: List<String> = emptyList(),
  val notificationMessage: Boolean = true,
  val notificationGroup: Boolean = true,
  val notificationPreview: Boolean = true,
  val notificationCall: Boolean = true,
  val welcomeEmailTriggered: Boolean = false,
  val callRingtoneUri: String = "",
  val callRingtoneTitle: String = "Default Ringtone",
  val notificationRingtoneUri: String = "",
  val notificationRingtoneTitle: String = "Default Notification",
  val otpVerified: Boolean = false,
  val friends: List<String> = emptyList()
) {
  fun toMap(): Map<String, Any?> {
    return mapOf(
      "uid" to uid,
      "displayName" to displayName,
      "username" to username,
      "usernameLowercase" to usernameLowercase.lowercase(),
      "email" to email,
      "photoURL" to photoURL,
      "bio" to bio,
      "createdAt" to createdAt,
      "lastSeen" to lastSeen,
      "isOnline" to isOnline,
      "fcmToken" to fcmToken,
      "privacyLastSeen" to privacyLastSeen,
      "privacyReadReceipts" to privacyReadReceipts,
      "privacyOnlineStatus" to privacyOnlineStatus,
      "privacyStatus" to privacyStatus,
      "blockedUsers" to blockedUsers,
      "notificationMessage" to notificationMessage,
      "notificationGroup" to notificationGroup,
      "notificationPreview" to notificationPreview,
      "notificationCall" to notificationCall,
      "welcomeEmailTriggered" to welcomeEmailTriggered,
      "callRingtoneUri" to callRingtoneUri,
      "callRingtoneTitle" to callRingtoneTitle,
      "notificationRingtoneUri" to notificationRingtoneUri,
      "notificationRingtoneTitle" to notificationRingtoneTitle,
      "otpVerified" to otpVerified,
      "friends" to friends
    )
  }

  companion object {
    @Suppress("UNCHECKED_CAST")
    fun fromMap(map: Map<String, Any?>): User {
      return User(
        uid = map["uid"] as? String ?: "",
        displayName = map["displayName"] as? String ?: "",
        username = map["username"] as? String ?: "",
        usernameLowercase = (map["usernameLowercase"] as? String ?: "").lowercase(),
        email = map["email"] as? String ?: "",
        photoURL = map["photoURL"] as? String ?: "",
        bio = map["bio"] as? String ?: "Hey there! I am using Pinggo 🐧",
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        lastSeen = (map["lastSeen"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        isOnline = map["isOnline"] as? Boolean ?: false,
        fcmToken = map["fcmToken"] as? String ?: "",
        privacyLastSeen = map["privacyLastSeen"] as? String ?: "everyone",
        privacyReadReceipts = map["privacyReadReceipts"] as? Boolean ?: true,
        privacyOnlineStatus = map["privacyOnlineStatus"] as? Boolean ?: true,
        privacyStatus = map["privacyStatus"] as? String ?: "everyone",
        blockedUsers = (map["blockedUsers"] as? List<String>) ?: emptyList(),
        notificationMessage = map["notificationMessage"] as? Boolean ?: true,
        notificationGroup = map["notificationGroup"] as? Boolean ?: true,
        notificationPreview = map["notificationPreview"] as? Boolean ?: true,
        notificationCall = map["notificationCall"] as? Boolean ?: true,
        welcomeEmailTriggered = map["welcomeEmailTriggered"] as? Boolean ?: false,
        callRingtoneUri = map["callRingtoneUri"] as? String ?: "",
        callRingtoneTitle = map["callRingtoneTitle"] as? String ?: "Default Ringtone",
        notificationRingtoneUri = map["notificationRingtoneUri"] as? String ?: "",
        notificationRingtoneTitle = map["notificationRingtoneTitle"] as? String ?: "Default Notification",
        otpVerified = map["otpVerified"] as? Boolean ?: false,
        friends = (map["friends"] as? List<String>) ?: emptyList()
      )
    }
  }
}

val User.handle: String
  get() = if (username.startsWith("@")) username else "@$username"
