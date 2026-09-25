package com.example.model

data class StatusUpdate(
  val id: String = "",
  val userId: String = "",
  val userName: String = "",
  val userPhoto: String = "",
  val text: String = "",
  val imageUrl: String = "",
  val timestamp: Long = System.currentTimeMillis()
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "userName" to userName,
    "userPhoto" to userPhoto,
    "text" to text,
    "imageUrl" to imageUrl,
    "timestamp" to timestamp
  )

  companion object {
    fun fromMap(map: Map<String, Any?>): StatusUpdate {
      return StatusUpdate(
        id = map["id"] as? String ?: "",
        userId = map["userId"] as? String ?: "",
        userName = map["userName"] as? String ?: "",
        userPhoto = map["userPhoto"] as? String ?: "",
        text = map["text"] as? String ?: "",
        imageUrl = map["imageUrl"] as? String ?: "",
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
      )
    }
  }
}
