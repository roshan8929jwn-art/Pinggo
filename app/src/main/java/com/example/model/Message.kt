package com.example.model

data class Message(
  val id: String = "",
  val conversationId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val senderPhoto: String = "",
  val text: String = "",
  val type: String = "text", // "text", "image", "voice", "file"
  val mediaUrl: String = "",
  val voiceDurationSec: Int = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val delivered: Boolean = true,
  val read: Boolean = false,
  val readBy: List<String> = emptyList(),
  val replyToMessageId: String = "",
  val replyToText: String = "",
  val replyToSender: String = "",
  val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of uids
  val isEdited: Boolean = false,
  val isDeleted: Boolean = false
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "conversationId" to conversationId,
    "senderId" to senderId,
    "senderName" to senderName,
    "senderPhoto" to senderPhoto,
    "text" to text,
    "type" to type,
    "mediaUrl" to mediaUrl,
    "voiceDurationSec" to voiceDurationSec,
    "timestamp" to timestamp,
    "delivered" to delivered,
    "read" to read,
    "readBy" to readBy,
    "replyToMessageId" to replyToMessageId,
    "replyToText" to replyToText,
    "replyToSender" to replyToSender,
    "reactions" to reactions,
    "isEdited" to isEdited,
    "isDeleted" to isDeleted
  )

  companion object {
    @Suppress("UNCHECKED_CAST")
    fun fromMap(map: Map<String, Any?>): Message {
      return Message(
        id = map["id"] as? String ?: "",
        conversationId = map["conversationId"] as? String ?: "",
        senderId = map["senderId"] as? String ?: "",
        senderName = map["senderName"] as? String ?: "",
        senderPhoto = map["senderPhoto"] as? String ?: "",
        text = map["text"] as? String ?: "",
        type = map["type"] as? String ?: "text",
        mediaUrl = map["mediaUrl"] as? String ?: "",
        voiceDurationSec = (map["voiceDurationSec"] as? Number)?.toInt() ?: 0,
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        delivered = map["delivered"] as? Boolean ?: true,
        read = map["read"] as? Boolean ?: false,
        readBy = (map["readBy"] as? List<String>) ?: emptyList(),
        replyToMessageId = map["replyToMessageId"] as? String ?: "",
        replyToText = map["replyToText"] as? String ?: "",
        replyToSender = map["replyToSender"] as? String ?: "",
        reactions = (map["reactions"] as? Map<String, List<String>>) ?: emptyMap(),
        isEdited = map["isEdited"] as? Boolean ?: false,
        isDeleted = map["isDeleted"] as? Boolean ?: false
      )
    }
  }
}
