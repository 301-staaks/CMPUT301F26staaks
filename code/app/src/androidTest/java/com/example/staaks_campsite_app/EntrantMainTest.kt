package com.example.staaks_campsite_app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.staaks_campsite_app.EntreeMain
import org.junit.Rule
import org.junit.Test

/**
 * UI Test Suite for EntreeMain screen.
 * Tests top app bar buttons, bottom navigation items, and screen tab switching.
 * Uses `onAllNodesWithText` to handle duplicate text nodes (e.g. top bar title + bottom nav label).
 * US 01.01.01
 * US 01.01.02
 * US 01.01.03
 */
class EntrantMainUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEntreeMainDisplaysTopAndBottomBars() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            EntreeMain(navController = navController)
        }

        // Verify initial top app bar title ("Home")
        composeTestRule.onAllNodesWithText("Home").onFirst().assertIsDisplayed()

        // Verify bottom navigation buttons are displayed
        composeTestRule.onNodeWithText("Reservations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
        composeTestRule.onNodeWithText("Message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan").assertIsDisplayed()
    }

    @Test
    fun testBottomNavReservationsButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            EntreeMain(navController = navController)
        }

        // Click "Reservations" bottom navigation item
        composeTestRule.onNodeWithText("Reservations").performClick()

        // Verify top bar title / screen updates to "Reservations"
        composeTestRule.onAllNodesWithText("Reservations").onFirst().assertIsDisplayed()
    }

    @Test
    fun testBottomNavInboxButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            EntreeMain(navController = navController)
        }

        // Click "Inbox" bottom navigation item
        composeTestRule.onNodeWithText("Inbox").performClick()

        // Verify top bar title / screen updates to "Inbox"
        composeTestRule.onAllNodesWithText("Inbox").onFirst().assertIsDisplayed()
    }

    @Test
    fun testBottomNavMessagesButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            EntreeMain(navController = navController)
        }

        // Click "Message" bottom navigation item
        composeTestRule.onNodeWithText("Message").performClick()

        // Verify top bar title updates to "Messages"
        composeTestRule.onAllNodesWithText("Messages").onFirst().assertIsDisplayed()
    }

    @Test
    fun testBottomNavScanButton() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            EntreeMain(navController = navController)
        }

        // Click "Scan" bottom navigation item
        composeTestRule.onNodeWithText("Scan").performClick()

        // Verify top bar title / screen updates to "Scan"
        composeTestRule.onAllNodesWithText("Scan").onFirst().assertIsDisplayed()
    }
}