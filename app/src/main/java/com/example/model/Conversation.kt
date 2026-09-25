package com.example.model

data class ParticipantInfo(
  val uid: String = "",
  val displayName: String = "",
  val username: String = "",
  val photoURL: String = ""
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "uid" to uid,
    "displayName" to displayName,
    "username" to username,
    "photoURL" to photoURL
  )

  companion object {
    fun fromMap(map: Map<String, Any?>?): ParticipantInfo {
      if (map == null) return ParticipantInfo()
      return ParticipantInfo(
        uid = map["uid"] as? String ?: "",
        displayName = map["displayName"] as? String ?: "",
        username = map["username"] as? String ?: "",
        photoURL = map["photoURL"] as? String ?: ""
      )
    }
  }
}

data class Conversation(
  val id: String = "",
  val type: String = "direct", // "direct", "group"
  val participants: List<String> = emptyList(),
  val participantDetails: Map<String, ParticipantInfo> = emptyMap(),
  val lastMessage: String = "",
  val lastMessageTimestamp: Long = System.currentTimeMillis(),
  val lastMessageSenderId: String = "",
  val unreadCounts: Map<String, Int> = emptyMap(),
  val groupName: String = "",
  val groupPhoto: String = "",
  val groupDescription: String = "",
  val adminUids: List<String> = emptyList(),
  val pinnedBy: List<String> = emptyList(),
  val mutedBy: List<String> = emptyList(),
  val archivedBy: List<String> = emptyList(),
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
) {
  fun getOtherParticipant(currentUserId: String): ParticipantInfo? {
    val otherId = participants.firstOrNull { it != currentUserId } ?: return null
    return participantDetails[otherId]
  }

  fun getTitle(currentUserId: String): String {
    return if (type == "group") {
      groupName.ifEmpty { "Pinggo Group" }
    } else {
      getOtherParticipant(currentUserId)?.displayName?.ifEmpty {
        getOtherParticipant(currentUserId)?.username ?: "Pinggo User"
      } ?: "Conversation"
    }
  }

  fun getAvatarUrl(currentUserId: String): String {
    return if (type == "group") {
      groupPhoto
    } else {
      getOtherParticipant(currentUserId)?.photoURL ?: ""
    }
  }
}
