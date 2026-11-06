package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.model.Campsite

// OWNER VIEW (preview only) - all data on this screen is placeholder
@Composable
fun OwnerMain(
    navController: NavController,
    campsites: List<Campsite>,
    onEditCampsite: (Campsite) -> Unit
) {
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
                        navController.navigate("start")
                    }
                ) {
                    Text("Back")
                }
            }

            Text(
                text = "Campsites",
                fontSize = 25.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = { navController.navigate("owner_campsite_new") }) {
                    Text("Add")
                }
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

            // One preview card per campsite
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Replaced placeholders with actual campsite data -Szymon
                items(campsites, key = { it.id }) { campsite ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(campsite.name, fontSize = 20.sp)
                            Text("Location: ${campsite.location}")
                            Text("Capacity: ${campsite.capacity}  |  Price/night: ${campsite.price}")
                            Text("Upcoming reservations: —")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onEditCampsite(campsite) }) {
                                Text("Edit")
                            }
                        }
                    }
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
