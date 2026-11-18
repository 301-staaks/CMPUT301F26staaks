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
import androidx.compose.foundation.layout.width
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

private val ownerTabs = listOf(
    Tab("owner_reservations_active", "Reservations", Icons.Default.CalendarMonth),
    Tab("owner_notifications", "Notifications", Icons.Default.Notifications),
    Tab("owner_messages", "Messages", Icons.Default.Textsms),
    Tab("owner_lotteries", "Lotteries", Icons.Default.ConfirmationNumber)
)
/**
 * The owner's chat screen for a conversation with a guest.
 *
 * The messages shown are placeholder data. "Send" currently only clears the text field, and
 * messages are not saved or loaded from the database yet.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun OwnerLotteries(navController: NavController) {

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

            val lotteries = listOf("Lottery 1", "Lottery 2") // placeholder (active lotteries)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row() {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009E60),
                            contentColor = Color.White
                        ),
                        onClick = { navController.navigate("owner_lotteries_past") }
                    ) {
                        Text("Past Lotteries")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF009E60),
                            contentColor = Color.White
                        ),
                        onClick = { navController.navigate("owner_lottery_new") }) {
                        Text("Create")
                    }

                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lotteries) { name ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(name, fontSize = 18.sp)
                                Text("Campsite: —")
                                Text("Entries: —")
                                Text("Draw date: —")
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF009E60),
                                            contentColor = Color.White
                                        ),
                                        onClick = { navController.navigate("owner_lottery_edit") }) {
                                        Text("Edit")
                                    }
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF009E60),
                                            contentColor = Color.White
                                        ),
                                        onClick = { navController.navigate("owner_lottery_notification") }) {
                                        Text("New Notification")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
