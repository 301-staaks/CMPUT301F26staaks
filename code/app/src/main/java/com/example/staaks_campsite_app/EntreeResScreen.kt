package com.example.staaks_campsite_app


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.Reservation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.LotteryRepository
import com.example.staaks_campsite_app.data.Repositories
import com.example.staaks_campsite_app.data.entrantLotteryStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * displays the entrant's reservation, lottery and history tabs
 *
 * the history tab shows the current entrant's cancelled reservations
 * the reservations tab lists their active reservations with a Cancel button (US 01.07.01)
 * the lotteries tab shows Pending / Won / Lost / Confirmed (US 01.03.01, US 01.03.02)
 *
 * @param navController controls navigation between screens
 * @param lotteryRepository the loaded lotteries and waitlist entries, for the lotteries tab
 * @param campsiteRepository the loaded campsites, for campsite names on the lotteries tab
 */
@Composable
fun EntreeResScreen(
    navController: NavController,
    lotteryRepository: LotteryRepository? = null,
    campsiteRepository: CampsiteRepository? = null
) {
    val context = LocalContext.current
    val entrantId = getDeviceId(context)

    var selectedTab by remember { mutableStateOf("Reservations") }

    val cancelledReservations = remember {
        mutableStateListOf<Reservation>()
    }

    val activeReservations = remember {
        mutableStateListOf<Reservation>()
    }

    // the reservation waiting for the entrant to confirm cancelling it
    var toCancel by remember { mutableStateOf<Reservation?>(null) }
    var cancelError by remember { mutableStateOf("") }

    var isLoadingHistory by remember {
        mutableStateOf(true)
    }

    var historyError by remember {
        mutableStateOf("")
    }

    DisposableEffect(entrantId) {

        val listener = Firebase.firestore.collection("reservations")
            .addSnapshotListener { snapshot, error ->

                isLoadingHistory = false

                if (error != null) {
                    historyError = "Could not load booking history."
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    historyError = "Could not load booking history."
                    return@addSnapshotListener
                }

                val loadedReservations = mutableListOf<Reservation>()
                val loadedActive = mutableListOf<Reservation>()

                // finds canceled reservations belonging to this entrant.
                for (document in snapshot.documents) {

                    val reservation =
                        document.toObject(Reservation::class.java)

                    if (reservation != null) {

                        reservation.reservationId = document.id

                        if (
                            reservation.entrantId == entrantId &&
                            reservation.status == "Cancelled"
                        ) {
                            loadedReservations.add(reservation)
                        }

                        if (
                            reservation.entrantId == entrantId &&
                            reservation.status == "Active"
                        ) {
                            loadedActive.add(reservation)
                        }
                    }
                }

                cancelledReservations.clear()
                cancelledReservations.addAll(loadedReservations)
                activeReservations.clear()
                activeReservations.addAll(loadedActive.sortedBy { it.startDate })

                historyError = ""
            }

        onDispose {
            listener.remove()
        }
    }
    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Reservations", homeRoute = "entree_main") },
        bottomBar = { UiBottomRow(navController = navController, tabs = entrantTabs) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.LightGray)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedTab = "Lotteries" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lotteries", fontSize = 12.sp)
                }
                Button(
                    onClick = { selectedTab = "Reservations" },
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text("Reservations", fontSize = 12.sp)
                }
                Button(
                    onClick = { selectedTab = "History" }, modifier = Modifier.weight(1f)
                ) {
                    Text("History", fontSize = 12.sp)
                }
            }
            if (selectedTab == "Reservations") {
                // the entrant's active reservations, each with Cancel (US 01.07.01)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        if (cancelError.isNotBlank()) {
                            Text(cancelError, color = Color.Red)
                        }
                        if (!isLoadingHistory && activeReservations.isEmpty()) {
                            Text("No active reservations.")
                        }
                    }
                    items(activeReservations, key = { it.reservationId }) { reservation ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(12.dp)
                        ) {
                            Text(reservation.campsiteName.ifBlank { "Campsite" }, fontSize = 20.sp)
                            Text("${reservation.startDate} to ${reservation.endDate}")
                            Button(onClick = { cancelError = ""; toCancel = reservation }) {
                                Text("Cancel Reservation")
                            }
                        }
                    }
                }

            } else if (selectedTab == "Lotteries") {
                // the lotteries the entrant joined and how they went (US 01.03.01, US 01.03.02)
                val entries = lotteryRepository?.waitlistEntries
                    ?.filter { it.entrantId == entrantId }
                    ?: emptyList()

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (entries.isEmpty()) {
                        item { Text("You have not joined any lotteries.") }
                    }
                    items(entries, key = { it.entryId }) { entry ->
                        val lottery = lotteryRepository?.getLottery(entry.lotteryId)
                        val names = lottery?.let { l ->
                            (listOf(l.campsiteId) + l.campsiteIds).filter { it.isNotBlank() }.distinct()
                                .mapNotNull { id -> campsiteRepository?.campsites?.find { it.id == id }?.name }
                                .joinToString(", ")
                        }.orEmpty().ifBlank { "Campsite" }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(12.dp)
                        ) {
                            Text(names, fontSize = 20.sp)
                            if (lottery != null) {
                                Text("${lottery.bookingStartDate} to ${lottery.bookingEndDate}")
                            }
                            Text("Status: ${entrantLotteryStatus(entry.status, lottery?.drawCompleted == true)}")
                        }
                    }
                }
            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    item {

                        if (isLoadingHistory) {
                            Text("Loading booking history...")
                        }

                        if (historyError.isNotBlank()) {
                            Text(
                                text = historyError,
                                color = Color.Red
                            )
                        }

                        if (
                            !isLoadingHistory &&
                            historyError.isBlank() &&
                            cancelledReservations.isEmpty()
                        ) {
                            Text("No cancelled reservations.")
                        }
                    }

                    items(
                        items = cancelledReservations,
                        key = { reservation -> reservation.reservationId }
                    ) { reservation ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(12.dp)
                        ) {

                            // uses the saved name even after the campsite is deleted.
                            if (reservation.campsiteName.isNotBlank()) {

                                Text(
                                    text = reservation.campsiteName,
                                    fontSize = 20.sp
                                )

                            } else {

                                Text("Campsite: ${reservation.campsiteId}")
                            }

                            Text("Start date: ${reservation.startDate}")
                            Text("End date: ${reservation.endDate}")
                            Text("Status: ${reservation.status}")
                        }
                    }
                }
            }
        }
    }

    toCancel?.let { reservation ->
        AlertDialog(
            onDismissRequest = { toCancel = null },
            title = { Text("Cancel reservation?") },
            text = { Text("${reservation.campsiteName.ifBlank { "Campsite" }}, ${reservation.startDate} to ${reservation.endDate}") },
            confirmButton = {
                TextButton(onClick = {
                    toCancel = null
                    Repositories.reservations.cancelReservation(reservation.reservationId, entrantId)
                        .addOnFailureListener { cancelError = it.message ?: "Could not cancel the reservation." }
                }) { Text("Cancel Reservation") }
            },
            dismissButton = {
                TextButton(onClick = { toCancel = null }) { Text("Keep") }
            }
        )
    }
}
