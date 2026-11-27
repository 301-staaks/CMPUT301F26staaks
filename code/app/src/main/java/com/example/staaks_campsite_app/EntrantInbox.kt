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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.staaks_campsite_app.data.cancellationCsv
import com.example.staaks_campsite_app.data.csvFileName
import com.example.staaks_campsite_app.data.lotteryReservationId
import com.example.staaks_campsite_app.data.opensDetail
import com.example.staaks_campsite_app.data.reservationCsv
import com.example.staaks_campsite_app.data.winChoiceErrors
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import com.google.android.gms.tasks.Tasks
import com.example.staaks_campsite_app.data.model.NotificationType
import com.example.staaks_campsite_app.ui.shared.rememberCsvDownloader
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore

/**
 * the entrant's inbox (US 01.03.01, US 01.03.02, US 01.05.01)
 *
 * lists the notifications sent to the entrant, newest first. unread ones have a
 * green dot and bold text. tapping one marks it read. win, confirmation and cancellation
 * notifications open a dialog with the full message, Confirm Reservation (win) and
 * Download CSV (US 01.05.02, US 01.07.02). the others call onOpenNotification.
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
    var opened by remember { mutableStateOf<Notification?>(null) }

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
                                if (opensDetail(notification)) {
                                    opened = notification
                                } else {
                                    onOpenNotification(notification)
                                }
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

    opened?.let { notification ->
        NotificationDialog(notification, entrantId, onClose = { opened = null })
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

/**
 * the full notification, with Confirm Reservation on a win and Download CSV once
 * there is a reservation (US 01.03.01, US 01.05.01, US 01.05.02, US 01.07.02)
 *
 * before confirming, the winner picks one of the lottery's campsites and their
 * arrival and departure dates inside the booking period. booked days are shown
 * and can't be picked.
 *
 * a win's reservation id is <lotteryId>_<entrantId>, so after confirming, the same
 * win notification also offers the CSV
 */
