package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pinggo", appName)
  }

  @Test
  fun `user model serialization`() {
    val user = com.example.model.User(
      uid = "u1",
      displayName = "Rohan Sharma",
      username = "@roshan",
      bio = "Living the best version of myself ✨"
    )
    val map = user.toMap()
    val restored = com.example.model.User.fromMap(map)
    assertEquals("u1", restored.uid)
    assertEquals("@roshan", restored.username)
    assertEquals("Rohan Sharma", restored.displayName)
  }

  @Test
  fun `username validation supports @roshan, @roshan123, and @pinggo_user`() {
    val validUsernames = listOf("@roshan", "@roshan123", "@pinggo_user", "roshan", "roshan_123")
    val regex = Regex("^@?[a-z0-9_]+$")

    for (un in validUsernames) {
      val clean = un.trim().lowercase()
      val bare = clean.removePrefix("@")
      org.junit.Assert.assertTrue("Expected $un to match format", regex.matches(clean))
      org.junit.Assert.assertTrue("Expected bare $bare length >= 3", bare.length >= 3)
      org.junit.Assert.assertTrue("Expected bare $bare length <= 20", bare.length <= 20)
    }

    val invalidUsernames = listOf("@", "@a", "@ab!", "roshan@user", "ro shan", "@roshan#")
    for (un in invalidUsernames) {
      val clean = un.trim().lowercase()
      val bare = clean.removePrefix("@")
      val isValid = regex.matches(clean) && bare.length in 3..20
      org.junit.Assert.assertFalse("Expected $un to be invalid", isValid)
    }
  }

  @Test
  fun `user handle formats with or without @ correctly`() {
    val userWithAt = com.example.model.User(username = "@roshan")
    val userWithoutAt = com.example.model.User(username = "roshan")
    val userEmpty = com.example.model.User(username = "")

    assertEquals("@roshan", com.example.model.handle(userWithAt))
    assertEquals("@roshan", com.example.model.handle(userWithoutAt))
    assertEquals("@username", com.example.model.handle(userEmpty))
  }

  @Test
  fun `username input filtering allows @, letters, numbers, and underscore`() {
    val rawTyped = "@Roshan_123!"
    val filtered = rawTyped.filter { it.isLetterOrDigit() || it == '_' || it == '@' }.take(21).lowercase()
    assertEquals("@roshan_123", filtered)
  }
}

