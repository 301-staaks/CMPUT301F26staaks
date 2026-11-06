package com.example.staaks_campsite_app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.staaks_campsite_app.data.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * US 02.03.01: the owner can view a list of their campsites.
 * Also checks the Edit entry point for US 02.01.03.
 */
@RunWith(AndroidJUnit4::class)
class OwnerMainTest {

    @get:Rule
    val rule = createComposeRule()

    private fun site(id: String, name: String) = Campsite(
        id = id,
        ownerId = "owner1",
        name = name,
        location = "Jasper, AB",
        capacity = 4,
        date = mutableListOf(),
        price = 25.0,
        images = mutableListOf(),
        description = "",
        rating = 0.0,
        ratingCount = 0
    )

    @Test
    fun showsAllOwnerCampsites() {
        val sites = listOf(site("c1", "Lake Site"), site("c2", "Forest Site"))
        rule.setContent {
            OwnerMain(
                navController = rememberNavController(),
                campsites = sites,
                onEditCampsite = {}
            )
        }
        rule.onNodeWithText("Lake Site").assertIsDisplayed()
        rule.onNodeWithText("Forest Site").assertIsDisplayed()
    }

    @Test
    fun editButtonPassesThatCampsite() {
        var edited: Campsite? = null
        val sites = listOf(site("c1", "Lake Site"), site("c2", "Forest Site"))
        rule.setContent {
            OwnerMain(
                navController = rememberNavController(),
                campsites = sites,
                onEditCampsite = { edited = it }
            )
        }
        rule.onAllNodesWithText("Edit")[1].performClick()
        assertEquals("c2", edited?.id)
    }

    @Test
    fun emptyListShowsNoCampsites() {
        rule.setContent {
            OwnerMain(
                navController = rememberNavController(),
                campsites = emptyList(),
                onEditCampsite = {}
            )
        }
        rule.onAllNodesWithText("Edit").assertCountEquals(0)
    }
}
