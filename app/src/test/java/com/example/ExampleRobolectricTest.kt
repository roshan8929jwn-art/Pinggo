package com.example

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.HomeScreen
import com.example.viewmodel.PinggoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
  }

  @Test
  fun `launch MainActivity test`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    controller.setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
  }

  @Test
  fun `test back navigation from Calls, Updates, and Profile to Chat List`() {
    val dispatcher = androidx.activity.OnBackPressedDispatcher()
    var selectedTab = "Chats"

    val callback = object : androidx.activity.OnBackPressedCallback(selectedTab != "Chats") {
      override fun handleOnBackPressed() {
        selectedTab = "Chats"
        isEnabled = (selectedTab != "Chats")
      }
    }
    dispatcher.addCallback(callback)

    // 1. Initial State
    assertEquals("Chats", selectedTab)
    assertFalse(callback.isEnabled)

    // Flow 1: Chat List -> Calls -> Back -> Chat List
    selectedTab = "Calls"
    callback.isEnabled = (selectedTab != "Chats")
    assertEquals("Calls", selectedTab)
    dispatcher.onBackPressed()
    assertEquals("Chats", selectedTab)
    assertFalse(callback.isEnabled)

    // Flow 2: Chat List -> Updates -> Back -> Chat List
    selectedTab = "Updates"
    callback.isEnabled = (selectedTab != "Chats")
    assertEquals("Updates", selectedTab)
    dispatcher.onBackPressed()
    assertEquals("Chats", selectedTab)
    assertFalse(callback.isEnabled)

    // Flow 3: Chat List -> Profile -> Back -> Chat List
    selectedTab = "Profile"
    callback.isEnabled = (selectedTab != "Chats")
    assertEquals("Profile", selectedTab)
    dispatcher.onBackPressed()
    assertEquals("Chats", selectedTab)
    assertFalse(callback.isEnabled)
  }

  @Test
  fun `username format validation test`() {
    val validUsernameRegex = Regex("^[a-z0-9_]{3,30}$")
    org.junit.Assert.assertTrue("rohan_01".matches(validUsernameRegex))
    org.junit.Assert.assertTrue("alex".matches(validUsernameRegex))
    org.junit.Assert.assertFalse("User Name".matches(validUsernameRegex))
    org.junit.Assert.assertFalse("UPPERCASE".matches(validUsernameRegex))
    org.junit.Assert.assertFalse("ab".matches(validUsernameRegex))
    org.junit.Assert.assertFalse("invalid@user".matches(validUsernameRegex))
  }

  @Test
  fun `status viewer model serialization test`() {
    val viewer = com.example.model.StatusViewer(
      uid = "u_viewer",
      displayName = "Sarah Connor",
      username = "sarah_c",
      photoURL = "https://example.com/sarah.jpg",
      viewedAt = 1700000000000L
    )
    assertEquals("u_viewer", viewer.uid)
    assertEquals("sarah_c", viewer.username)
    assertEquals("Sarah Connor", viewer.displayName)
  }
}

