package com.example.staaks_campsite_app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.staaks_campsite_app.data.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Change these if the labels in OwnerCampsiteEdit.kt are different
private const val NAME_LABEL = "Campsite Name"
private const val LOCATION_LABEL = "Location"
private const val SAVE_NEW = "Create Campsite"
private const val SAVE_EDIT = "Save Changes"

/**
 * US 02.01.01: the owner can register a campsite.
 * US 02.01.03: the owner can update their campsite.
 */
@RunWith(AndroidJUnit4::class)
class OwnerCampsiteEditTest {

    @get:Rule
    val rule = createComposeRule()

    // US 02.01.01
    @Test
    fun newCampsiteIsSavedWithOwnerId() {
        var saved: Campsite? = null
        rule.setContent {
            OwnerCampsiteEdit(
                navController = rememberNavController(),
                isNew = true,
                ownerId = "owner1",
                existing = null,
                onSave = { saved = it }
            )
        }
        rule.onNodeWithText(NAME_LABEL).performTextInput("Lake Site")
        rule.onNodeWithText(LOCATION_LABEL).performTextInput("Jasper, AB")
        rule.onNodeWithText(SAVE_NEW).performScrollTo().performClick()

        assertNotNull(saved)
        assertEquals("Lake Site", saved!!.name)
        assertEquals("Jasper, AB", saved!!.location)
        assertEquals("owner1", saved!!.ownerId)
    }

    // US 02.01.03
    @Test
    fun editFormIsPrefilled() {
        rule.setContent {
            OwnerCampsiteEdit(
                navController = rememberNavController(),
                isNew = false,
                ownerId = "owner1",
                existing = existingSite(),
                onSave = {}
            )
        }
        rule.onNodeWithText("Old Name").assertIsDisplayed()
    }

    // US 02.01.03
    @Test
    fun editSavesChangesAndKeepsId() {
        var saved: Campsite? = null
        rule.setContent {
            OwnerCampsiteEdit(
                navController = rememberNavController(),
                isNew = false,
                ownerId = "owner1",
                existing = existingSite(),
                onSave = { saved = it }
            )
        }
        rule.onNodeWithText(NAME_LABEL).performTextClearance()
        rule.onNodeWithText(NAME_LABEL).performTextInput("New Name")
        rule.onNodeWithText(SAVE_EDIT).performScrollTo().performClick()

        assertEquals("c1", saved?.id)
        assertEquals("owner1", saved?.ownerId)
        assertEquals("New Name", saved?.name)
    }

    private fun existingSite() = Campsite(
        id = "c1",
        ownerId = "owner1",
        name = "Old Name",
        location = "Jasper, AB",
        capacity = 4,
        date = mutableListOf(),
        price = 25.0,
        images = mutableListOf(),
        description = "",
        rating = 0.0,
        ratingCount = 0
    )
}
