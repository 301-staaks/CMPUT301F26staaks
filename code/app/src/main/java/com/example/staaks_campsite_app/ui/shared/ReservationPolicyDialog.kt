package com.example.staaks_campsite_app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.session.SessionManager


val app_policy = """
    1. Lottery & Reservations
    • Winners are drawn automatically when registration closes.
    • Selected entrants must confirm or decline within the booking window.
    • Owners may reject reservations and issue refunds during emergencies (e.g., wildfire warnings).

    2. Community Standards
    • Reviews and ratings must be genuine. Admins may remove violating content or accounts.

    3. Device ID & Privacy
    • Your account uses your unique device ID—no password required.
    • You can opt out of notifications or delete your profile anytime.
"""

/**
 * Policy guidelines page.
 * @param policy the policy text to display.
 * @param campsiteRules the campsite rules to display.
 * @param onConfirm called when the user confirms the reservation.
 * @param onDismiss called when the user cancels the reservation.
 *
 */
@Composable
fun PolicyGuidelinesPage(
    policy: String = app_policy,
    campsiteRules: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var agreed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rules & App Policy") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (campsiteRules.isNotBlank()) {
                    Text("Campsite Rules", fontWeight = FontWeight.Bold)
                    Text(campsiteRules, modifier = Modifier.padding(bottom = 12.dp))
                }
                Text("Platform Guidelines", fontWeight = FontWeight.Bold)
                Text(policy)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(checked = agreed, onCheckedChange = { agreed = it })
                    Text("I have read and agree to the rules.")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = agreed) {
                Text("Confirm Reservation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}