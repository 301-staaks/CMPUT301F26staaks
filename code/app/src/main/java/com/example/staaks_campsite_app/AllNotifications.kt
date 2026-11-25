package com.example.staaks_campsite_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.NotificationSend
import com.example.staaks_campsite_app.data.Repositories
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.formatSentAt
import com.example.staaks_campsite_app.data.groupIntoSends
import com.example.staaks_campsite_app.data.logName
import com.example.staaks_campsite_app.data.notificationTypeLabel
import com.example.staaks_campsite_app.data.recipientSummary

/**
 * the admin's log of every notification sent in the app (US 03.08.01)
 *
 * shows owner messages and the notifications the app sends by itself (win, not
 * selected, confirmation), newest first. a notification sent to several entrants
 * shows once. each entry shows the sender, recipients, content and time sent, and
 * tapping one shows the full message and every recipient.
 *
 * the log is view-only: nothing can be edited or deleted from it. notifications an
 * entrant cleared from their inbox still show here.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun AllNotifications(navController: NavController) {
    val flow = remember { Repositories.notifications.observeAll() }
    val result by flow.collectAsState(initial = null)

    val userRepository = remember { UserRepository() }
    val namesById = userRepository.users.associate { it.id to it.name }

    var selected by remember { mutableStateOf<NotificationSend?>(null) }

    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "All Notifications", homeRoute = "admin_main") },
        bottomBar = { UiBottomRow(navController = navController, tabs = adminTabs) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            val current = result

            if (current == null) {
                Text("Loading notifications...")
            } else if (current.isFailure) {
                Text("Could not load notifications.", color = Color.Red)
            } else {
                val sends = groupIntoSends(current.getOrDefault(emptyList()))

                if (sends.isEmpty()) {
                    Text("No notifications found")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sends, key = { it.batchId }) { send ->
                            NotificationLogCard(
                                send = send,
                                namesById = namesById,
                                onClick = { selected = send }
                            )
                        }
                    }
                }
            }
        }
    }

    val send = selected
    if (send != null) {
        NotificationLogDetails(
            send = send,
            namesById = namesById,
            onClose = { selected = null }
        )
    }
}

@Composable
private fun NotificationLogCard(
    send: NotificationSend,
    namesById: Map<String, String>,
    onClick: () -> Unit
) {
    val recipientNames = send.recipientIds.map { logName(it, namesById) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = notificationTypeLabel(send.type),
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = send.title.ifBlank { "(no title)" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text("From: ${logName(send.senderId, namesById)}")
            Text("To: ${recipientSummary(recipientNames)}")
            Text(
                text = send.message,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatSentAt(send.sentAt),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NotificationLogDetails(
    send: NotificationSend,
    namesById: Map<String, String>,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(send.title.ifBlank { "(no title)" }) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Type: ${notificationTypeLabel(send.type)}")
                Text("From: ${logName(send.senderId, namesById)}")
                if (send.campsiteName.isNotBlank()) {
                    Text("Campsite: ${send.campsiteName}")
                }
                Text("Sent: ${formatSentAt(send.sentAt)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(send.message)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recipients (${send.recipientIds.size})",
                    fontWeight = FontWeight.Bold
                )
                for (recipientId in send.recipientIds) {
                    Text("- ${logName(recipientId, namesById)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Close") }
        }
    )
}
