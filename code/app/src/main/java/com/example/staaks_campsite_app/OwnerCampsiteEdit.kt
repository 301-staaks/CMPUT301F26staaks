package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.session.SessionManager

private val ownerTabs = listOf(
    Tab("owner_reservations_active", "Reservations", Icons.Default.CalendarMonth),
    Tab("owner_notifications", "Notifications", Icons.Default.Notifications),
    Tab("owner_messages", "Messages", Icons.Default.Textsms),
    Tab("owner_lotteries", "Lotteries", Icons.Default.ConfirmationNumber)
)

/**
 * The owner's screen for creating a new campsite or editing an existing one
 *
 * @param navController controls navigation to other screens.
 * @param isNew true when creating a new campsite, which changes the title and button text.
 * @param ownerId the id of the owner who will own a newly created campsite.
 * @param existing the campsite being edited, or null when creating a new one.
 * @param onSave called with the campsite to store when the user saves.
 */
@Composable
fun OwnerCampsiteEdit(
    navController: NavController,
    isNew: Boolean,
    ownerId: String,
    existing: Campsite? = null,
    onSave: (Campsite) -> Unit
) {
    val session by SessionManager.state.collectAsState()

    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var location by remember(existing?.id) { mutableStateOf(existing?.location ?: "") }
    var capacity by remember(existing?.id) { mutableStateOf(existing?.capacity?.toString() ?: "") }
    var price by remember(existing?.id) { mutableStateOf(existing?.price?.toString() ?: "") }
    var description by remember(existing?.id) { mutableStateOf(existing?.description ?: "") }


    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Owner Main", homeRoute = "owner_main", isAdmin = session!!.isAdmin) },
        bottomBar = { UiBottomRow(navController = navController, tabs = ownerTabs) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            /*
            -------------------------------------------------------------------------------------------------------------------------------------
            Main Content of Page
            -------------------------------------------------------------------------------------------------------------------------------------
             */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Campsite Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Maximum Occupancy") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per Night") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())

                Button( // creates a new campsite owned by owner or updates changed values of an existing one -Szymon
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    ),
                    onClick = {
                        val campsiteToSave = if (existing != null) {
                            existing.copy(
                                name = name,
                                location = location,
                                capacity = capacity.toIntOrNull() ?: 0,
                                price = price.toDoubleOrNull() ?: 0.0,
                                description = description
                            )
                        } else {
                            Campsite(
                                ownerId = ownerId,
                                name = name,
                                location = location,
                                capacity = capacity.toIntOrNull() ?: 0,
                                price = price.toDoubleOrNull() ?: 0.0,
                                description = description
                            )
                        }
                        onSave(campsiteToSave)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isNew) "Create Campsite" else "Save Changes")
                }
            }
        }
    }
}
