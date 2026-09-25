package com.example.model

data class CallSession(
  val id: String = "",
  val callerId: String = "",
  val receiverId: String = "",
  val callerName: String = "",
  val callerPhoto: String = "",
  val receiverName: String = "",
  val receiverPhoto: String = "",
  val type: String = "voice", // "voice", "video"
  val status: String = "ringing", // "ringing", "accepted", "rejected", "ended", "missed"
  val createdAt: Long = System.currentTimeMillis(),
  val durationSec: Int = 0
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "callerId" to callerId,
    "receiverId" to receiverId,
    "callerName" to callerName,
    "callerPhoto" to callerPhoto,
    "receiverName" to receiverName,
    "receiverPhoto" to receiverPhoto,
    "type" to type,
    "status" to status,
    "createdAt" to createdAt,
    "durationSec" to durationSec
  )

  companion object {
    fun fromMap(map: Map<String, Any?>): CallSession {
      return CallSession(
        id = map["id"] as? String ?: "",
        callerId = map["callerId"] as? String ?: "",
        receiverId = map["receiverId"] as? String ?: "",
        callerName = map["callerName"] as? String ?: "",
        callerPhoto = map["callerPhoto"] as? String ?: "",
        receiverName = map["receiverName"] as? String ?: "",
        receiverPhoto = map["receiverPhoto"] as? String ?: "",
        type = map["type"] as? String ?: "voice",
        status = map["status"] as? String ?: "ringing",
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        durationSec = (map["durationSec"] as? Number)?.toInt() ?: 0
      )
    }
  }
}
