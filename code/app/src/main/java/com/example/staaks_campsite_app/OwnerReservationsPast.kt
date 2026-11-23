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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.session.SessionManager

/**
 * The owner's "Past" reservations screen.
 *
 * The reservation cards currently show placeholder data. They are not loaded
 * from the database yet.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun OwnerReservationsPast(navController: NavController) {

    val session by SessionManager.state.collectAsState()

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

            val reservations = listOf("#1001", "#1002", "#1003") // placeholder

            Column(modifier = Modifier.fillMaxSize()) {
                // Switch between Active / Past / Cancelled
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009E60),
                            contentColor = Color.White
                        ),
                        onClick = { navController.navigate("owner_reservations_active") { launchSingleTop = true } },
                        enabled = true
                    ) { Text("Active") }
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009E60),
                            contentColor = Color.White
                        ),
                        onClick = { navController.navigate("owner_reservations_past") { launchSingleTop = true } },
                        enabled = false
                    ) { Text("Past") }
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009E60),
                            contentColor = Color.White
                        ),
                        onClick = { navController.navigate("owner_reservations_cancelled") { launchSingleTop = true } },
                        enabled = true
                    ) { Text("Cancelled") }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reservations) { id ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Reservation $id", fontSize = 18.sp)
                                Text("Campsite: —  |  Site: —")
                                Text("Guest: —")
                                Text("Dates: —")
                                Text("Status: Past")
                            }
                        }
                    }
                }
            }
        }
    }
}
