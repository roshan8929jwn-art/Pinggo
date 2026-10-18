package com.example.model

/**
 * Score-based levels and tier thresholds for Pinggo Chat Score.
 * Authoritative levels are derived directly from the user's accumulated score.
 */
enum class ChatLevel(
  val level: Int,
  val title: String,
  val badge: String,
  val minScore: Long,
  val maxScore: Long? // null for top tier
) {
  STARTER(1, "Starter", "🐣", 0L, 99L),
  CHATTER(2, "Chatter", "💬", 100L, 499L),
  SOCIAL_STAR(3, "Social Star", "⭐", 500L, 1499L),
  SUPER_CHATTER(4, "Super Chatter", "⚡", 1500L, 4999L),
  PINGGO_LEGEND(5, "Pinggo Legend", "👑", 5000L, null);

  companion object {
    fun fromScore(score: Long): ChatLevel {
      val s = if (score < 0L) 0L else score
      return entries.lastOrNull { s >= it.minScore } ?: STARTER
    }

    fun getNextLevel(currentLevel: ChatLevel): ChatLevel? {
      val nextIndex = currentLevel.ordinal + 1
      return if (nextIndex < entries.size) entries[nextIndex] else null
    }

    fun getProgressToNextLevel(score: Long): Float {
      val current = fromScore(score)
      val next = getNextLevel(current) ?: return 1.0f
      val range = (next.minScore - current.minScore).toFloat()
      if (range <= 0f) return 1.0f
      val currentProgress = (score - current.minScore).toFloat()
      return (currentProgress / range).coerceIn(0.0f, 1.0f)
    }

    fun getPointsNeeded(score: Long): Long {
      val current = fromScore(score)
      val next = getNextLevel(current) ?: return 0L
      return (next.minScore - score).coerceAtLeast(0L)
    }
  }
}

/**
 * Result of authoritative score computation for an action.
 */
data class ScoreAwardResult(
  val success: Boolean,
  val pointsAwarded: Int = 0,
  val totalScore: Long = 0L,
  val streakBonus: Int = 0,
  val currentStreak: Int = 0,
  val longestStreak: Int = 0,
  val previousLevel: ChatLevel = ChatLevel.STARTER,
  val currentLevel: ChatLevel = ChatLevel.STARTER,
  val isLevelUp: Boolean = false,
  val isStreakMilestone: Boolean = false,
  val reason: String = ""
)

/**
 * UI celebration / animation trigger event.
 */
data class ScoreEvent(
  val pointsDelta: Int,
  val streakBonus: Int,
  val currentStreak: Int,
  val isStreakMilestone: Boolean,
  val level: ChatLevel,
  val isLevelUp: Boolean,
  val messageText: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
