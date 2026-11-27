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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.staaks_campsite_app.data.Repositories
import com.example.staaks_campsite_app.data.formatSentAt
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.groupIntoSends
import com.example.staaks_campsite_app.data.logName
import com.example.staaks_campsite_app.data.recipientSummary


/**
 * The owner's notifications screen.
 *
 * Has the Send Notification button (US 02.10.01) and the list of notifications this
 * owner sent, newest first (US 02.10.02). Tapping one shows the full message and
 * every recipient.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun OwnerNotifications(navController: NavController) {

    val session by SessionManager.state.collectAsState()

    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Notifications", homeRoute = "owner_main") },
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

            val context = LocalContext.current
            val ownerId = remember(context) { getDeviceId(context) }
            val sentFlow = remember(ownerId) { Repositories.notifications.observeSent(ownerId) }
            val sentResult by sentFlow.collectAsState(initial = null)
            val sends = groupIntoSends(sentResult?.getOrNull() ?: emptyList())
            val namesById = Repositories.users.users.associate { it.id to it.name }
            var expanded by remember { mutableStateOf("") }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // opens the owner's Send Notification form (US 02.10.01)
                item {
                    Button(
                        onClick = { navController.navigate("owner_lottery_notification") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                    ) {
                        Text("Send Notification", fontSize = 20.sp)
                    }
                }

                item {
                    when {
                        sentResult == null -> Text("Loading sent notifications...")
                        sentResult?.isFailure == true -> Text("Could not load sent notifications.", color = Color.Red)
                        sends.isEmpty() -> Text("No notifications sent")
                    }
                }

                items(sends, key = { it.batchId }) { send ->
                    val names = send.recipientIds.map { logName(it, namesById) }
                    val open = expanded == send.batchId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = if (open) "" else send.batchId }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(send.campsiteName.ifBlank { "Campsite" }, fontSize = 18.sp)
                            Text("To: " + if (open) names.joinToString(", ") else recipientSummary(names))
                            Text(if (open) send.message else send.message.take(60) + if (send.message.length > 60) "..." else "")
                            Text(formatSentAt(send.sentAt), color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
