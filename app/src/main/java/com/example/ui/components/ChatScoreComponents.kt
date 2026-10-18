package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ChatLevel
import com.example.model.ScoreEvent
import com.example.model.User
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Compact Chat Score badge for conversation list items and user headings.
 */
@Composable
fun ChatScoreBadge(
  score: Long,
  streakCount: Int = 0,
  modifier: Modifier = Modifier
) {
  val level = ChatLevel.fromScore(score)
  Surface(
    modifier = modifier.testTag("chat_score_badge"),
    color = PinggoPinkLight.copy(alpha = 0.35f),
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(0.5.dp, PinggoPinkPrimary.copy(alpha = 0.3f))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Text(
        text = level.badge,
        fontSize = 11.sp
      )
      Text(
        text = "$score",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = PinggoPinkPrimary
      )
      if (streakCount > 0) {
        Text(
          text = "🔥$streakCount",
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFFF5722)
        )
      }
    }
  }
}

/**
 * Detailed Chat Score & Daily Streaks summary card for ProfileTab.
 */
@Composable
fun ChatScoreSummaryCard(
  user: User,
  onViewLeaderboard: () -> Unit,
  modifier: Modifier = Modifier
) {
  val score = user.chatScore
  val level = ChatLevel.fromScore(score)
  val nextLevel = ChatLevel.getNextLevel(level)
  val progress = ChatLevel.getProgressToNextLevel(score)
  val pointsNeeded = ChatLevel.getPointsNeeded(score)

  GlassCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("chat_score_summary_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header row with Level Title and Trophy button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = PinggoPinkPrimary.copy(alpha = 0.12f),
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = level.badge, fontSize = 18.sp)
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Chat Score & Level",
              style = MaterialTheme.typography.labelMedium,
              color = LightSecondaryText,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Level ${level.level}: ${level.title}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = LightPrimaryText
            )
          }
        }

        // Leaderboard shortcut pill button
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = PinggoPinkLight.copy(alpha = 0.4f),
          border = BorderStroke(1.dp, PinggoPinkPrimary.copy(alpha = 0.3f)),
          modifier = Modifier
            .clickable(onClick = onViewLeaderboard)
            .testTag("open_leaderboard_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = "Leaderboard",
              tint = PinggoPinkPrimary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Rankings",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = PinggoPinkPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main stats row: Total Score and Daily Streak
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Score Box
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = SolidSurfaceWhite,
          border = BorderStroke(1.dp, SolidBorderColor),
          shadowElevation = 1.dp
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "TOTAL SCORE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = LightSecondaryText,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "%,d".format(score),
              fontSize = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              color = PinggoPinkPrimary
            )
            Text(
              text = "pts earned",
              fontSize = 11.sp,
              color = LightSecondaryText
            )
          }
        }

        // Streak Box
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = SolidSurfaceWhite,
          border = BorderStroke(1.dp, SolidBorderColor),
          shadowElevation = 1.dp
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "DAILY STREAK",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = LightSecondaryText,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "🔥 ${user.streakCount}",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF5722)
              )
              Text(
                text = " d",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LightSecondaryText
              )
            }
            Text(
              text = if (user.longestStreak > 0) "Best: ${user.longestStreak} days" else "Active today",
              fontSize = 11.sp,
              color = LightSecondaryText
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Level Progress Bar
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (nextLevel != null) "Progress to Level ${nextLevel.level}: ${nextLevel.title} ${nextLevel.badge}" else "Max Level Achieved 👑",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightPrimaryText
          )
          if (nextLevel != null) {
            Text(
              text = "$pointsNeeded pts needed",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = PinggoPinkPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress track
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(PinggoOffWhite)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction = progress)
              .fillMaxHeight()
              .clip(RoundedCornerShape(4.dp))
              .background(
                Brush.horizontalGradient(
                  colors = listOf(PinggoPinkLight, PinggoPinkPrimary)
                )
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Scoring rules hint
      Surface(
        color = PinggoOffWhite,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = LightSecondaryText,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "+1 pt per sent message • +5 pts daily streak milestone • 300 pts daily limit",
            fontSize = 10.sp,
            color = LightSecondaryText
          )
        }
      }
    }
  }
}

/**
 * Public score display card for previewing other users in search and chat previews.
 */
@Composable
fun PublicScoreCard(
  user: User,
  modifier: Modifier = Modifier
) {
  val score = user.chatScore
  val level = ChatLevel.fromScore(score)
  val progress = ChatLevel.getProgressToNextLevel(score)
  val nextLevel = ChatLevel.getNextLevel(level)

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = SolidSurfaceWhite,
    border = BorderStroke(1.dp, SolidBorderColor),
    shadowElevation = 1.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = level.badge, fontSize = 20.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Level ${level.level}: ${level.title}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = LightPrimaryText
            )
            Text(
              text = "Public Chat Score",
              fontSize = 11.sp,
              color = LightSecondaryText
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = PinggoPinkLight.copy(alpha = 0.35f),
          border = BorderStroke(0.5.dp, PinggoPinkPrimary.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "%,d".format(score),
              fontSize = 14.sp,
              fontWeight = FontWeight.ExtraBold,
              color = PinggoPinkPrimary
            )
            Text(
              text = " PTS",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = PinggoPinkPrimary.copy(alpha = 0.8f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (user.streakCount > 0) "🔥 ${user.streakCount} Day Streak (Best: ${user.longestStreak})" else "Active Chat Member",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (user.streakCount > 0) Color(0xFFFF5722) else LightSecondaryText
        )
        if (nextLevel != null) {
          Text(
            text = "${(progress * 100).toInt()}% to Level ${nextLevel.level}",
            fontSize = 11.sp,
            color = LightSecondaryText
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(PinggoOffWhite)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(fraction = progress)
            .fillMaxHeight()
            .clip(RoundedCornerShape(3.dp))
            .background(PinggoPinkPrimary)
        )
      }
    }
  }
}

