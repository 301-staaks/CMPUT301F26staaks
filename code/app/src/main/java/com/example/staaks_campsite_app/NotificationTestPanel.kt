package com.example.staaks_campsite_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staaks_campsite_app.data.Repositories
import com.google.android.gms.tasks.Task

/*
 * DEBUG ONLY. delete this file (and its call in NotificationInbox) once the lottery,
 * reservation and owner screens send real notifications.
 *
 * everything here is placeholder data: there are no lotteries or waitlists yet,
 * so the "waitlist" is just the recipient typed in, or your own device.
 */
private const val PLACEHOLDER_CAMPSITE_ID = "placeholder-campsite"
private const val PLACEHOLDER_CAMPSITE_NAME = "Test Campsite"
private const val PLACEHOLDER_LOTTERY_ID = "placeholder-lottery"
private const val PLACEHOLDER_RESERVATION_ID = "placeholder-reservation"
private const val PLACEHOLDER_OWNER_ID = "placeholder-owner"
private const val PLACEHOLDER_START_DATE = "2027-05-21"
private const val PLACEHOLDER_END_DATE = "2027-05-23"

/**
 * buttons that send one of each notification type with placeholder data
 *
 * by default they go to the current device, so they show up in this inbox. type
 * another user's id to send to them instead (for example a second emulator).
 *
 * @param currentUserId the id used when the recipient field is blank
 */
@Composable
fun NotificationTestPanel(currentUserId: String) {
    val repository = Repositories.notifications
    var recipient by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val recipients = listOf(recipient.trim().ifBlank { currentUserId })

    fun report(task: Task<Void>, label: String) {
        status = "Sending $label..."
        task
            .addOnSuccessListener { status = "Sent: $label" }
            .addOnFailureListener { status = "Failed: ${it.message}" }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Debug: send test notifications", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text("Recipient id (blank = you)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TestButton("Win") {
                    report(
                        repository.sendLotteryWin(
                            recipients, PLACEHOLDER_CAMPSITE_ID,
                            PLACEHOLDER_CAMPSITE_NAME, PLACEHOLDER_LOTTERY_ID
                        ),
                        "win"
                    )
                }
                TestButton("Not selected") {
                    report(
                        repository.sendNotSelected(
                            recipients, PLACEHOLDER_CAMPSITE_ID,
                            PLACEHOLDER_CAMPSITE_NAME, PLACEHOLDER_LOTTERY_ID
                        ),
                        "not selected"
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TestButton("Confirmation") {
                    report(
                        repository.sendReservationConfirmed(
                            recipients.first(), PLACEHOLDER_CAMPSITE_ID, PLACEHOLDER_CAMPSITE_NAME,
                            PLACEHOLDER_START_DATE, PLACEHOLDER_END_DATE,
                            PLACEHOLDER_LOTTERY_ID, PLACEHOLDER_RESERVATION_ID
                        ),
                        "confirmation"
                    )
                }
                TestButton("Owner message") {
                    report(
                        repository.sendOwnerMessage(
                            PLACEHOLDER_OWNER_ID, recipients, PLACEHOLDER_CAMPSITE_ID,
                            PLACEHOLDER_CAMPSITE_NAME, "",
                            "Check-in opens at 3 pm. See you there!", PLACEHOLDER_LOTTERY_ID
                        ),
                        "owner message"
                    )
                }
            }

            if (status.isNotBlank()) {
                Text(status, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TestButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 8.dp)) {
        Text(label, fontSize = 12.sp)
    }
}
