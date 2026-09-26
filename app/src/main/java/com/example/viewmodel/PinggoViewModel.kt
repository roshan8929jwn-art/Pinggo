package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseAuthRepository
import com.example.data.FirebaseStorageRepository
import com.example.data.FirestoreChatRepository
import com.example.data.VoiceRecorderHelper
import com.example.model.CallSession
import com.example.model.Conversation
import com.example.model.Message
import com.example.model.StatusUpdate
import com.example.model.User
import com.example.ui.theme.AppThemeMode
import com.example.model.ParticipantInfo
import com.example.util.FirebaseDiagnosticInfo
import com.example.util.FirebaseInitializer
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class PinggoViewModel(application: Application) : AndroidViewModel(application) {
  val authRepo = FirebaseAuthRepository(application)
  val chatRepo = FirestoreChatRepository()
  val storageRepo = FirebaseStorageRepository()
  val voiceHelper = VoiceRecorderHelper(application)

  val firebaseDiagnostic: StateFlow<FirebaseDiagnosticInfo> = FirebaseInitializer.diagnostic

  private val _isGuestMode = MutableStateFlow(false)
  val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

  private val _guestProfile = MutableStateFlow<User?>(null)

  // Auth & Profile
  val currentUser: StateFlow<FirebaseUser?> = authRepo.currentUser
  val userProfile: StateFlow<User?> = combine(authRepo.userProfile, _guestProfile) { authUser, guest ->
    authUser ?: guest
  }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  private val _isAuthLoading = MutableStateFlow(false)
  val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

  private val _authError = MutableStateFlow<String?>(null)
  val authError: StateFlow<String?> = _authError.asStateFlow()

  // App Theme
  private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  private val _glassDesign = MutableStateFlow(com.example.ui.theme.GlassDesign.CLEAR)
  val glassDesign: StateFlow<com.example.ui.theme.GlassDesign> = _glassDesign.asStateFlow()

  fun setGlassDesign(design: com.example.ui.theme.GlassDesign) {
    _glassDesign.value = design
    viewModelScope.launch {
      val prefs = getApplication<Application>().getSharedPreferences("pinggo_settings", android.content.Context.MODE_PRIVATE)
      prefs.edit().putString("glass_design", design.name).apply()
    }
  }

  // Username validation state
  private val _usernameCheckState = MutableStateFlow<Boolean?>(null) // null = unchecked, true = ok, false = taken
  val usernameCheckState: StateFlow<Boolean?> = _usernameCheckState.asStateFlow()

  // Conversations
  private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
  val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

  private val _selectedFilter = MutableStateFlow("All") // "All", "Unread", "Groups", "Personal"
  val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

  // Active Conversation & Messages
  private val _activeConversation = MutableStateFlow<Conversation?>(null)
  val activeConversation: StateFlow<Conversation?> = _activeConversation.asStateFlow()

  private val _activeMessages = MutableStateFlow<List<Message>>(emptyList())
  val activeMessages: StateFlow<List<Message>> = _activeMessages.asStateFlow()

  private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
  val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

  private val _replyingTo = MutableStateFlow<Message?>(null)
  val replyingTo: StateFlow<Message?> = _replyingTo.asStateFlow()

  // Search
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<User>>(emptyList())
  val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

  private val _isSearching = MutableStateFlow(false)
  val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

  private val _previewUser = MutableStateFlow<User?>(null)
  val previewUser: StateFlow<User?> = _previewUser.asStateFlow()

  // Calls
  private val _activeCall = MutableStateFlow<CallSession?>(null)
  val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

  private val _callDurationSec = MutableStateFlow(0)
  val callDurationSec: StateFlow<Int> = _callDurationSec.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _isSpeakerOn = MutableStateFlow(false)
  val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

  private val _isVideoOn = MutableStateFlow(true)
  val isVideoOn: StateFlow<Boolean> = _isVideoOn.asStateFlow()

  // Status Updates / Stories
  private val _statusUpdates = MutableStateFlow<List<StatusUpdate>>(emptyList())
  val statusUpdates: StateFlow<List<StatusUpdate>> = _statusUpdates.asStateFlow()

  // Real Call History
  private val _callHistory = MutableStateFlow<List<CallSession>>(emptyList())
  val callHistory: StateFlow<List<CallSession>> = _callHistory.asStateFlow()

  // Blocked Users
  private val _blockedUsersList = MutableStateFlow<List<User>>(emptyList())
  val blockedUsersList: StateFlow<List<User>> = _blockedUsersList.asStateFlow()

  // Toast / Status Message
  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private var conversationsJob: Job? = null
  private var messagesJob: Job? = null
  private var typingJob: Job? = null
  private var incomingCallJob: Job? = null
  private var callTimerJob: Job? = null
  private var callHistoryJob: Job? = null
  private var statusUpdatesJob: Job? = null

  init {
    val prefs = application.getSharedPreferences("pinggo_settings", android.content.Context.MODE_PRIVATE)
    val savedDesign = prefs.getString("glass_design", "CLEAR") ?: "CLEAR"
    _glassDesign.value = try {
      com.example.ui.theme.GlassDesign.valueOf(savedDesign)
    } catch (e: Exception) {
      com.example.ui.theme.GlassDesign.CLEAR
    }

    viewModelScope.launch {
      currentUser.collect { user ->
        if (user != null) {
          authRepo.loadUserProfile(user.uid)
          listenToConversations(user.uid)
          listenToIncomingCalls(user.uid)
          listenToCallHistory(user.uid)
          listenToStatusUpdates()
          
          // Migration for existing users
          viewModelScope.launch {
            userProfile.collect { profile ->
              if (profile != null && profile.usernameLowercase.isEmpty() && profile.username.isNotEmpty()) {
                val updated = profile.copy(usernameLowercase = profile.username.lowercase())
                authRepo.updateUserProfile(updated)
              }
            }
          }
        } else if (!_isGuestMode.value) {
          _conversations.value = emptyList()
          _activeConversation.value = null
          _activeMessages.value = emptyList()
          _statusUpdates.value = emptyList()
          _callHistory.value = emptyList()
          _blockedUsersList.value = emptyList()
        }
      }
    }
  }

  private fun listenToStatusUpdates() {
    statusUpdatesJob?.cancel()
    statusUpdatesJob = viewModelScope.launch {
      chatRepo.getStatusUpdatesFlow().collect { list ->
        _statusUpdates.value = list
      }
    }
  }

  fun enterGuestMode() {
    _isGuestMode.value = true
    val guest = User(
      uid = "guest_pinggo_user",
      displayName = "Pinggo Explorer",
      username = "pinggouser",
      email = "explorer@pinggo.internal",
      bio = "Exploring the Pinggo Liquid Glass Experience ✨",
      createdAt = System.currentTimeMillis(),
      isOnline = true
    )
    _guestProfile.value = guest

    val demoDetails = mapOf(
      guest.uid to ParticipantInfo(guest.uid, guest.displayName, guest.username, guest.photoURL),
      "ai_sparky" to ParticipantInfo("ai_sparky", "Pinggo AI Assistant", "pinggo_ai", "")
    )
    _conversations.value = listOf(
      Conversation(
        id = "conv_ai_demo",
        groupName = "Pinggo AI Assistant",
        type = "ai",
        participants = listOf(guest.uid, "ai_sparky"),
        participantDetails = demoDetails,
        lastMessage = "Welcome to Pinggo! You are exploring in Demo Mode. Connect Firebase in Firebase Console to enable global cloud sync!",
        lastMessageTimestamp = System.currentTimeMillis(),
        unreadCounts = mapOf(guest.uid to 0)
      )
    )
  }

  fun retryFirebaseInitialization() {
    viewModelScope.launch {
      FirebaseInitializer.initialize(getApplication())
      currentUser.value?.let { user ->
        authRepo.loadUserProfile(user.uid)
        listenToConversations(user.uid)
        listenToStatusUpdates()
      }
    }
  }

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
    viewModelScope.launch {
      delay(3000)
      if (_toastMessage.value == msg) {
        _toastMessage.value = null
      }
    }
  }

  fun setFilter(filter: String) {
    _selectedFilter.value = filter
  }

  fun checkUsername(username: String) {
    viewModelScope.launch {
      val isAvail = authRepo.isUsernameAvailable(username)
      _usernameCheckState.value = isAvail
    }
  }

  fun formatAuthError(err: Throwable): String {
    val msg = err.message ?: ""
    val errorCode = if (err is com.google.firebase.auth.FirebaseAuthException) "[${err.errorCode}] " else ""
    val projectId = FirebaseInitializer.diagnostic.value.projectId

    return when {
      msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) -> {
        "CONFIGURATION_NOT_FOUND: Firebase Authentication is not fully configured for project $projectId.\n\n" +
        "1. Ensure the Google provider is ENABLED in Firebase Console.\n" +
        "2. Ensure the Web Client ID is correctly added to AI Studio secrets.\n" +
        "3. Verify that the SHA-1 fingerprints are added to your Firebase project."
      }
      msg.contains("DEVELOPER_ERROR", ignoreCase = true) -> {
        "DEVELOPER_ERROR: This usually means the SHA-1 fingerprint of the signing key is not registered in the Firebase Console, or the Web Client ID is invalid for this project ($projectId)."
      }
      msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
      msg.contains("wrong password", ignoreCase = true) ||
      msg.contains("user-not-found", ignoreCase = true) -> {
        "Invalid email or password. Please verify and try again."
      }
      msg.contains("email-already-in-use", ignoreCase = true) ||
      msg.contains("EMAIL_EXISTS", ignoreCase = true) -> {
        "This email is already in use. Please sign in instead."
      }
      msg.contains("weak-password", ignoreCase = true) -> {
        "Password must be at least 6 characters."
      }
      msg.contains("invalid-email", ignoreCase = true) -> {
        "Please enter a valid email address."
      }
      msg.contains("No credential available", ignoreCase = true) -> {
        "No Google account credentials available on this device."
      }
      msg.contains("network-request-failed", ignoreCase = true) -> {
        "Network error. Please check your internet connection."
      }
      else -> "${errorCode}${msg}".ifEmpty { "Authentication failed (${err.javaClass.simpleName})" }
    }
  }

  fun signInWithGoogle() {
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val result = authRepo.signInWithGoogle()
      result.onSuccess { user ->
        val profile = authRepo.loadUserProfile(user.uid)
        _isAuthLoading.value = false
        if (profile == null) {
          showToast("Welcome! Please setup your Pinggo profile 🐧")
        } else {
          showToast("Welcome back, ${profile.displayName}!")
        }
      }.onFailure { err ->
        _isAuthLoading.value = false
        _authError.value = formatAuthError(err)
      }
    }
  }

  fun signInWithEmail(email: String, pass: String) {
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val result = authRepo.signInWithEmail(email, pass)
      result.onSuccess { user ->
        authRepo.loadUserProfile(user.uid)
        _isAuthLoading.value = false
        showToast("Signed in successfully!")
      }.onFailure { err ->
        _isAuthLoading.value = false
        _authError.value = formatAuthError(err)
      }
    }
  }

  fun signUpWithEmail(email: String, pass: String) {
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val result = authRepo.signUpWithEmail(email, pass)
      result.onSuccess { user ->
        _isAuthLoading.value = false
        showToast("Account created! Please set up your profile 🐧")
      }.onFailure { err ->
        _isAuthLoading.value = false
        _authError.value = formatAuthError(err)
      }
    }
  }

  fun continueAsDirectUser(name: String, email: String) {
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val result = authRepo.signInAnonymously()
      result.onSuccess { user ->
        _isAuthLoading.value = false
      }.onFailure { err ->
        _isAuthLoading.value = false
        _authError.value = formatAuthError(err)
      }
    }
  }

  fun createProfile(displayName: String, username: String, bio: String, photoUrl: String, password: String? = null, onComplete: () -> Unit) {
    viewModelScope.launch {
      _isAuthLoading.value = true
      
      // If a password was provided (e.g. for a new Google account setup), link it.
      if (!password.isNullOrBlank() && currentUser.value?.email != null) {
         authRepo.linkEmailPassword(currentUser.value!!.email!!, password)
      }

      val res = authRepo.createUserProfile(displayName, username, bio, photoUrl)
      _isAuthLoading.value = false
      res.onSuccess {
        showToast("Profile created! Welcome to Pinggo 🐧")
        onComplete()
      }.onFailure { err ->
        _authError.value = err.message ?: "Could not create profile"
      }
    }
  }

  fun signOut() {
    viewModelScope.launch {
      if (_isGuestMode.value) {
        _isGuestMode.value = false
        _guestProfile.value = null
        _conversations.value = emptyList()
        _activeConversation.value = null
        _activeMessages.value = emptyList()
      } else {
        authRepo.signOut()
      }
      showToast("Signed out")
    }
  }

  private fun listenToConversations(userId: String) {
    conversationsJob?.cancel()
    conversationsJob = viewModelScope.launch {
      chatRepo.getConversationsFlow(userId).collect { list ->
        _conversations.value = list
      }
    }
  }

  fun openConversation(conversation: Conversation) {
    _activeConversation.value = conversation
    val user = userProfile.value ?: return

    if (conversation.id == "conv_ai_demo" && _activeMessages.value.isEmpty()) {
      _activeMessages.value = listOf(
        Message(
          id = "msg_ai_welcome",
          conversationId = conversation.id,
          senderId = "ai_sparky",
          senderName = "Pinggo AI Assistant",
          text = "Welcome to Pinggo! You are exploring in Demo Mode. Connect Firebase in Firebase Console to enable global cloud sync!",
          timestamp = System.currentTimeMillis()
        )
      )
      return
    }

    viewModelScope.launch {
      chatRepo.markAsRead(conversation.id, user.uid)
    }

    messagesJob?.cancel()
    messagesJob = viewModelScope.launch {
      chatRepo.getMessagesFlow(conversation.id).collect { msgs ->
        _activeMessages.value = msgs
      }
    }

    typingJob?.cancel()
    typingJob = viewModelScope.launch {
      chatRepo.getTypingFlow(conversation.id, user.uid).collect { typers ->
        _typingUsers.value = typers
      }
    }
  }

  fun closeConversation() {
    _activeConversation.value = null
    _activeMessages.value = emptyList()
    _typingUsers.value = emptyList()
    _replyingTo.value = null
    messagesJob?.cancel()
    typingJob?.cancel()
  }

  fun setTyping(isTyping: Boolean) {
    val conv = _activeConversation.value ?: return
    val user = userProfile.value ?: return
    viewModelScope.launch {
      chatRepo.setTyping(conv.id, user.uid, user.displayName.ifEmpty { user.username }, isTyping)
    }
  }

  fun setReplyingTo(message: Message?) {
    _replyingTo.value = message
  }

  fun sendMessage(text: String) {
    val conv = _activeConversation.value ?: return
    val user = userProfile.value ?: return
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return

    val reply = _replyingTo.value
    _replyingTo.value = null

    if (conv.id == "conv_ai_demo" || _isGuestMode.value) {
      val msgId = java.util.UUID.randomUUID().toString()
      val msg = Message(
        id = msgId,
        conversationId = conv.id,
        senderId = user.uid,
        senderName = user.displayName,
        text = trimmed,
        timestamp = System.currentTimeMillis()
      )
      _activeMessages.value = _activeMessages.value + msg
      viewModelScope.launch {
        delay(800)
        val aiReply = Message(
          id = java.util.UUID.randomUUID().toString(),
          conversationId = conv.id,
          senderId = "ai_sparky",
          senderName = "Pinggo AI Assistant",
          text = "I received your message: \"$trimmed\"! The Pinggo Liquid Glass interface is running smoothly.",
          timestamp = System.currentTimeMillis()
        )
        _activeMessages.value = _activeMessages.value + aiReply
      }
      return
    }

    viewModelScope.launch {
      chatRepo.sendMessage(
        conversationId = conv.id,
        sender = user,
        text = trimmed,
        type = "text",
        replyTo = reply
      )
    }
  }

  fun sendImageMessage(uri: Uri) {
    val conv = _activeConversation.value ?: return
    val user = userProfile.value ?: return
    viewModelScope.launch {
      showToast("Uploading photo...")
      val uploadRes = storageRepo.uploadImage(user.uid, uri)
      uploadRes.onSuccess { downloadUrl ->
        chatRepo.sendMessage(
          conversationId = conv.id,
          sender = user,
          text = "Photo",
          type = "image",
          mediaUrl = downloadUrl
        )
      }.onFailure {
        showToast("Failed to upload image")
      }
    }
  }

  fun startVoiceRecording() {
    voiceHelper.startRecording()
  }

  fun stopAndSendVoiceRecording() {
    val audioFile = voiceHelper.stopRecording()
    val conv = _activeConversation.value ?: return
    val user = userProfile.value ?: return
    val duration = voiceHelper.recordingDurationSec.value

    if (audioFile == null || !audioFile.exists()) {
      showToast("Audio recording failed")
      return
    }

    viewModelScope.launch {
      showToast("Sending voice note...")
      val res = storageRepo.uploadVoiceNote(user.uid, audioFile)
      res.onSuccess { downloadUrl ->
        chatRepo.sendMessage(
          conversationId = conv.id,
          sender = user,
          text = "Voice message ($duration s)",
          type = "voice",
          mediaUrl = downloadUrl,
          mediaDurationSec = duration
        )
      }.onFailure {
        showToast("Failed to upload voice note")
      }
    }
  }

  fun cancelVoiceRecording() {
    voiceHelper.cancelRecording()
  }

  fun playVoiceMessage(message: Message) {
    if (message.mediaUrl.isNotEmpty()) {
      voiceHelper.playAudio(message.mediaUrl, message.id)
    }
  }

  fun toggleReaction(message: Message, emoji: String) {
    val conv = _activeConversation.value ?: return
    val user = userProfile.value ?: return
    viewModelScope.launch {
      chatRepo.addReaction(conv.id, message.id, user.uid, emoji)
    }
  }

  fun searchUsers(query: String) {
    _searchQuery.value = query
    val user = userProfile.value ?: return
    if (query.trim().isEmpty()) {
      _searchResults.value = emptyList()
      return
    }
    viewModelScope.launch {
      _isSearching.value = true
      val res = chatRepo.searchUsers(query, user.uid)
      _searchResults.value = res
      _isSearching.value = false
    }
  }

  fun setPreviewUser(user: User?) {
    _previewUser.value = user
  }

  fun startDirectChat(targetUser: User, onReady: (Conversation) -> Unit) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      val res = chatRepo.getOrCreateDirectConversation(me, targetUser)
      res.onSuccess { conv ->
        openConversation(conv)
        onReady(conv)
      }.onFailure {
        showToast("Could not open chat: ${it.message}")
      }
    }
  }

  fun createGroup(name: String, desc: String, members: List<User>, onCreated: (Conversation) -> Unit) {
    val me = userProfile.value ?: return
    if (name.trim().isEmpty()) {
      showToast("Group name cannot be empty")
      return
    }
    viewModelScope.launch {
      val res = chatRepo.createGroup(me, name.trim(), desc.trim(), "", members)
      res.onSuccess { conv ->
        openConversation(conv)
        onCreated(conv)
        showToast("Group \"$name\" created! 🐧")
      }.onFailure {
        showToast("Failed to create group")
      }
    }
  }

  fun blockUser(targetUser: User) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      chatRepo.blockUser(me.uid, targetUser.uid)
      _previewUser.value = null
      showToast("Blocked @${targetUser.username}")
    }
  }

  fun reportUser(targetUser: User, reason: String) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      chatRepo.reportUser(me.uid, targetUser.uid, reason)
      _previewUser.value = null
      showToast("Report submitted. Thank you for keeping Pinggo safe.")
    }
  }

  fun updateProfile(
    displayName: String,
    username: String,
    bio: String,
    photoUri: Uri?,
    onComplete: (Boolean, String?) -> Unit
  ) {
    val user = userProfile.value ?: run {
      onComplete(false, "No active user")
      return
    }
    viewModelScope.launch {
      _isAuthLoading.value = true
      var photoUrl = user.photoURL
      if (photoUri != null) {
        val uploadRes = storageRepo.uploadAvatar(user.uid, photoUri)
        if (uploadRes.isSuccess) {
          photoUrl = uploadRes.getOrNull() ?: photoUrl
        } else {
          _isAuthLoading.value = false
          val msg = "Photo upload failed: ${uploadRes.exceptionOrNull()?.message}"
          showToast(msg)
          onComplete(false, msg)
          return@launch
        }
      }

      val res = authRepo.updateUserProfileWithUsernameChange(
        displayName = displayName.trim(),
        newUsername = username.trim(),
        bio = bio.trim(),
        photoUrl = photoUrl
      )

      _isAuthLoading.value = false
      if (res.isSuccess) {
        showToast("Profile updated successfully! ✨")
        onComplete(true, null)
      } else {
        val err = res.exceptionOrNull()?.message ?: "Failed to update profile"
        showToast(err)
        onComplete(false, err)
      }
    }
  }

  fun postStatus(text: String) {
    val me = userProfile.value ?: return
    if (text.trim().isEmpty()) return
    viewModelScope.launch {
      chatRepo.postStatusUpdate(me, text.trim())
      showToast("Status updated! ✨")
    }
  }

  fun uploadStatusMedia(
    mediaUri: Uri,
    isVideo: Boolean,
    caption: String,
    onProgress: (Boolean) -> Unit,
    onComplete: (Boolean) -> Unit
  ) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      onProgress(true)
      val uploadRes = storageRepo.uploadStatusMedia(me.uid, mediaUri, isVideo)
      if (uploadRes.isSuccess) {
        val url = uploadRes.getOrNull() ?: ""
        chatRepo.postStatusUpdate(me, caption, url, if (isVideo) "video" else "image")
        onProgress(false)
        onComplete(true)
        showToast("Status posted! ✨")
      } else {
        onProgress(false)
        onComplete(false)
        showToast("Failed to upload status: ${uploadRes.exceptionOrNull()?.message}")
      }
    }
  }

  fun recordStatusView(updateId: String) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      chatRepo.recordStatusView(updateId, me)
    }
  }

  fun loadStatusViewers(updateId: String, onResult: (List<com.example.model.StatusViewer>) -> Unit) {
    viewModelScope.launch {
      val viewers = chatRepo.getStatusViewers(updateId)
      onResult(viewers)
    }
  }

  // Privacy & Blocking
  fun updatePrivacySettings(lastSeen: String, readReceipts: Boolean, statusPrivacy: String) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      val res = authRepo.updatePrivacySettings(me.uid, lastSeen, readReceipts, statusPrivacy)
      if (res.isSuccess) {
        showToast("Privacy settings updated")
      } else {
        showToast("Failed to update privacy settings")
      }
    }
  }

  fun updateNotificationSettings(message: Boolean, group: Boolean, preview: Boolean, call: Boolean) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      val res = authRepo.updateNotificationSettings(me.uid, message, group, preview, call)
      if (res.isSuccess) {
        showToast("Notification settings updated")
      } else {
        showToast("Failed to update notification settings")
      }
    }
  }

  fun loadBlockedUsers() {
    val user = userProfile.value ?: return
    viewModelScope.launch {
      val users = authRepo.fetchUsersByIds(user.blockedUsers)
      _blockedUsersList.value = users
    }
  }

  fun blockUser(targetUserId: String) {
    val user = userProfile.value ?: return
    viewModelScope.launch {
      val res = authRepo.blockUser(user.uid, targetUserId)
      if (res.isSuccess) {
        showToast("User blocked")
        loadBlockedUsers()
      } else {
        showToast("Failed to block user")
      }
    }
  }

  fun unblockUser(targetUserId: String) {
    val user = userProfile.value ?: return
    viewModelScope.launch {
      val res = authRepo.unblockUser(user.uid, targetUserId)
      if (res.isSuccess) {
        showToast("User unblocked")
        loadBlockedUsers()
      } else {
        showToast("Failed to unblock user")
      }
    }
  }

  // Guest Account & Auth
  fun registerGuestAccount(
    username: String,
    email: String,
    password: String,
    confirmPass: String,
    displayName: String,
    onComplete: (Boolean, String?) -> Unit
  ) {
    if (password != confirmPass) {
      val err = "Passwords do not match"
      _authError.value = err
      onComplete(false, err)
      return
    }
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val res = authRepo.registerGuestWithEmailPassword(username, email, password, displayName)
      _isAuthLoading.value = false
      if (res.isSuccess) {
        showToast("Verification email sent. Please check your inbox and verify your email address.")
        onComplete(true, null)
      } else {
        val err = res.exceptionOrNull()?.message ?: "Registration failed"
        _authError.value = err
        onComplete(false, err)
      }
    }
  }

  fun signInWithUsernameOrEmail(
    identifier: String,
    password: String,
    onComplete: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val res = authRepo.signInWithUsernameOrEmail(identifier, password)
      _isAuthLoading.value = false
      if (res.isSuccess) {
        onComplete(true, null)
      } else {
        val err = res.exceptionOrNull()?.message ?: "Login failed"
        _authError.value = err
        onComplete(false, err)
      }
    }
  }

  fun resendVerificationEmail() {
    viewModelScope.launch {
      val res = authRepo.sendEmailVerification()
      if (res.isSuccess) {
        showToast("Verification email sent! Check your inbox.")
      } else {
        showToast("Failed to send: ${res.exceptionOrNull()?.message}")
      }
    }
  }

  fun sendOtp(email: String) {
    viewModelScope.launch {
      val res = authRepo.sendOtp(email)
      if (res.isSuccess) {
        showToast("OTP sent to $email")
      } else {
        showToast("Failed to send OTP: ${res.exceptionOrNull()?.message}")
      }
    }
  }

  fun verifyOtp(email: String, otp: String, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      val res = authRepo.verifyOtp(email, otp)
      if (res.isSuccess) {
        showToast("OTP Verified! ✨")
        onResult(true)
      } else {
        showToast(res.exceptionOrNull()?.message ?: "Verification failed")
        onResult(false)
      }
    }
  }

  fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
    val trimmed = email.trim()
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
      onResult(false, "Please enter a valid email address")
      return
    }
    viewModelScope.launch {
      _isAuthLoading.value = true
      val res = authRepo.sendPasswordResetEmail(trimmed)
      _isAuthLoading.value = false
      if (res.isSuccess) {
        showToast("Password reset email sent to $trimmed")
        onResult(true, null)
      } else {
        val err = res.exceptionOrNull()?.message ?: "Failed to send reset email"
        onResult(false, err)
      }
    }
  }

  fun updatePassword(newPass: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      _isAuthLoading.value = true
      val res = authRepo.updatePassword(newPass)
      _isAuthLoading.value = false
      if (res.isSuccess) {
        onResult(true, null)
      } else {
        val err = res.exceptionOrNull()?.message ?: "Failed to update password"
        onResult(false, err)
      }
    }
  }

  fun sendSignInLinkToEmail(email: String, onResult: (Boolean, String?) -> Unit) {
    val trimmed = email.trim()
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
      val err = "Please enter a valid email address (e.g. name@gmail.com)"
      _authError.value = err
      onResult(false, err)
      return
    }
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val res = authRepo.sendSignInLinkToEmail(trimmed)
      _isAuthLoading.value = false
      if (res.isSuccess) {
        showToast("Sign-in link sent to $trimmed! Check your inbox.")
        onResult(true, null)
      } else {
        val err = res.exceptionOrNull()?.message ?: "Failed to send sign-in link"
        _authError.value = err
        onResult(false, err)
      }
    }
  }

  fun signInWithEmailLink(email: String, emailLink: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
      _isAuthLoading.value = true
      _authError.value = null
      val res = authRepo.signInWithEmailLink(email.trim(), emailLink)
      _isAuthLoading.value = false
      if (res.isSuccess) {
        showToast("Signed in successfully! Welcome to Pinggo.")
        onResult(true, null)
      } else {
        val err = res.exceptionOrNull()?.message ?: "Failed to sign in with link"
        _authError.value = err
        onResult(false, err)
      }
    }
  }

  // Ringtone Settings
  fun saveCallRingtone(uri: String, title: String) {
    val me = userProfile.value
    com.example.util.RingtoneHelper.saveCallRingtone(getApplication(), uri, title)
    if (me != null) {
      val updated = me.copy(callRingtoneUri = uri, callRingtoneTitle = title)
      _guestProfile.value = updated
      viewModelScope.launch {
        authRepo.updateRingtoneSettings(me.uid, callRingtoneUri = uri, callRingtoneTitle = title)
      }
    }
    showToast("Call ringtone updated! 🔔")
  }

  fun saveNotificationRingtone(uri: String, title: String) {
    val me = userProfile.value
    com.example.util.RingtoneHelper.saveNotificationRingtone(getApplication(), uri, title)
    if (me != null) {
      val updated = me.copy(notificationRingtoneUri = uri, notificationRingtoneTitle = title)
      _guestProfile.value = updated
      viewModelScope.launch {
        authRepo.updateRingtoneSettings(me.uid, notificationRingtoneUri = uri, notificationRingtoneTitle = title)
      }
    }
    showToast("Notification sound updated! 💬")
  }

  // Calling
  private fun listenToIncomingCalls(userId: String) {
    incomingCallJob?.cancel()
    incomingCallJob = viewModelScope.launch {
      chatRepo.getIncomingCallsFlow(userId).collect { call ->
        if (call != null && _activeCall.value == null) {
          _activeCall.value = call
          if (call.receiverId == userId && call.status == "ringing") {
            val user = userProfile.value
            val ringtoneUri = user?.callRingtoneUri ?: ""
            com.example.util.RingtoneHelper.startCallRingtone(getApplication(), ringtoneUri)
          }
          startCallTimer()
        }
      }
    }
  }

  private fun listenToCallHistory(userId: String) {
    callHistoryJob?.cancel()
    callHistoryJob = viewModelScope.launch {
      chatRepo.getCallHistoryFlow(userId).collect { list ->
        _callHistory.value = list
      }
    }
  }

  fun startCall(targetUser: User, type: String) {
    val me = userProfile.value ?: return
    viewModelScope.launch {
      val res = chatRepo.initiateCall(me, targetUser, type)
      res.onSuccess { call ->
        _activeCall.value = call
        startCallTimer()
      }.onFailure {
        showToast("Could not initiate call")
      }
    }
  }

  fun acceptCall() {
    val call = _activeCall.value ?: return
    com.example.util.RingtoneHelper.stopCallRingtone()
    viewModelScope.launch {
      chatRepo.updateCallStatus(call.id, "accepted")
      _activeCall.value = call.copy(status = "accepted")
    }
  }

  fun endCall() {
    com.example.util.RingtoneHelper.stopCallRingtone()
    val call = _activeCall.value
    val duration = _callDurationSec.value
    if (call != null) {
      viewModelScope.launch {
        val finalStatus = if (call.status == "accepted") "completed" else "missed"
        chatRepo.updateCallStatus(call.id, finalStatus, duration)
      }
    }
    callTimerJob?.cancel()
    _activeCall.value = null
    _callDurationSec.value = 0
  }

  private fun startCallTimer() {
    callTimerJob?.cancel()
    _callDurationSec.value = 0
    callTimerJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        _callDurationSec.value += 1
      }
    }
  }

  fun toggleMute() {
    _isMuted.value = !_isMuted.value
  }

  fun toggleSpeaker() {
    _isSpeakerOn.value = !_isSpeakerOn.value
  }

  fun toggleVideo() {
    _isVideoOn.value = !_isVideoOn.value
  }

  override fun onCleared() {
    super.onCleared()
    voiceHelper.release()
    conversationsJob?.cancel()
    messagesJob?.cancel()
    typingJob?.cancel()
    incomingCallJob?.cancel()
    callTimerJob?.cancel()
  }
}