/**
 * Animated floating score award pill celebration.
 * Shows +1 PTS, streak milestone, or level-up without blocking the screen.
 */
@Composable
fun ScoreCelebrationToast(
  event: ScoreEvent?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isVisible by remember(event) { mutableStateOf(event != null) }

  LaunchedEffect(event) {
    if (event != null) {
      isVisible = true
      delay(2400)
      isVisible = false
      delay(300)
      onDismiss()
    }
  }

  AnimatedVisibility(
    visible = isVisible && event != null,
    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(200)),
    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(250)),
    modifier = modifier
  ) {
    if (event != null) {
      val isLevelUp = event.isLevelUp
      val isMilestone = event.isStreakMilestone

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isLevelUp) PinggoPinkPrimary else SolidSurfaceWhite,
        border = BorderStroke(1.dp, if (isLevelUp) PinggoPinkPrimary else PinggoPinkLight),
        shadowElevation = 6.dp,
        modifier = Modifier
          .padding(horizontal = 24.dp, vertical = 12.dp)
          .testTag("score_celebration_toast")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = if (isLevelUp) "🎉" else if (isMilestone) "🔥" else "✨",
            fontSize = 18.sp
          )
          Column {
            Text(
              text = if (isLevelUp) "LEVEL UP!" else if (isMilestone) "DAILY STREAK EXTENDED!" else "MESSAGE PERSISTED",
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isLevelUp) Color.White.copy(alpha = 0.9f) else LightSecondaryText,
              letterSpacing = 0.5.sp
            )
            Text(
              text = event.messageText.ifEmpty { "+${event.pointsDelta} PTS" },
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (isLevelUp) Color.White else LightPrimaryText
            )
          }
          if (event.pointsDelta > 0) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isLevelUp) Color.White.copy(alpha = 0.25f) else PinggoPinkLight.copy(alpha = 0.4f)
            ) {
              Text(
                text = "+${event.pointsDelta}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isLevelUp) Color.White else PinggoPinkPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Public Leaderboard Dialog showing ranked users, scores, levels and streaks.
 */
@Composable
fun LeaderboardDialog(
  leaderboard: List<User>,
  currentUserId: String,
  isLoading: Boolean,
  onRefresh: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .clip(RoundedCornerShape(26.dp))
        .testTag("leaderboard_dialog"),
      color = SolidSurfaceWhite,
      border = BorderStroke(1.dp, SolidBorderColor),
      shadowElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = PinggoPinkLight.copy(alpha = 0.4f),
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = "🏆", fontSize = 20.sp)
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Chat Score Rankings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightPrimaryText
              )
              Text(
                text = "Public Community Leaderboard",
                fontSize = 11.sp,
                color = LightSecondaryText
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onRefresh,
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Leaderboard",
                tint = PinggoPinkPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = LightSecondaryText,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = SolidBorderColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              color = PinggoPinkPrimary,
              modifier = Modifier.size(36.dp)
            )
          }
        } else if (leaderboard.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "💬", fontSize = 42.sp)
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "No scores recorded yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightPrimaryText
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Send messages to rank on the public leaderboard!",
                fontSize = 12.sp,
                color = LightSecondaryText,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            itemsIndexed(leaderboard, key = { _, user -> user.uid }) { index, user ->
              val rank = index + 1
              val isMe = user.uid == currentUserId
              val level = ChatLevel.fromScore(user.chatScore)

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isMe) PinggoPinkLight.copy(alpha = 0.25f) else SolidSurfaceWhite,
                border = BorderStroke(
                  1.dp,
                  if (isMe) PinggoPinkPrimary.copy(alpha = 0.5f) else SolidBorderColor
                ),
                shadowElevation = if (isMe) 2.dp else 0.5.dp,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Rank badge / medal
                  Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    when (rank) {
                      1 -> Text(text = "🥇", fontSize = 20.sp)
                      2 -> Text(text = "🥈", fontSize = 18.sp)
                      3 -> Text(text = "🥉", fontSize = 18.sp)
                      else -> Text(
                        text = "#$rank",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightSecondaryText
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(8.dp))

                  GlassAvatar(
                    photoUrl = user.photoURL,
                    name = user.displayName.ifEmpty { user.username },
                    size = 40.dp
                  )

                  Spacer(modifier = Modifier.width(10.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = user.displayName.ifEmpty { user.username },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightPrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      if (isMe) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                          color = PinggoPinkPrimary,
                          shape = RoundedCornerShape(8.dp)
                        ) {
                          Text(
                            text = "YOU",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                          )
                        }
                      }
                    }
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Text(
                        text = "${level.badge} ${level.title}",
                        fontSize = 11.sp,
                        color = LightSecondaryText
                      )
                      if (user.streakCount > 0) {
                        Text(
                          text = "• 🔥${user.streakCount}d",
                          fontSize = 11.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = Color(0xFFFF5722)
                        )
                      }
                    }
                  }

                  // Score pill
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PinggoPinkLight.copy(alpha = 0.4f),
                    border = BorderStroke(0.5.dp, PinggoPinkPrimary.copy(alpha = 0.3f))
                  ) {
                    Text(
                      text = "%,d PTS".format(user.chatScore),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = PinggoPinkPrimary,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
