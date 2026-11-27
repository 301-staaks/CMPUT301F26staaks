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
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import com.example.staaks_campsite_app.data.Repositories
import com.example.staaks_campsite_app.data.chosenRecipients
import com.example.staaks_campsite_app.data.ownerNotificationErrors
import com.example.staaks_campsite_app.data.sentConfirmation
import com.example.staaks_campsite_app.session.SessionManager

/**
 * The owner's screen for writing a notification to the entrants of a lottery.
 *
 * The owner picks a lottery, sends to all its waitlist entrants or ticks some (US 02.10.01).
 * Missing pieces are shown in red and nothing is sent. Cancel asks before discarding.
 *
 * @param navController controls navigation to other screens.
 * @param ownerId the owner sending the notification.
 * @param lotteries the owner's lotteries to pick from.
 * @param entrantNames entrant names by id.
 * @param initialLotteryId the lottery to start with, when opened from a lottery card.
 */
@Composable
fun OwnerLotteryNotification(
    navController: NavController,
    ownerId: String = "",
    lotteries: List<NotifyLotteryOption> = emptyList(),
    entrantNames: Map<String, String> = emptyMap(),
    initialLotteryId: String = ""
) {

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

            var title by remember { mutableStateOf("") }
            var message by remember { mutableStateOf("") }
            var lotteryId by remember { mutableStateOf(initialLotteryId) }
            var pickerOpen by remember { mutableStateOf(false) }
            var sendToAll by remember { mutableStateOf(true) }
            var selected by remember { mutableStateOf(setOf<String>()) }
            var errors by remember { mutableStateOf(listOf<String>()) }
            var sending by remember { mutableStateOf(false) }
            var sentText by remember { mutableStateOf<String?>(null) }
            var confirmDiscard by remember { mutableStateOf(false) }

            val lottery = lotteries.find { it.lotteryId == lotteryId }
            val waitlist = lottery?.entrantIds ?: emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // lottery picker
                Box {
                    OutlinedButton(onClick = { pickerOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(lottery?.label ?: if (lotteries.isEmpty()) "No active lotteries" else "Select a lottery")
                    }
                    DropdownMenu(expanded = pickerOpen, onDismissRequest = { pickerOpen = false }) {
                        lotteries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    lotteryId = option.lotteryId
                                    selected = emptySet()
                                    pickerOpen = false
                                }
                            )
                        }
                    }
                }

                // recipients
                if (lottery != null && waitlist.isEmpty()) {
                    Text("There is no one on this lottery's waitlist to notify.", color = Color.Gray)
                } else if (lottery != null) {
                    Row(Modifier.clickable { sendToAll = true }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = sendToAll, onClick = { sendToAll = true })
                        Text("All waitlist entrants (${waitlist.size})", fontSize = 16.sp)
                    }
                    Row(Modifier.clickable { sendToAll = false }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !sendToAll, onClick = { sendToAll = false })
                        Text("Choose entrants", fontSize = 16.sp)
                    }
                    if (!sendToAll) {
                        waitlist.forEach { id ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = id in selected,
                                    onCheckedChange = { selected = if (it) selected + id else selected - id }
                                )
                                Text(entrantNames[id]?.ifBlank { null } ?: "Unnamed entrant")
                            }
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, minLines = 4, modifier = Modifier.fillMaxWidth())

                errors.forEach { Text(it, color = Color.Red) }

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    ),
                    enabled = !sending,
                    onClick = {
                        val recipients = chosenRecipients(waitlist, sendToAll, selected)
                        errors = ownerNotificationErrors(lottery?.lotteryId ?: "", recipients, message)
                        if (errors.isEmpty() && lottery != null) {
                            sending = true
                            Repositories.notifications.sendOwnerMessage(
                                ownerId, recipients, lottery.campsiteId, lottery.campsiteName,
                                title, message, lottery.lotteryId
                            )
                                .addOnSuccessListener { sending = false; sentText = sentConfirmation(recipients.size) }
                                .addOnFailureListener { sending = false; errors = listOf("Could not send the notification. Try again.") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (sending) "Sending..." else "Send Notification")
                }

                TextButton(
                    onClick = {
                        if (title.isNotBlank() || message.isNotBlank()) confirmDiscard = true
                        else navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }

            sentText?.let { text ->
                AlertDialog(
                    onDismissRequest = { sentText = null; navController.popBackStack() },
                    confirmButton = { TextButton(onClick = { sentText = null; navController.popBackStack() }) { Text("OK") } },
                    text = { Text(text) }
                )
            }

            if (confirmDiscard) {
                AlertDialog(
                    onDismissRequest = { confirmDiscard = false },
                    confirmButton = { TextButton(onClick = { confirmDiscard = false; navController.popBackStack() }) { Text("Discard") } },
                    dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing") } },
                    text = { Text("Discard this notification?") }
                )
            }
        }
    }
}

/**
 * one of the owner's lotteries in the notification form's picker
 *
 * @property lotteryId the lottery
 * @property campsiteId the lottery's main campsite, saved on the notification
 * @property campsiteName every campsite in the lottery, shown in the entrant's inbox
 * @property label the text in the picker
 * @property entrantIds who can be notified, from notifiableEntrants()
 */
data class NotifyLotteryOption(
    val lotteryId: String,
    val campsiteId: String,
    val campsiteName: String,
    val label: String,
    val entrantIds: List<String>
)
