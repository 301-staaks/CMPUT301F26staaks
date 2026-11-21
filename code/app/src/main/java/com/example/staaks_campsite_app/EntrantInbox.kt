package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.ApplicationInfo
import androidx.compose.ui.platform.LocalContext
import com.example.staaks_campsite_app.data.Repositories
import com.example.staaks_campsite_app.data.formatSentAt
import com.example.staaks_campsite_app.data.inboxNotifications
import com.example.staaks_campsite_app.data.model.Notification

/**
 * the entrant's inbox (US 01.03.01, US 01.03.02, US 01.05.01)
 *
 * lists the notifications sent to the entrant, newest first. unread ones have a
 * green dot and bold text. tapping one marks it read and calls onOpenNotification.
 * "Clear All" hides them from the inbox without deleting them.
 *
 * debug builds also show NotificationTestPanel at the top, to send placeholder
 * notifications until the lottery and reservation code can send real ones.
 *
 * @param entrantId the id of the entrant whose inbox this is
 * @param onOpenNotification called with the notification that was tapped. nothing
 * opens yet; this is where the Lottery Win page and Reservations tab get linked later
 * @param modifier modifier for the inbox
 */
@Composable
fun NotificationInbox(
    entrantId: String,
    onOpenNotification: (Notification) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDebugBuild = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val repository = Repositories.notifications
    val flow = remember(entrantId) { repository.observeReceived(entrantId) }
    val result by flow.collectAsState(initial = null)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        if (isDebugBuild) {
            // TODO remove once lotteries and reservations send real notifications
            NotificationTestPanel(currentUserId = entrantId)
            Spacer(modifier = Modifier.height(8.dp))
        }

        val current = result

        if (current == null) {
            Text("Loading notifications...")
        } else if (current.isFailure) {
            Text("Could not load notifications.", color = Color.Red)
        } else {
            val visible = inboxNotifications(current.getOrDefault(emptyList()))

            if (visible.isEmpty()) {
                Text("No notifications.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visible, key = { it.notificationId }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) {
                                    repository.markAsRead(notification.notificationId)
                                }
                                onOpenNotification(notification)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { repository.clearAll(visible.map { it.notificationId }) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Clear All")
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification, onClick: () -> Unit) {
    val unread = !notification.isRead
    val weight = if (unread) FontWeight.Bold else FontWeight.Normal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (unread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF009E60))
                        .semantics { contentDescription = "Unread" }
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.campsiteName.ifBlank { "Campsite" },
                    fontSize = 18.sp,
                    fontWeight = weight
                )
                if (notification.title.isNotBlank()) {
                    Text(notification.title, fontWeight = weight)
                }
                Text(notification.message)
                Text(
                    text = formatSentAt(notification.sentAt),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
