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
      username = "rohansharma",
      bio = "Living the best version of myself ✨"
    )
    val map = user.toMap()
    val restored = com.example.model.User.fromMap(map)
    assertEquals("u1", restored.uid)
    assertEquals("rohansharma", restored.username)
    assertEquals("Rohan Sharma", restored.displayName)
  }
}
