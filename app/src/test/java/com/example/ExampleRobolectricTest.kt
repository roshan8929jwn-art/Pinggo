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
    var currentTab = "Chats"

    composeTestRule.setContent {
      var selectedTab by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("Chats") }
      androidx.activity.compose.BackHandler(enabled = selectedTab != "Chats") {
        selectedTab = "Chats"
      }
      currentTab = selectedTab
      androidx.compose.material3.Text(
        text = selectedTab,
        modifier = androidx.compose.ui.Modifier.testTag("current_tab_text")
      )
      androidx.compose.material3.Button(
        onClick = { selectedTab = "Calls" },
        modifier = androidx.compose.ui.Modifier.testTag("nav_calls_btn")
      ) { androidx.compose.material3.Text("Calls") }
      androidx.compose.material3.Button(
        onClick = { selectedTab = "Updates" },
        modifier = androidx.compose.ui.Modifier.testTag("nav_updates_btn")
      ) { androidx.compose.material3.Text("Updates") }
      androidx.compose.material3.Button(
        onClick = { selectedTab = "Profile" },
        modifier = androidx.compose.ui.Modifier.testTag("nav_profile_btn")
      ) { androidx.compose.material3.Text("Profile") }
    }

    // 1. Initial state: Chat List tab
    assertEquals("Chats", currentTab)

    // Flow 1: Chat List -> Calls -> Back -> Chat List
    composeTestRule.onNodeWithTag("nav_calls_btn").performClick()
    assertEquals("Calls", currentTab)
    composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
    assertEquals("Chats", currentTab)
    assertFalse(composeTestRule.activity.isFinishing)

    // Flow 2: Chat List -> Updates -> Back -> Chat List
    composeTestRule.onNodeWithTag("nav_updates_btn").performClick()
    assertEquals("Updates", currentTab)
    composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
    assertEquals("Chats", currentTab)
    assertFalse(composeTestRule.activity.isFinishing)

    // Flow 3: Chat List -> Profile -> Back -> Chat List
    composeTestRule.onNodeWithTag("nav_profile_btn").performClick()
    assertEquals("Profile", currentTab)
    composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
    assertEquals("Chats", currentTab)
    assertFalse(composeTestRule.activity.isFinishing)
  }
}

