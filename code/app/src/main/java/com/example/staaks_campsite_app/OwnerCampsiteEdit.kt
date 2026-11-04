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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.Campsite

// OWNER VIEW (preview only) - all data on this screen is placeholder
@Composable
fun OwnerCampsiteEdit(
    navController: NavController,
    isNew: Boolean,
    ownerId: String,
    existing: Campsite? = null,
    onSave: (Campsite) -> Unit
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var location by remember(existing?.id) { mutableStateOf(existing?.location ?: "") }
    var capacity by remember(existing?.id) { mutableStateOf(existing?.capacity?.toString() ?: "") }
    var price by remember(existing?.id) { mutableStateOf(existing?.price?.toString() ?: "") }
    var description by remember(existing?.id) { mutableStateOf(existing?.description ?: "") }

    Column {

        // Pinned Top Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()            // Makes the row span the full width
                .background(color = Color(0xFF1B5E20))
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text("Back")
                }
            }

            Text(
                text = if (isNew) "New Site" else "Edit Site",
                fontSize = 25.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {

            }
        }

        // This is for when the admin is spoofing a different user. Logic must be implemented here to allow this to pop up
        if (false) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()            // Makes the row span the full width
                    .background(color = Color(0xFF63B267))
                    .heightIn(max = 45.dp)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "View:",
                    fontSize = 15.sp,
                    color = Color.White
                )
                Button(onClick = { /* ... */ }) { Text("Admin") }
                Button(onClick = { /* ... */ }) { Text("Owner") }
                Button(onClick = { /* ... */ }) { Text("Entree") }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
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

        // Pinned Bottom Row (Owner navigation)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("owner_reservations_active") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Reservations", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_notifications") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Notifs", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_messages") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Messages", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_lotteries") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Lotteries", fontSize = 11.sp) }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .heightIn(max = 16.dp)
                .heightIn(min = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

        }
    }
}
