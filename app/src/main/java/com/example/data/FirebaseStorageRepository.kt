package com.example.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseStorageRepository {
  private val storage: FirebaseStorage?
    get() = try {
      FirebaseStorage.getInstance()
    } catch (e: Exception) {
      null
    }

  suspend fun uploadVoiceNote(userId: String, audioFile: File): Result<String> {
    return try {
      val s = storage ?: return Result.failure(IllegalStateException("Firebase Storage is not initialized"))
      val ref = s.reference.child("voice_notes/$userId/${audioFile.name}")
      val uri = Uri.fromFile(audioFile)
      ref.putFile(uri).await()
      val downloadUrl = ref.downloadUrl.await().toString()
      Result.success(downloadUrl)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  suspend fun uploadImage(userId: String, imageUri: Uri): Result<String> {
    return try {
      val s = storage ?: return Result.failure(IllegalStateException("Firebase Storage is not initialized"))
      val filename = "img_${System.currentTimeMillis()}.jpg"
      val ref = s.reference.child("chat_images/$userId/$filename")
      ref.putFile(imageUri).await()
      val downloadUrl = ref.downloadUrl.await().toString()
      Result.success(downloadUrl)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  suspend fun uploadAvatar(userId: String, imageUri: Uri): Result<String> {
    return try {
      val s = storage ?: return Result.failure(IllegalStateException("Firebase Storage is not initialized"))
      val ref = s.reference.child("avatars/$userId.jpg")
      ref.putFile(imageUri).await()
      val downloadUrl = ref.downloadUrl.await().toString()
      Result.success(downloadUrl)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }
}
