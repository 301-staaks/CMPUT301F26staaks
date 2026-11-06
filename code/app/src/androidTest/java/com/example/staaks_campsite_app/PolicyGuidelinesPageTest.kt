package com.example.staaks_campsite_app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * US 01.11.01: the entrant can see the app policy and guidelines.
 */
@RunWith(AndroidJUnit4::class)
class PolicyGuidelinesPageTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun policyTextIsShown() {
        rule.setContent {
            PolicyGuidelinesPage(navController = rememberNavController())
        }
        rule.onAllNodesWithText("Policy", substring = true, ignoreCase = true)
            .onFirst()
            .assertIsDisplayed()
    }
}
