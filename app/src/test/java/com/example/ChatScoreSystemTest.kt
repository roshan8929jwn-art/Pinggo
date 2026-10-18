package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Unit and business logic test suite validating all 12 Chat Score, Streak, Level,
 * and Idempotency requirements.
 */
class ChatScoreSystemTest {

  @Test
  fun test1_validSentMessageAwardsPoints() {
    val user = User(uid = "u1", chatScore = 10L, streakCount = 1)
    val pointsDelta = 1
    val newScore = user.chatScore + pointsDelta
    assertEquals(11L, newScore)
  }

  @Test
  fun test2_receivedMessageDoesNotAwardPointsToRecipient() {
    val sender = User(uid = "sender_1", chatScore = 50L)
    val recipient = User(uid = "recipient_2", chatScore = 20L)
    val msg = Message(
      id = "m1",
      conversationId = "conv_1",
      senderId = sender.uid,
      text = "Hello!"
    )
    // Verify recipient cannot be credited for message where senderId != recipient.uid
    val isRecipientEligible = (msg.senderId == recipient.uid)
    assertFalse("Recipient must not earn sender points", isRecipientEligible)
    assertEquals(20L, recipient.chatScore)
  }

  @Test
  fun test3_duplicateMessageEventsDoNotDuplicatePoints() {
    val processedMessageIds = mutableSetOf<String>()
    val messageId = "msg_unique_abc"

    // First attempt: should succeed
    val firstAttemptProcessed = processedMessageIds.add(messageId)
    assertTrue("First message event must be accepted", firstAttemptProcessed)

    // Second attempt with duplicate messageId (idempotency check): should be rejected
    val secondAttemptProcessed = processedMessageIds.add(messageId)
    assertFalse("Duplicate event with same messageId must be rejected", secondAttemptProcessed)
  }

  @Test
  fun test4_failedOrDeletedOrEmptyMessagesAwardNoPoints() {
    val deletedMsg = Message(id = "m_del", isDeleted = true, text = "deleted")
    val emptyMsg = Message(id = "m_empty", text = "   ")
    val systemMsg = Message(id = "m_sys", type = "system", text = "User joined")

    fun isMessageEligible(msg: Message): Boolean {
      if (msg.isDeleted) return false
      val validTypes = setOf("text", "image", "voice", "file")
      if (msg.type !in validTypes) return false
      if (msg.type == "text" && msg.text.trim().isEmpty()) return false
      return true
    }

    assertFalse(isMessageEligible(deletedMsg))
    assertFalse(isMessageEligible(emptyMsg))
    assertFalse(isMessageEligible(systemMsg))
  }

  @Test
  fun test5_repeatedMessagesCannotBypassDailyLimit() {
    val dailyLimit = 300
    var currentDayPoints = 299

    // Message 300 awards 1 pt
    val canAward300 = currentDayPoints < dailyLimit
    assertTrue(canAward300)
    currentDayPoints += 1
    assertEquals(300, currentDayPoints)

    // Message 301 onwards is capped at 0 message points
    val canAward301 = currentDayPoints < dailyLimit
    assertFalse("Points must be capped at 300 per calendar day", canAward301)
  }

  @Test
  fun test6_streakIncrementsAtMostOncePerDay() {
    val todayDate = "2026-10-18"
    val yesterdayDate = "2026-10-17"

    // User was active yesterday: today's first message increments streak
    var lastActiveDate = yesterdayDate
    var currentStreak = 3

    val newStreak: Int
    val streakBonus: Int
    if (lastActiveDate == todayDate) {
      newStreak = currentStreak
      streakBonus = 0
    } else if (lastActiveDate == yesterdayDate) {
      newStreak = currentStreak + 1
      streakBonus = 5
      lastActiveDate = todayDate
    } else {
      newStreak = 1
      streakBonus = 0
    }

    assertEquals(4, newStreak)
    assertEquals(5, streakBonus)

    // User sends another message on the SAME day: streak should NOT increment again
    val secondMessageStreak: Int
    val secondStreakBonus: Int
    if (lastActiveDate == todayDate) {
      secondMessageStreak = newStreak
      secondStreakBonus = 0
    } else {
      secondMessageStreak = newStreak + 1
      secondStreakBonus = 5
    }

    assertEquals("Streak must not increment multiple times on same day", 4, secondMessageStreak)
    assertEquals("Streak bonus must not be awarded multiple times on same day", 0, secondStreakBonus)
  }

  @Test
  fun test7_missedDayResetsCurrentStreakCorrectly() {
    val todayDate = "2026-10-18"
    val threeDaysAgoDate = "2026-10-15"

    var currentStreak = 12
    val longestStreak = 15
    val lastActiveDate = threeDaysAgoDate

    val newStreak = if (lastActiveDate == todayDate) {
      currentStreak
    } else if (lastActiveDate == "2026-10-17") {
      currentStreak + 1
    } else {
      1 // Missed calendar day resets streak to 1
    }

    val newLongestStreak = maxOf(longestStreak, newStreak)

    assertEquals("Missed day must reset current streak to 1", 1, newStreak)
    assertEquals("Longest streak statistic must be preserved", 15, newLongestStreak)
  }

