package com.example.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class VoiceRecorderHelper(private val context: Context) {
  private var mediaRecorder: MediaRecorder? = null
  private var mediaPlayer: MediaPlayer? = null
  private var currentRecordingFile: File? = null

  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private val _recordingDurationSec = MutableStateFlow(0)
  val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _playingMessageId = MutableStateFlow<String?>(null)
  val playingMessageId: StateFlow<String?> = _playingMessageId.asStateFlow()

  private val _playProgress = MutableStateFlow(0f)
  val playProgress: StateFlow<Float> = _playProgress.asStateFlow()

  private val handler = Handler(Looper.getMainLooper())
  private var durationRunnable: Runnable? = null
  private var progressRunnable: Runnable? = null

  fun startRecording(): File? {
    stopPlaying()
    try {
      val audioDir = File(context.cacheDir, "voice_notes").apply { mkdirs() }
      val file = File(audioDir, "voice_${System.currentTimeMillis()}.m4a")
      currentRecordingFile = file

      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }

      recorder.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioEncodingBitRate(128000)
        setAudioSamplingRate(44100)
        setOutputFile(file.absolutePath)
        prepare()
        start()
      }
      mediaRecorder = recorder
      _isRecording.value = true
      _recordingDurationSec.value = 0

      durationRunnable = object : Runnable {
        override fun run() {
          if (_isRecording.value) {
            _recordingDurationSec.value += 1
            handler.postDelayed(this, 1000)
          }
        }
      }
      handler.postDelayed(durationRunnable!!, 1000)
      return file
    } catch (e: Exception) {
      e.printStackTrace()
      _isRecording.value = false
      return null
    }
  }

  fun stopRecording(): File? {
    return try {
      durationRunnable?.let { handler.removeCallbacks(it) }
      mediaRecorder?.apply {
        stop()
        release()
      }
      mediaRecorder = null
      _isRecording.value = false
      currentRecordingFile
    } catch (e: Exception) {
      e.printStackTrace()
      mediaRecorder = null
      _isRecording.value = false
      null
    }
  }

  fun cancelRecording() {
    try {
      durationRunnable?.let { handler.removeCallbacks(it) }
      mediaRecorder?.apply {
        stop()
        release()
      }
    } catch (e: Exception) {
      // ignore
    } finally {
      mediaRecorder = null
      _isRecording.value = false
      _recordingDurationSec.value = 0
      currentRecordingFile?.delete()
      currentRecordingFile = null
    }
  }

  fun playAudio(urlOrPath: String, messageId: String) {
    stopPlaying()
    try {
      val player = MediaPlayer()
      player.setDataSource(urlOrPath)
      player.prepare()
      player.start()
      mediaPlayer = player
      _isPlaying.value = true
      _playingMessageId.value = messageId

      progressRunnable = object : Runnable {
        override fun run() {
          mediaPlayer?.let { p ->
            if (p.isPlaying && p.duration > 0) {
              _playProgress.value = p.currentPosition.toFloat() / p.duration.toFloat()
              handler.postDelayed(this, 100)
            }
          }
        }
      }
      handler.post(progressRunnable!!)

      player.setOnCompletionListener {
        stopPlaying()
      }
    } catch (e: Exception) {
      e.printStackTrace()
      stopPlaying()
    }
  }

  fun stopPlaying() {
    progressRunnable?.let { handler.removeCallbacks(it) }
    try {
      mediaPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
    } catch (e: Exception) {
      // ignore
    } finally {
      mediaPlayer = null
      _isPlaying.value = false
      _playingMessageId.value = null
      _playProgress.value = 0f
    }
  }

  fun release() {
    cancelRecording()
    stopPlaying()
  }
}
