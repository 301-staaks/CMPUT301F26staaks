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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * displays the entrant's reservation, lottery and history tabs
 *
 * the history tab shows the current entrant's cancelled reservations
 *
 * @param navController controls navigation between screens
 */
@Composable
fun EntreeResScreen(navController: NavController) {
    val context = LocalContext.current
    val entrantId = getDeviceId(context)

    var selectedTab by remember { mutableStateOf("Reservations") }

    val cancelledReservations = remember {
        mutableStateListOf<Reservation>()
    }

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
                    }
                }

                cancelledReservations.clear()
                cancelledReservations.addAll(loadedReservations)

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Sample Reservation View")
                    }
                }

            } else if (selectedTab == "Lotteries") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Sample Lotteries View")
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
}