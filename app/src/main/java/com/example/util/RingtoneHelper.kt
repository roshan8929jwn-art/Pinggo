package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

data class RingtoneItem(
  val title: String,
  val uriString: String,
  val isDefault: Boolean = false
)

object RingtoneHelper {
  private const val TAG = "RingtoneHelper"
  private const val PREFS_NAME = "pinggo_ringtone_prefs"
  private const val KEY_CALL_RINGTONE_URI = "call_ringtone_uri"
  private const val KEY_CALL_RINGTONE_TITLE = "call_ringtone_title"
  private const val KEY_NOTIF_RINGTONE_URI = "notif_ringtone_uri"
  private const val KEY_NOTIF_RINGTONE_TITLE = "notif_ringtone_title"

  const val NOTIFICATION_CHANNEL_ID = "pinggo_messages_channel"
  const val NOTIFICATION_CHANNEL_NAME = "Pinggo Messages"

  private var previewRingtone: Ringtone? = null
  private var callMediaPlayer: MediaPlayer? = null
  private var callRingtone: Ringtone? = null

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun saveCallRingtone(context: Context, uriString: String, title: String) {
    getPrefs(context).edit()
      .putString(KEY_CALL_RINGTONE_URI, uriString)
      .putString(KEY_CALL_RINGTONE_TITLE, title)
      .apply()
  }

  fun getCallRingtone(context: Context): Pair<String, String> {
    val prefs = getPrefs(context)
    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)?.toString() ?: ""
    val uri = prefs.getString(KEY_CALL_RINGTONE_URI, defaultUri) ?: defaultUri
    val title = prefs.getString(KEY_CALL_RINGTONE_TITLE, "Default Ringtone") ?: "Default Ringtone"
    return Pair(uri, title)
  }

  fun saveNotificationRingtone(context: Context, uriString: String, title: String) {
    getPrefs(context).edit()
      .putString(KEY_NOTIF_RINGTONE_URI, uriString)
      .putString(KEY_NOTIF_RINGTONE_TITLE, title)
      .apply()
    updateNotificationChannel(context, uriString)
  }

  fun getNotificationRingtone(context: Context): Pair<String, String> {
    val prefs = getPrefs(context)
    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)?.toString() ?: ""
    val uri = prefs.getString(KEY_NOTIF_RINGTONE_URI, defaultUri) ?: defaultUri
    val title = prefs.getString(KEY_NOTIF_RINGTONE_TITLE, "Default Notification") ?: "Default Notification"
    return Pair(uri, title)
  }

  /**
   * Retrieves all available Android system ringtones or notification sounds
   */
  fun getAvailableRingtones(context: Context, type: Int): List<RingtoneItem> {
    val list = mutableListOf<RingtoneItem>()
    val defaultUri = RingtoneManager.getDefaultUri(type)?.toString() ?: ""
    val defaultLabel = if (type == RingtoneManager.TYPE_RINGTONE) "Default System Ringtone" else "Default Notification Sound"

    list.add(RingtoneItem(title = defaultLabel, uriString = defaultUri, isDefault = true))

    try {
      val manager = RingtoneManager(context)
      manager.setType(type)
      val cursor = manager.cursor
      if (cursor != null && cursor.moveToFirst()) {
        do {
          val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX) ?: "Ringtone"
          val uri = manager.getRingtoneUri(cursor.position)?.toString() ?: ""
          if (uri.isNotEmpty() && uri != defaultUri) {
            list.add(RingtoneItem(title = title, uriString = uri, isDefault = false))
          }
        } while (cursor.moveToNext())
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error querying system ringtones: ${e.message}")
    }

    return list
  }

  /**
   * Play audio preview for user testing
   */
  fun playPreview(context: Context, uriString: String) {
    stopPreview()
    stopCallRingtone()
    try {
      val uri = if (uriString.isNotBlank()) Uri.parse(uriString) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      val ringtone = RingtoneManager.getRingtone(context, uri)
      if (ringtone != null) {
        ringtone.audioAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
        ringtone.play()
        previewRingtone = ringtone
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to play preview: ${e.message}")
    }
  }

  fun stopPreview() {
    try {
      previewRingtone?.stop()
      previewRingtone = null
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping preview: ${e.message}")
    }
  }

  /**
   * Play incoming call ringtone continuously until answered or ended
   */
  fun startCallRingtone(context: Context, configuredUri: String = "") {
    stopCallRingtone()
    stopPreview()
    try {
      val (savedUri, _) = getCallRingtone(context)
      val finalUriString = configuredUri.ifBlank { savedUri }
      val uri = if (finalUriString.isNotBlank()) {
        Uri.parse(finalUriString)
      } else {
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
          ?: Settings.System.DEFAULT_RINGTONE_URI
      }

      val ringtone = RingtoneManager.getRingtone(context, uri)
      if (ringtone != null) {
        ringtone.audioAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          ringtone.isLooping = true
        }
        ringtone.play()
        callRingtone = ringtone
      } else {
        // Fallback to MediaPlayer
        val player = MediaPlayer.create(context, uri)
        if (player != null) {
          player.isLooping = true
          player.setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          player.start()
          callMediaPlayer = player
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error playing call ringtone: ${e.message}")
    }
  }

  fun stopCallRingtone() {
    try {
      callRingtone?.stop()
      callRingtone = null
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping call ringtone: ${e.message}")
    }
    try {
      callMediaPlayer?.stop()
      callMediaPlayer?.release()
      callMediaPlayer = null
    } catch (e: Exception) {
      Log.w(TAG, "Error releasing call player: ${e.message}")
    }
  }

  /**
   * Play message notification sound
   */
  fun playNotificationSound(context: Context, configuredUri: String = "") {
    try {
      val (savedUri, _) = getNotificationRingtone(context)
      val finalUriString = configuredUri.ifBlank { savedUri }
      val uri = if (finalUriString.isNotBlank()) {
        Uri.parse(finalUriString)
      } else {
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
          ?: Settings.System.DEFAULT_NOTIFICATION_URI
      }

      val ringtone = RingtoneManager.getRingtone(context, uri)
      if (ringtone != null) {
        ringtone.audioAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
        ringtone.play()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error playing notification sound: ${e.message}")
    }
  }

  /**
   * Update Android Notification Channel sound config on Android 8.0+
   */
  fun updateNotificationChannel(context: Context, soundUriString: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
          ?: return

        val soundUri = if (soundUriString.isNotBlank()) {
          Uri.parse(soundUriString)
        } else {
          RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val audioAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()

        val channel = NotificationChannel(
          NOTIFICATION_CHANNEL_ID,
          NOTIFICATION_CHANNEL_NAME,
          NotificationManager.IMPORTANCE_HIGH
        ).apply {
          description = "Notifications for incoming messages and chat alerts on Pinggo"
          enableVibration(true)
          setSound(soundUri, audioAttributes)
        }

        notificationManager.createNotificationChannel(channel)
      } catch (e: Exception) {
        Log.w(TAG, "Error configuring notification channel: ${e.message}")
      }
    }
  }

  /**
   * Open system Notification Channel settings on Android 8.0+
   */
  fun openChannelSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
          putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
          putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (e: Exception) {
        // Fallback to app notification settings
        try {
          val appIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(appIntent)
        } catch (ex: Exception) {
          Log.w(TAG, "Could not open notification settings", ex)
        }
      }
    }
  }
}