@Composable
private fun NotificationDialog(notification: Notification, entrantId: String, onClose: () -> Unit) {
    val db = Firebase.firestore
    val isWin = notification.type == NotificationType.WIN
    val reservationId = if (isWin) {
        lotteryReservationId(notification.lotteryId, entrantId)
    } else {
        notification.reservationId
    }

    var reservation by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(0) }
    var working by remember { mutableStateOf(false) }
    var showPolicy by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // the winner's choice, only used while the win is not confirmed yet
    var winCampsites by remember { mutableStateOf(listOf<WinCampsite>()) }
    var choicesLoaded by remember { mutableStateOf(false) }
    var bookingStart by remember { mutableStateOf("") }
    var bookingEnd by remember { mutableStateOf("") }
    var pickedCampsite by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var choiceErrors by remember { mutableStateOf(listOf<String>()) }

    // loads the lottery's booking period and campsites, with their booked days
    fun loadChoices() {
        db.collection("lotteries").document(notification.lotteryId).get()
            .addOnSuccessListener { lottery ->
                bookingStart = lottery.getString("bookingStartDate") ?: ""
                bookingEnd = lottery.getString("bookingEndDate") ?: ""
                if (startDate.isBlank()) startDate = bookingStart
                if (endDate.isBlank()) endDate = bookingEnd

                val ids = (listOf(lottery.getString("campsiteId") ?: "") +
                        ((lottery.get("campsiteIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()))
                    .filter { it.isNotBlank() }
                    .distinct()
                val tasks = ids.map { db.collection("campsites").document(it).get() }

                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    winCampsites = tasks
                        .filter { it.isSuccessful && it.result.exists() }
                        .map { task ->
                            val campsite = task.result
                            WinCampsite(
                                id = campsite.id,
                                name = campsite.getString("name")?.ifBlank { null } ?: "Campsite",
                                booked = (campsite.get("booked") as? List<*>)
                                    ?.filterIsInstance<String>() ?: emptyList()
                            )
                        }
                    if (winCampsites.size == 1) pickedCampsite = winCampsites[0].id
                    choicesLoaded = true
                }
            }
            .addOnFailureListener {
                error = "Could not load this lottery."
                choicesLoaded = true
            }
    }

    val downloadCsv = rememberCsvDownloader(
        onError = { error = it },
        onSaved = { info = "CSV saved." }
    )

    LaunchedEffect(reservationId, reload) {
        if (reservationId.isBlank() || (isWin && notification.lotteryId.isBlank())) {
            loaded = true
            choicesLoaded = true
            return@LaunchedEffect
        }
        db.collection("reservations").document(reservationId).get()
            .addOnSuccessListener { document ->
                reservation = if (document.exists()) document else null
                loaded = true
                if (isWin && !document.exists() && !choicesLoaded) {
                    loadChoices()
                }
            }
            .addOnFailureListener {
                loaded = true
                error = "Could not load the reservation."
            }
    }

    // looks up the entrant name and campsite location, then saves the CSV
    fun download(saved: DocumentSnapshot) {
        working = true
        error = ""
        db.collection("users").document(entrantId).get().addOnCompleteListener { user ->
            val entrantName = if (user.isSuccessful) user.result?.getString("name") ?: "" else ""
            val campsiteId = saved.getString("campsiteId") ?: ""

            fun save(location: String) {
                working = false
                val campsiteName = saved.getString("campsiteName")?.ifBlank { null } ?: notification.campsiteName
                val start = saved.getString("startDate") ?: ""
                val end = saved.getString("endDate") ?: ""
                val cancelled = saved.getString("status") == "Cancelled"

                if (cancelled) {
                    val cancelledDate = saved.getString("cancelledDate")?.ifBlank { null }
                        ?: formatSentAt(notification.sentAt).take(10)
                    downloadCsv(
                        csvFileName("cancellation", campsiteName, start),
                        cancellationCsv(entrantName, campsiteName, location, start, end, cancelledDate)
                    )
                } else {
                    downloadCsv(
                        csvFileName("reservation", campsiteName, start),
                        reservationCsv(entrantName, campsiteName, location, start, end)
                    )
                }
            }

            if (campsiteId.isBlank()) {
                save("")
            } else {
                db.collection("campsites").document(campsiteId).get().addOnCompleteListener { campsite ->
                    save(if (campsite.isSuccessful) campsite.result?.getString("location") ?: "" else "")
                }
            }
        }
    }

    val saved = reservation

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(notification.title.ifBlank { "Notification" }) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(notification.campsiteName.ifBlank { "Campsite" }, fontWeight = FontWeight.Bold)
                Text(notification.message)
                Text(formatSentAt(notification.sentAt), color = Color.Gray, fontSize = 12.sp)
                if (!loaded) {
                    Text("Loading reservation...", color = Color.Gray)
                } else if (saved == null && !isWin) {
                    Text("This reservation was not found, so no CSV is available.", color = Color.Gray)
                }
                if (loaded && saved == null && isWin) {
                    if (!choicesLoaded) {
                        Text("Loading campsites...", color = Color.Gray)
                    } else {
                        Text("Pick your campsite", fontWeight = FontWeight.Bold)
                        if (winCampsites.isEmpty()) {
                            Text("This lottery's campsites no longer exist.", color = Color.Red)
                        }
                        winCampsites.forEach { campsite ->
                            Row(
                                modifier = Modifier.clickable { pickedCampsite = campsite.id },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = pickedCampsite == campsite.id,
                                    onClick = { pickedCampsite = campsite.id }
                                )
                                Text(campsite.name)
                            }
                        }

                        Text("Pick your dates", fontWeight = FontWeight.Bold)
                        Text("Booking period: $bookingStart to $bookingEnd", color = Color.Gray, fontSize = 12.sp)
                        DateField("Arrival", startDate, null) { startDate = it }
                        DateField("Departure", endDate, null) { endDate = it }

                        val booked = winCampsites.find { it.id == pickedCampsite }?.booked
                            ?.filter { it in bookingStart..bookingEnd }
                            ?.sorted()
                            ?: emptyList()
                        if (booked.isNotEmpty()) {
                            Text("Already booked: ${booked.joinToString(", ")}", color = Color.Gray, fontSize = 12.sp)
                        }
                        choiceErrors.forEach { Text(it, color = Color.Red) }
                    }
                }
                if (info.isNotBlank()) Text(info)
                if (error.isNotBlank()) Text(error, color = Color.Red)
            }
        },
        confirmButton = {
            if (loaded && saved != null) {
                Button(enabled = !working, onClick = { download(saved) }) { Text("Download CSV") }
            } else if (loaded && isWin && choicesLoaded) {
                Button(
                    enabled = !working,
                    onClick = {
                        val booked = winCampsites.find { it.id == pickedCampsite }?.booked ?: emptyList()
                        choiceErrors = winChoiceErrors(
                            pickedCampsite, startDate, endDate, bookingStart, bookingEnd, booked
                        )
                        if (choiceErrors.isEmpty()) showPolicy = true
                    }
                ) { Text("Confirm Reservation") }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Close") }
        }
    )

    if (showPolicy) {
        PolicyGuidelinesPage(
            campsiteRules = "",
            onConfirm = {
                showPolicy = false
                working = true
                error = ""
                Repositories.reservations.confirmReservation(
                    notification.lotteryId, entrantId, pickedCampsite, startDate, endDate
                )
                    .addOnSuccessListener {
                        working = false
                        info = "Reservation confirmed."
                        reload++
                    }
                    .addOnFailureListener {
                        working = false
                        error = it.message ?: "Could not confirm the reservation."
                    }
            },
            onDismiss = { showPolicy = false }
        )
    }
}

/** one campsite a winner can pick, with the days already booked there */
private data class WinCampsite(
    val id: String,
    val name: String,
    val booked: List<String>
)
