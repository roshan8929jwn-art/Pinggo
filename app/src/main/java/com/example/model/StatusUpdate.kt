package com.example.model

data class StatusViewer(
  val uid: String = "",
  val displayName: String = "",
  val username: String = "",
  val photoURL: String = "",
  val viewedAt: Long = System.currentTimeMillis()
)

data class StatusUpdate(
  val id: String = "",
  val userId: String = "",
  val userName: String = "",
  val userPhoto: String = "",
  val text: String = "",
  val imageUrl: String = "",
  val mediaType: String = "image", // "image", "video", "text"
  val timestamp: Long = System.currentTimeMillis(),
  val viewers: Map<String, Long> = emptyMap() // viewerUid -> timestamp
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "userName" to userName,
    "userPhoto" to userPhoto,
    "text" to text,
    "imageUrl" to imageUrl,
    "mediaType" to mediaType,
    "timestamp" to timestamp,
    "viewers" to viewers
  )

  companion object {
    @Suppress("UNCHECKED_CAST")
    fun fromMap(map: Map<String, Any?>): StatusUpdate {
      return StatusUpdate(
        id = map["id"] as? String ?: "",
        userId = map["userId"] as? String ?: "",
        userName = map["userName"] as? String ?: "",
        userPhoto = map["userPhoto"] as? String ?: "",
        text = map["text"] as? String ?: "",
        imageUrl = (map["imageUrl"] as? String) ?: (map["mediaUrl"] as? String) ?: "",
        mediaType = map["mediaType"] as? String ?: "image",
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        viewers = (map["viewers"] as? Map<String, Number>)?.mapValues { it.value.toLong() } ?: emptyMap()
      )
    }
  }
}
