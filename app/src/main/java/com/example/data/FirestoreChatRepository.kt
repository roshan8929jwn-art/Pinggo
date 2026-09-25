package com.example.data

import com.example.model.CallSession
import com.example.model.Conversation
import com.example.model.Message
import com.example.model.ParticipantInfo
import com.example.model.StatusUpdate
import com.example.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreChatRepository {
  private val firestore = FirebaseFirestore.getInstance()

  /**
   * Real-time listener for current user's conversations
   */
  fun getConversationsFlow(userId: String): Flow<List<Conversation>> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    val query = firestore.collection("conversations")
      .whereArrayContains("participants", userId)

    val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        error.printStackTrace()
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val list = snapshot.documents.mapNotNull { doc ->
          try {
            val data = doc.data ?: return@mapNotNull null
            val id = doc.id
            val type = data["type"] as? String ?: "direct"
            @Suppress("UNCHECKED_CAST")
            val participants = (data["participants"] as? List<String>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val detailsRaw = data["participantDetails"] as? Map<String, Map<String, Any?>> ?: emptyMap()
            val details = detailsRaw.mapValues { ParticipantInfo.fromMap(it.value) }

            val lastMessage = data["lastMessage"] as? String ?: ""
            val lastMessageTimestamp = (data["lastMessageTimestamp"] as? Number)?.toLong() ?: 0L
            val lastMessageSenderId = data["lastMessageSenderId"] as? String ?: ""
            @Suppress("UNCHECKED_CAST")
            val unreadCounts = (data["unreadCounts"] as? Map<String, Number>)
              ?.mapValues { it.value.toInt() } ?: emptyMap()

            val groupName = data["groupName"] as? String ?: ""
            val groupPhoto = data["groupPhoto"] as? String ?: ""
            val groupDescription = data["groupDescription"] as? String ?: ""
            @Suppress("UNCHECKED_CAST")
            val adminUids = (data["adminUids"] as? List<String>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val pinnedBy = (data["pinnedBy"] as? List<String>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val mutedBy = (data["mutedBy"] as? List<String>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val archivedBy = (data["archivedBy"] as? List<String>) ?: emptyList()
            val createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
            val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L

            Conversation(
              id = id,
              type = type,
              participants = participants,
              participantDetails = details,
              lastMessage = lastMessage,
              lastMessageTimestamp = lastMessageTimestamp,
              lastMessageSenderId = lastMessageSenderId,
              unreadCounts = unreadCounts,
              groupName = groupName,
              groupPhoto = groupPhoto,
              groupDescription = groupDescription,
              adminUids = adminUids,
              pinnedBy = pinnedBy,
              mutedBy = mutedBy,
              archivedBy = archivedBy,
              createdAt = createdAt,
              updatedAt = updatedAt
            )
          } catch (e: Exception) {
            e.printStackTrace()
            null
          }
        }.sortedByDescending { it.lastMessageTimestamp }
        trySend(list)
      }
    }

    awaitClose { listener.remove() }
  }

  /**
   * Real-time listener for messages in a conversation
   */
  fun getMessagesFlow(conversationId: String): Flow<List<Message>> = callbackFlow {
    if (conversationId.isEmpty()) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    val query = firestore.collection("conversations")
      .document(conversationId)
      .collection("messages")
      .orderBy("timestamp", Query.Direction.ASCENDING)

    val listener = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        error.printStackTrace()
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val messages = snapshot.documents.mapNotNull { doc ->
          val data = doc.data ?: return@mapNotNull null
          Message.fromMap(data)
        }
        trySend(messages)
      }
    }

    awaitClose { listener.remove() }
  }

  /**
   * Real-time listener for typing indicator in conversation
   */
  fun getTypingFlow(conversationId: String, currentUserId: String): Flow<List<String>> = callbackFlow {
    if (conversationId.isEmpty()) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    val listener = firestore.collection("conversations")
      .document(conversationId)
      .collection("typing")
      .addSnapshotListener { snapshot, _ ->
        if (snapshot != null) {
          val now = System.currentTimeMillis()
          val typers = snapshot.documents.mapNotNull { doc ->
            val timestamp = doc.getLong("timestamp") ?: 0L
            val userId = doc.id
            val name = doc.getString("name") ?: "Someone"
            if (userId != currentUserId && (now - timestamp) < 5000) {
              name
            } else null
          }
          trySend(typers)
        }
      }

    awaitClose { listener.remove() }
  }

  suspend fun setTyping(conversationId: String, userId: String, userName: String, isTyping: Boolean) {
    if (conversationId.isEmpty() || userId.isEmpty()) return
    try {
      val docRef = firestore.collection("conversations")
        .document(conversationId)
        .collection("typing")
        .document(userId)

      if (isTyping) {
        docRef.set(mapOf("name" to userName, "timestamp" to System.currentTimeMillis())).await()
      } else {
        docRef.delete().await()
      }
    } catch (e: Exception) {
      // ignore
    }
  }

  /**
   * Send a real-time message
   */
  suspend fun sendMessage(
    conversationId: String,
    sender: User,
    text: String,
    type: String = "text",
    mediaUrl: String = "",
    voiceDurationSec: Int = 0,
    replyTo: Message? = null
  ): Result<Message> {
    return try {
      val msgId = UUID.randomUUID().toString()
      val now = System.currentTimeMillis()

      val message = Message(
        id = msgId,
        conversationId = conversationId,
        senderId = sender.uid,
        senderName = sender.displayName.ifEmpty { sender.username },
        senderPhoto = sender.photoURL,
        text = text,
        type = type,
        mediaUrl = mediaUrl,
        voiceDurationSec = voiceDurationSec,
        timestamp = now,
        delivered = true,
        read = false,
        replyToMessageId = replyTo?.id ?: "",
        replyToText = replyTo?.text ?: "",
        replyToSender = replyTo?.senderName ?: ""
      )

      // 1. Add message to subcollection
      firestore.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(msgId)
        .set(message.toMap())
        .await()

      // 2. Update conversation snippet
      val snippet = when (type) {
        "voice" -> "🎤 Voice message"
        "image" -> "📷 Photo"
        "file" -> "📎 Document"
        else -> text
      }

      firestore.collection("conversations").document(conversationId).update(
        mapOf(
          "lastMessage" to snippet,
          "lastMessageTimestamp" to now,
          "lastMessageSenderId" to sender.uid,
          "updatedAt" to now
        )
      ).await()

      Result.success(message)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  /**
   * Get or create a direct 1-on-1 conversation
   */
  suspend fun getOrCreateDirectConversation(currentUser: User, targetUser: User): Result<Conversation> {
    return try {
      // Look for existing conversation with these two participants
      val existing = firestore.collection("conversations")
        .whereEqualTo("type", "direct")
        .whereArrayContains("participants", currentUser.uid)
        .get()
        .await()

      val match = existing.documents.firstOrNull { doc ->
        @Suppress("UNCHECKED_CAST")
        val participants = doc.get("participants") as? List<String> ?: emptyList()
        participants.contains(targetUser.uid)
      }

      if (match != null) {
        val detailsRaw = match.get("participantDetails") as? Map<String, Map<String, Any?>> ?: emptyMap()
        val details = detailsRaw.mapValues { ParticipantInfo.fromMap(it.value) }
        val conv = Conversation(
          id = match.id,
          type = "direct",
          participants = listOf(currentUser.uid, targetUser.uid),
          participantDetails = details,
          lastMessage = match.getString("lastMessage") ?: "",
          lastMessageTimestamp = match.getLong("lastMessageTimestamp") ?: System.currentTimeMillis()
        )
        return Result.success(conv)
      }

      // Create new conversation
      val convId = UUID.randomUUID().toString()
      val now = System.currentTimeMillis()
      val details = mapOf(
        currentUser.uid to ParticipantInfo(currentUser.uid, currentUser.displayName, currentUser.username, currentUser.photoURL),
        targetUser.uid to ParticipantInfo(targetUser.uid, targetUser.displayName, targetUser.username, targetUser.photoURL)
      )

      val data = mapOf(
        "id" to convId,
        "type" to "direct",
        "participants" to listOf(currentUser.uid, targetUser.uid),
        "participantDetails" to details.mapValues { it.value.toMap() },
        "lastMessage" to "Say hello! 👋",
        "lastMessageTimestamp" to now,
        "lastMessageSenderId" to "",
        "unreadCounts" to mapOf(currentUser.uid to 0, targetUser.uid to 0),
        "createdAt" to now,
        "updatedAt" to now
      )

      firestore.collection("conversations").document(convId).set(data).await()
      val newConv = Conversation(
        id = convId,
        type = "direct",
        participants = listOf(currentUser.uid, targetUser.uid),
        participantDetails = details,
        lastMessage = "Say hello! 👋",
        lastMessageTimestamp = now
      )
      Result.success(newConv)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  /**
   * Create a group conversation
   */
  suspend fun createGroup(
    currentUser: User,
    groupName: String,
    groupDescription: String,
    groupPhoto: String,
    memberUsers: List<User>
  ): Result<Conversation> {
    return try {
      val convId = UUID.randomUUID().toString()
      val now = System.currentTimeMillis()

      val allMembers = (listOf(currentUser) + memberUsers).distinctBy { it.uid }
      val participantUids = allMembers.map { it.uid }
      val participantDetails = allMembers.associate {
        it.uid to ParticipantInfo(it.uid, it.displayName, it.username, it.photoURL)
      }

      val data = mapOf(
        "id" to convId,
        "type" to "group",
        "participants" to participantUids,
        "participantDetails" to participantDetails.mapValues { it.value.toMap() },
        "lastMessage" to "${currentUser.displayName} created group \"$groupName\"",
        "lastMessageTimestamp" to now,
        "lastMessageSenderId" to currentUser.uid,
        "groupName" to groupName,
        "groupDescription" to groupDescription,
        "groupPhoto" to groupPhoto,
        "adminUids" to listOf(currentUser.uid),
        "unreadCounts" to participantUids.associateWith { 0 },
        "createdAt" to now,
        "updatedAt" to now
      )

      firestore.collection("conversations").document(convId).set(data).await()
      val conv = Conversation(
        id = convId,
        type = "group",
        participants = participantUids,
        participantDetails = participantDetails,
        groupName = groupName,
        groupPhoto = groupPhoto,
        groupDescription = groupDescription,
        adminUids = listOf(currentUser.uid),
        lastMessage = "Group created",
        lastMessageTimestamp = now
      )
      Result.success(conv)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Add emoji reaction
   */
  suspend fun addReaction(conversationId: String, messageId: String, userId: String, emoji: String) {
    try {
      val docRef = firestore.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)

      firestore.runTransaction { transaction ->
        val snapshot = transaction.get(docRef)
        @Suppress("UNCHECKED_CAST")
        val reactions = (snapshot.get("reactions") as? Map<String, List<String>>)
          ?.mapValues { it.value.toMutableList() }?.toMutableMap() ?: mutableMapOf()

        val list = reactions.getOrPut(emoji) { mutableListOf() }
        if (list.contains(userId)) {
          list.remove(userId)
          if (list.isEmpty()) reactions.remove(emoji)
        } else {
          list.add(userId)
        }
        transaction.update(docRef, "reactions", reactions)
      }.await()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Mark messages as read
   */
  suspend fun markAsRead(conversationId: String, userId: String) {
    try {
      firestore.collection("conversations").document(conversationId).update(
        "unreadCounts.$userId", 0
      ).await()
    } catch (e: Exception) {
      // ignore
    }
  }

  /**
   * Search registered Pinggo users
   */
  suspend fun searchUsers(query: String, currentUserId: String): List<User> {
    val clean = query.trim().lowercase()
    if (clean.isEmpty()) return emptyList()

    return try {
      val results = mutableListOf<User>()

      // 1. Search by username prefix
      val usernameQuery = firestore.collection("users")
        .whereGreaterThanOrEqualTo("username", clean)
        .whereLessThanOrEqualTo("username", clean + "\uf8ff")
        .limit(20)
        .get()
        .await()

      for (doc in usernameQuery.documents) {
        val user = User.fromMap(doc.data ?: continue)
        if (user.uid != currentUserId && results.none { it.uid == user.uid }) {
          results.add(user)
        }
      }

      // 2. Search by displayName (case-insensitive local check on prefix match)
      if (results.size < 10) {
        val allUsers = firestore.collection("users").limit(30).get().await()
        for (doc in allUsers.documents) {
          val user = User.fromMap(doc.data ?: continue)
          if (user.uid != currentUserId && results.none { it.uid == user.uid }) {
            if (user.displayName.lowercase().contains(clean) || user.email.lowercase().contains(clean)) {
              results.add(user)
            }
          }
        }
      }
      results
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  /**
   * Pin or unpin conversation
   */
  suspend fun togglePin(conversationId: String, userId: String, isPinned: Boolean) {
    try {
      val docRef = firestore.collection("conversations").document(conversationId)
      val snapshot = docRef.get().await()
      @Suppress("UNCHECKED_CAST")
      val pinned = (snapshot.get("pinnedBy") as? List<String>)?.toMutableList() ?: mutableListOf()
      if (isPinned) {
        if (!pinned.contains(userId)) pinned.add(userId)
      } else {
        pinned.remove(userId)
      }
      docRef.update("pinnedBy", pinned).await()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Block / Report User
   */
  suspend fun blockUser(currentUserId: String, targetUserId: String) {
    try {
      firestore.collection("blockedUsers")
        .document(currentUserId)
        .collection("blocked")
        .document(targetUserId)
        .set(mapOf("blockedAt" to System.currentTimeMillis()))
        .await()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  suspend fun reportUser(reporterId: String, reportedUserId: String, reason: String) {
    try {
      val reportId = UUID.randomUUID().toString()
      firestore.collection("reports").document(reportId).set(
        mapOf(
          "reportId" to reportId,
          "reporterId" to reporterId,
          "reportedUserId" to reportedUserId,
          "reason" to reason,
          "timestamp" to System.currentTimeMillis()
        )
      ).await()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Updates / Status Stories
   */
  fun getStatusUpdatesFlow(): Flow<List<StatusUpdate>> = callbackFlow {
    val query = firestore.collection("updates")
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .limit(30)

    val listener = query.addSnapshotListener { snapshot, error ->
      if (error != null) {
        error.printStackTrace()
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val list = snapshot.documents.mapNotNull { doc ->
          val data = doc.data ?: return@mapNotNull null
          StatusUpdate.fromMap(data)
        }
        trySend(list)
      }
    }
    awaitClose { listener.remove() }
  }

  suspend fun postStatusUpdate(user: User, text: String, imageUrl: String = ""): Result<StatusUpdate> {
    return try {
      val id = UUID.randomUUID().toString()
      val update = StatusUpdate(
        id = id,
        userId = user.uid,
        userName = user.displayName.ifEmpty { user.username },
        userPhoto = user.photoURL,
        text = text,
        imageUrl = imageUrl,
        timestamp = System.currentTimeMillis()
      )
      firestore.collection("updates").document(id).set(update.toMap()).await()
      Result.success(update)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Call sessions signaling for real-time WebRTC / Voice / Video call
   */
  fun getIncomingCallsFlow(userId: String): Flow<CallSession?> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(null)
      awaitClose { }
      return@callbackFlow
    }

    val query = firestore.collection("calls")
      .whereEqualTo("receiverId", userId)
      .whereEqualTo("status", "ringing")

    val listener = query.addSnapshotListener { snapshot, _ ->
      val callDoc = snapshot?.documents?.firstOrNull()
      if (callDoc != null && callDoc.data != null) {
        trySend(CallSession.fromMap(callDoc.data!!))
      } else {
        trySend(null)
      }
    }
    awaitClose { listener.remove() }
  }

  suspend fun initiateCall(caller: User, receiver: User, type: String): Result<CallSession> {
    return try {
      val callId = UUID.randomUUID().toString()
      val call = CallSession(
        id = callId,
        callerId = caller.uid,
        receiverId = receiver.uid,
        callerName = caller.displayName.ifEmpty { caller.username },
        callerPhoto = caller.photoURL,
        receiverName = receiver.displayName.ifEmpty { receiver.username },
        receiverPhoto = receiver.photoURL,
        type = type,
        status = "ringing",
        createdAt = System.currentTimeMillis()
      )
      firestore.collection("calls").document(callId).set(call.toMap()).await()
      Result.success(call)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateCallStatus(callId: String, status: String) {
    try {
      firestore.collection("calls").document(callId).update("status", status).await()
    } catch (e: Exception) {
      // ignore
    }
  }
}
