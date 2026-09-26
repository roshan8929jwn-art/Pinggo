package com.example.data

import android.util.Log
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
  private val firestore: FirebaseFirestore?
    get() = try {
      FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
      Log.w("FirestoreChatRepo", "FirebaseFirestore not ready: ${e.message}")
      null
    }

  /**
   * Real-time listener for current user's conversations
   */
  fun getConversationsFlow(userId: String): Flow<List<Conversation>> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      val query = db.collection("conversations")
        .whereArrayContains("participants", userId)

      listener = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w("FirestoreChatRepo", "Conversations listener error: ${error.message}")
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
              null
            }
          }.sortedByDescending { it.lastMessageTimestamp }
          trySend(list)
        }
      }
    } catch (e: Throwable) {
      Log.e("FirestoreChatRepo", "Failed to start conversations flow", e)
      trySend(emptyList())
    }

    awaitClose {
      try {
        listener?.remove()
      } catch (e: Throwable) {
        // ignore
      }
    }
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

    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      val query = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .orderBy("timestamp", Query.Direction.ASCENDING)

      listener = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w("FirestoreChatRepo", "Messages listener error: ${error.message}")
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
    } catch (e: Throwable) {
      Log.e("FirestoreChatRepo", "Failed to start messages flow", e)
      trySend(emptyList())
    }

    awaitClose {
      try {
        listener?.remove()
      } catch (e: Throwable) {
        // ignore
      }
    }
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

    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      listener = db.collection("conversations")
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
    } catch (e: Throwable) {
      trySend(emptyList())
    }

    awaitClose {
      try {
        listener?.remove()
      } catch (e: Throwable) {
        // ignore
      }
    }
  }

  suspend fun setTyping(conversationId: String, userId: String, userName: String, isTyping: Boolean) {
    if (conversationId.isEmpty() || userId.isEmpty()) return
    val db = firestore ?: return
    try {
      val docRef = db.collection("conversations")
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
   * Send a message to a conversation
   */
  suspend fun sendMessage(
    conversationId: String,
    sender: User,
    text: String,
    type: String = "text", // "text", "voice", "image", "file"
    mediaUrl: String = "",
    mediaDurationSec: Int = 0,
    mediaSize: Long = 0,
    replyTo: Message? = null
  ): Result<Message> {
    val db = firestore ?: return Result.failure(IllegalStateException("Cloud Firestore is not available"))
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
        voiceDurationSec = mediaDurationSec,
        timestamp = now,
        delivered = true,
        read = false,
        replyToMessageId = replyTo?.id ?: "",
        replyToText = replyTo?.text ?: "",
        replyToSender = replyTo?.senderName ?: ""
      )

      // 1. Add message to subcollection
      db.collection("conversations")
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

      db.collection("conversations").document(conversationId).update(
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
    val db = firestore ?: return Result.failure(IllegalStateException("Cloud Firestore is not available"))
    if (currentUser.uid == targetUser.uid) {
      return Result.failure(IllegalArgumentException("You cannot message yourself"))
    }
    if (currentUser.blockedUsers.contains(targetUser.uid)) {
      return Result.failure(IllegalStateException("You have blocked this user. Unblock them in Privacy Settings to chat."))
    }
    if (targetUser.blockedUsers.contains(currentUser.uid)) {
      return Result.failure(IllegalStateException("Unable to start chat with this user."))
    }

    return try {
      // Look for existing conversation with these two participants
      val existing = db.collection("conversations")
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

      db.collection("conversations").document(convId).set(data).await()
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
    val db = firestore ?: return Result.failure(IllegalStateException("Cloud Firestore is not available"))
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

      db.collection("conversations").document(convId).set(data).await()
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
    val db = firestore ?: return
    try {
      val docRef = db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(messageId)

      db.runTransaction { transaction ->
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
  suspend fun markAsRead(conversationId: String, userId: String, sendReadReceipts: Boolean = true) {
    val db = firestore ?: return
    try {
      db.collection("conversations").document(conversationId).update(
        "unreadCounts.$userId", 0
      ).await()

      if (sendReadReceipts) {
        val messagesSnapshot = db.collection("conversations").document(conversationId)
          .collection("messages")
          .whereEqualTo("read", false)
          .limit(30)
          .get()
          .await()

        for (doc in messagesSnapshot.documents) {
          val senderId = doc.getString("senderId")
          if (senderId != userId) {
            doc.reference.update(mapOf("read" to true)).await()
          }
        }
      }
    } catch (e: Exception) {
      // ignore
    }
  }

  /**
   * Search registered Pinggo users
   */
  suspend fun searchUsers(query: String, currentUserId: String): List<User> {
    val clean = query.trim().lowercase().removePrefix("@")
    if (clean.isEmpty()) return emptyList()
    val db = firestore ?: return emptyList()

    return try {
      val results = mutableListOf<User>()

      // 1. Search by exact usernameLowercase
      val exactMatch = db.collection("users")
        .whereEqualTo("usernameLowercase", clean)
        .limit(5)
        .get()
        .await()

      for (doc in exactMatch.documents) {
        val user = User.fromMap(doc.data ?: continue)
        if (user.uid != currentUserId && results.none { it.uid == user.uid }) {
          results.add(user)
        }
      }

      // 2. Search by usernameLowercase prefix if no exact match or to fill results
      if (results.size < 10) {
        val prefixQuery = db.collection("users")
          .whereGreaterThanOrEqualTo("usernameLowercase", clean)
          .whereLessThanOrEqualTo("usernameLowercase", clean + "\uf8ff")
          .limit(10)
          .get()
          .await()

        for (doc in prefixQuery.documents) {
          val user = User.fromMap(doc.data ?: continue)
          if (user.uid != currentUserId && results.none { it.uid == user.uid }) {
            results.add(user)
          }
        }
      }

      // 3. Search by displayName (case-insensitive local check)
      if (results.size < 15) {
        val allUsers = db.collection("users").limit(40).get().await()
        for (doc in allUsers.documents) {
          val user = User.fromMap(doc.data ?: continue)
          if (user.uid != currentUserId && results.none { it.uid == user.uid }) {
            if (user.displayName.lowercase().contains(clean)) {
              results.add(user)
            }
          }
        }
      }
      results
    } catch (e: Exception) {
      Log.w("FirestoreChatRepo", "searchUsers error: ${e.message}")
      emptyList()
    }
  }

  /**
   * Pin or unpin conversation
   */
  suspend fun togglePin(conversationId: String, userId: String, isPinned: Boolean) {
    val db = firestore ?: return
    try {
      val docRef = db.collection("conversations").document(conversationId)
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
    val db = firestore ?: return
    try {
      db.collection("blockedUsers")
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
    val db = firestore ?: return
    try {
      val reportId = UUID.randomUUID().toString()
      db.collection("reports").document(reportId).set(
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
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      val query = db.collection("updates")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(40)

      listener = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w("FirestoreChatRepo", "Status updates error: ${error.message}")
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
    } catch (e: Throwable) {
      trySend(emptyList())
    }

    awaitClose {
      try {
        listener?.remove()
      } catch (e: Throwable) {
        // ignore
      }
    }
  }

  suspend fun postStatusUpdate(
    user: User,
    text: String,
    imageUrl: String = "",
    mediaType: String = "image"
  ): Result<StatusUpdate> {
    val db = firestore ?: return Result.failure(IllegalStateException("Cloud Firestore is not available"))
    return try {
      val id = UUID.randomUUID().toString()
      val update = StatusUpdate(
        id = id,
        userId = user.uid,
        userName = user.displayName.ifEmpty { user.username },
        userPhoto = user.photoURL,
        text = text,
        imageUrl = imageUrl,
        mediaType = mediaType,
        timestamp = System.currentTimeMillis()
      )
      db.collection("updates").document(id).set(update.toMap()).await()
      Result.success(update)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun recordStatusView(updateId: String, viewer: User) {
    if (updateId.isEmpty() || viewer.uid.isEmpty()) return
    val db = firestore ?: return
    try {
      val now = System.currentTimeMillis()
      db.collection("updates").document(updateId).update("viewers.${viewer.uid}", now).await()
      db.collection("updates").document(updateId)
        .collection("viewers").document(viewer.uid)
        .set(
          mapOf(
            "uid" to viewer.uid,
            "displayName" to viewer.displayName,
            "username" to viewer.username,
            "usernameLowercase" to viewer.usernameLowercase,
            "photoURL" to viewer.photoURL,
            "viewedAt" to now
          )
        ).await()
    } catch (e: Exception) {
      // ignore
    }
  }

  suspend fun getStatusViewers(updateId: String): List<com.example.model.StatusViewer> {
    val db = firestore ?: return emptyList()
    return try {
      val snapshot = db.collection("updates").document(updateId)
        .collection("viewers")
        .orderBy("viewedAt", Query.Direction.DESCENDING)
        .get().await()

      snapshot.documents.mapNotNull { doc ->
        val data = doc.data ?: return@mapNotNull null
        com.example.model.StatusViewer(
          uid = data["uid"] as? String ?: doc.id,
          displayName = data["displayName"] as? String ?: "",
          username = data["username"] as? String ?: "",
          photoURL = data["photoURL"] as? String ?: "",
          viewedAt = (data["viewedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
      }
    } catch (e: Exception) {
      emptyList()
    }
  }

  /**
   * Real Call History (incoming and outgoing)
   */
  fun getCallHistoryFlow(userId: String): Flow<List<CallSession>> = callbackFlow {
    if (userId.isEmpty()) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      // Listen to calls where current user was caller or receiver
      listener = db.collection("calls")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(50)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("FirestoreChatRepo", "Call history listener error: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null) {
            val list = snapshot.documents.mapNotNull { doc ->
              val data = doc.data ?: return@mapNotNull null
              val session = CallSession.fromMap(data)
              if (session.callerId == userId || session.receiverId == userId) {
                session
              } else null
            }
            trySend(list)
          }
        }
    } catch (e: Throwable) {
      trySend(emptyList())
    }

    awaitClose {
      try {
        listener?.remove()
      } catch (e: Throwable) {
        // ignore
      }
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

    val db = firestore
    if (db == null) {
      trySend(null)
      awaitClose { }
      return@callbackFlow
    }

    var listener: ListenerRegistration? = null
    try {
      val query = db.collection("calls")
        .whereEqualTo("receiverId", userId)
        .whereEqualTo("status", "ringing")

      listener = query.addSnapshotListener { snapshot, _ ->
        val callDoc = snapshot?.documents?.firstOrNull()
        if (callDoc != null && callDoc.data != null) {
          trySend(CallSession.fromMap(callDoc.data!!))
        } else {
          trySend(null)
        }
      }
    } catch (e: Throwable) {
      trySend(null)
    }

    awaitClose {
      try {
        listener?.remove()
      } catch (e: Throwable) {
        // ignore
      }
    }
  }

  suspend fun initiateCall(caller: User, receiver: User, type: String): Result<CallSession> {
    val db = firestore ?: return Result.failure(IllegalStateException("Cloud Firestore is not available"))
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
      db.collection("calls").document(callId).set(call.toMap()).await()
      Result.success(call)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateCallStatus(callId: String, status: String, durationSec: Int = 0) {
    val db = firestore ?: return
    try {
      val updates = mutableMapOf<String, Any>("status" to status)
      if (durationSec > 0) {
        updates["durationSec"] = durationSec
      }
      db.collection("calls").document(callId).update(updates).await()
    } catch (e: Exception) {
      // ignore
    }
  }

  // --- Friend Requests & Notifications ---

  suspend fun sendFriendRequest(sender: User, receiverId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
    try {
      val requestId = "${sender.uid}_$receiverId"
      val request = com.example.model.FriendRequest(
        id = requestId,
        senderId = sender.uid,
        senderName = sender.displayName,
        senderUsername = sender.username,
        senderPhoto = sender.photoURL,
        receiverId = receiverId,
        status = "pending",
        timestamp = System.currentTimeMillis()
      )

      db.collection("friend_requests").document(requestId).set(request.toMap()).await()

      // Create notification for receiver
      val notifId = UUID.randomUUID().toString()
      val notif = com.example.model.Notification(
        id = notifId,
        recipientId = receiverId,
        senderId = sender.uid,
        senderName = sender.displayName,
        senderUsername = sender.username,
        senderPhoto = sender.photoURL,
        type = "friend_request",
        content = "${sender.displayName} (@${sender.username}) sent you a friend request.",
        relatedId = requestId
      )
      db.collection("notifications").document(notifId).set(notif.toMap()).await()

      return Result.success(Unit)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }

  suspend fun cancelFriendRequest(senderId: String, receiverId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
    try {
      val requestId = "${senderId}_$receiverId"
      db.collection("friend_requests").document(requestId).delete().await()
      return Result.success(Unit)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }

  suspend fun acceptFriendRequest(requestId: String, receiver: User): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
    try {
      val requestDoc = db.collection("friend_requests").document(requestId).get().await()
      if (!requestDoc.exists()) return Result.failure(Exception("Request not found"))
      
      val senderId = requestDoc.getString("senderId") ?: ""
      val senderName = requestDoc.getString("senderName") ?: ""
      
      db.runTransaction { transaction ->
        // Update request status
        transaction.update(db.collection("friend_requests").document(requestId), "status", "accepted")
        
        // Update both users friends list
        val receiverRef = db.collection("users").document(receiver.uid)
        val senderRef = db.collection("users").document(senderId)
        
        transaction.update(receiverRef, "friends", com.google.firebase.firestore.FieldValue.arrayUnion(senderId))
        transaction.update(senderRef, "friends", com.google.firebase.firestore.FieldValue.arrayUnion(receiver.uid))
      }.await()

      // Create notification for sender
      val notifId = UUID.randomUUID().toString()
      val notif = com.example.model.Notification(
        id = notifId,
        recipientId = senderId,
        senderId = receiver.uid,
        senderName = receiver.displayName,
        senderUsername = receiver.username,
        senderPhoto = receiver.photoURL,
        type = "request_accepted",
        content = "${receiver.displayName} accepted your friend request!",
        relatedId = requestId
      )
      db.collection("notifications").document(notifId).set(notif.toMap()).await()

      return Result.success(Unit)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }

  suspend fun declineFriendRequest(requestId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
    try {
      db.collection("friend_requests").document(requestId).update("status", "declined").await()
      return Result.success(Unit)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }

  fun getNotificationsFlow(userId: String): Flow<List<com.example.model.Notification>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val listener = db.collection("notifications")
      .whereEqualTo("recipientId", userId)
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc ->
            com.example.model.Notification.fromMap(doc.id, doc.data ?: return@mapNotNull null)
          }
          trySend(list)
        }
      }
    awaitClose { listener.remove() }
  }

  suspend fun markNotificationAsRead(notificationId: String) {
    firestore?.collection("notifications")?.document(notificationId)?.update("isRead", true)?.await()
  }

  suspend fun clearAllNotifications(userId: String) {
    val db = firestore ?: return
    try {
      val snapshot = db.collection("notifications")
        .whereEqualTo("recipientId", userId)
        .get().await()
      db.runBatch { batch ->
        for (doc in snapshot.documents) {
          batch.delete(doc.reference)
        }
      }.await()
    } catch (e: Exception) { }
  }

  fun getFriendRequestsFlow(userId: String): Flow<List<com.example.model.FriendRequest>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val listener = db.collection("friend_requests")
      .whereEqualTo("receiverId", userId)
      .whereEqualTo("status", "pending")
      .addSnapshotListener { snapshot, error ->
        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc ->
            com.example.model.FriendRequest.fromMap(doc.id, doc.data ?: return@mapNotNull null)
          }
          trySend(list)
        }
      }
    awaitClose { listener.remove() }
  }

  fun getOutgoingRequestsFlow(userId: String): Flow<List<com.example.model.FriendRequest>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      awaitClose { }
      return@callbackFlow
    }
    val listener = db.collection("friend_requests")
      .whereEqualTo("senderId", userId)
      .whereEqualTo("status", "pending")
      .addSnapshotListener { snapshot, error ->
        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc ->
            com.example.model.FriendRequest.fromMap(doc.id, doc.data ?: return@mapNotNull null)
          }
          trySend(list)
        }
      }
    awaitClose { listener.remove() }
  }
}