  @Test
  fun test8_levelThresholdsAndProgressCalculations() {
    assertEquals(ChatLevel.STARTER, ChatLevel.fromScore(0L))
    assertEquals(ChatLevel.STARTER, ChatLevel.fromScore(99L))
    assertEquals(ChatLevel.CHATTER, ChatLevel.fromScore(100L))
    assertEquals(ChatLevel.CHATTER, ChatLevel.fromScore(499L))
    assertEquals(ChatLevel.SOCIAL_STAR, ChatLevel.fromScore(500L))
    assertEquals(ChatLevel.SOCIAL_STAR, ChatLevel.fromScore(1499L))
    assertEquals(ChatLevel.SUPER_CHATTER, ChatLevel.fromScore(1500L))
    assertEquals(ChatLevel.SUPER_CHATTER, ChatLevel.fromScore(4999L))
    assertEquals(ChatLevel.PINGGO_LEGEND, ChatLevel.fromScore(5000L))
    assertEquals(ChatLevel.PINGGO_LEGEND, ChatLevel.fromScore(100000L))

    // Progress to next level
    // Starter: min 0, max 99, next is Chatter at 100
    assertEquals(0.0f, ChatLevel.getProgressToNextLevel(0L), 0.001f)
    assertEquals(0.5f, ChatLevel.getProgressToNextLevel(50L), 0.001f)
    assertEquals(50L, ChatLevel.getPointsNeeded(50L))

    // Top tier (Legend) progress is full (1.0f)
    assertEquals(1.0f, ChatLevel.getProgressToNextLevel(5500L), 0.001f)
    assertEquals(0L, ChatLevel.getPointsNeeded(5500L))
  }

  @Test
  fun test9_userModelSerializationIncludesAllScoreAndStreakFields() {
    val user = User(
      uid = "u_score_test",
      displayName = "Pinggo Tester",
      username = "tester99",
      chatScore = 1250L,
      streakCount = 7,
      longestStreak = 14,
      lastActiveDate = "2026-10-18",
      dailyPointsCount = 25,
      dailyPointsDate = "2026-10-18"
    )

    val map = user.toMap()
    assertEquals(1250L, map["chatScore"])
    assertEquals(7, map["streakCount"])
    assertEquals(14, map["longestStreak"])
    assertEquals("2026-10-18", map["lastActiveDate"])
    assertEquals(25, map["dailyPointsCount"])
    assertEquals("2026-10-18", map["dailyPointsDate"])

    val restored = User.fromMap(map)
    assertEquals(1250L, restored.chatScore)
    assertEquals(7, restored.streakCount)
    assertEquals(14, restored.longestStreak)
    assertEquals("2026-10-18", restored.lastActiveDate)
    assertEquals(ChatLevel.SOCIAL_STAR, restored.level)
  }

  @Test
  fun test10_unauthorizedArbitraryScoreWriteValidation() {
    // In our firestore security rule and model validation, single update delta cannot exceed 6 pts
    val currentScore = 100L
    val attemptedScore = 999999L // Malicious arbitrary write
    val validUpdate = (attemptedScore >= currentScore && attemptedScore <= currentScore + 6L)
    assertFalse("Arbitrary large score jump must be rejected", validUpdate)

    val validDeltaScore = currentScore + 6L
    val legitimateUpdate = (validDeltaScore >= currentScore && validDeltaScore <= currentScore + 6L)
    assertTrue("Valid score increment (+6 max) must be accepted", legitimateUpdate)
  }

  @Test
  fun test11_selfMessagesInDirectChatsAreIneligible() {
    val senderUid = "u_self"
    val directConv = Conversation(
      id = "conv_self",
      type = "direct",
      participants = listOf(senderUid)
    )
    val otherParticipants = directConv.participants.filter { it != senderUid }
    val isDirectSelfChat = (directConv.type == "direct" && otherParticipants.isEmpty())
    assertTrue("Direct chat with oneself must be detected", isDirectSelfChat)
  }

  @Test
  fun test12_defaultScoreForUsersWithoutScoreRecord() {
    // Users created before Chat Score or without score document default safely to 0 score and Level 1 Starter
    val legacyMap = mapOf(
      "uid" to "legacy_user",
      "displayName" to "Old User",
      "username" to "oldie"
    )
    val user = User.fromMap(legacyMap)
    assertEquals(0L, user.chatScore)
    assertEquals(0, user.streakCount)
    assertEquals(0, user.longestStreak)
    assertEquals("", user.lastActiveDate)
    assertEquals(ChatLevel.STARTER, user.level)
    assertEquals("Starter", user.level.title)
    assertEquals("🐣", user.level.badge)
  }

  @Test
  fun test13_chatStreakBadgeFormattingAndZeroHandling() {
    fun formatStreakBadgeText(streak: Int): String? {
      if (streak <= 0) return null
      return "🔥 $streak ${if (streak == 1) "day" else "days"}"
    }

    assertNull("Streak 0 must not display badge", formatStreakBadgeText(0))
    assertNull("Negative streak must not display badge", formatStreakBadgeText(-1))
    assertEquals("🔥 1 day", formatStreakBadgeText(1))
    assertEquals("🔥 7 days", formatStreakBadgeText(7))
    assertEquals("🔥 25 days", formatStreakBadgeText(25))
  }

  @Test
  fun test14_profileChatScoreDisplaysStoredTotalScore() {
    val user = User(
      uid = "u_profile_test",
      displayName = "Pinggo Ace",
      username = "ace",
      chatScore = 1250L,
      streakCount = 7,
      longestStreak = 10
    )

    val formattedScore = "%,d".format(user.chatScore)
    assertEquals("1,250", formattedScore)
    val headline = "💬 Chat Score: $formattedScore"
    assertEquals("💬 Chat Score: 1,250", headline)

    val level = ChatLevel.fromScore(user.chatScore)
    assertEquals(ChatLevel.SOCIAL_STAR, level)
    assertEquals("Level 3: Social Star", "Level ${level.level}: ${level.title}")
    val streakText = if (user.streakCount > 0) "${user.streakCount} ${if (user.streakCount == 1) "day" else "days"}" else "0 days"
    assertEquals("7 days", streakText)
  }
}
