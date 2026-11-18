package com.example.staaks_campsite_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.ReservationRepository
import com.example.staaks_campsite_app.data.model.Reservation


/**
 * displays the reservation screen
 * @param userId the id of the user
 * @param userType the type of user
 * @param showScreen the type of reservation to show
 * @param onClicked the function to call when the campsite is clicked
 * @param onMessage the function to call when the message button is pressed
 * @param onReservation the function to call when the reservation button is pressed
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationScreen(
    userId: String,
    userType: String,
    showScreen: String = "active",

    onClicked: (reservation: Reservation) -> Unit = {},
    onMessage: (reservation: Reservation) -> Unit,
    onReservation: (reservation: Reservation) -> Unit
) {
    val reservationRepository = remember { ReservationRepository() }
    val campsites = remember { CampsiteRepository().campsites }
    var showScreen by remember { mutableStateOf(showScreen) }

    // check if the reservations are past
    reservationRepository.checkReservationPast()

    Column(modifier = Modifier.fillMaxWidth()) {
        /*
        // switch between active, past, and cancelled reservations
        if (userType == "Owner") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { showScreen = "active" }) {
                    Text("Active")
                }
                Button(onClick = { showScreen = "past" }) {
                    Text("Past")
                }
                Button(onClick = { showScreen = "cancelled" }) {
                    Text("Cancelled")
                }
            }
        } else if (userType == "Entrant") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { showScreen = "active" }) {
                    Text("Active")
                }
                Button(onClick = { showScreen = "past" }) {
                    Text("Past")
                }
            }
        }

         */

        val filteredReservations = run {
            val list = mutableListOf<Reservation>()
            if (userType == "Owner") {
                val ownerCampsiteIds = campsites.filter { it.ownerId == userId }.map { it.id }
                for (res in reservationRepository.reservations) {
                    if (res.campsiteId in ownerCampsiteIds) {
                        when (showScreen) {
                            "active" -> if (res.status == "Active") list.add(res)
                            "past" -> if (res.status == "Past") list.add(res)
                            "cancelled" -> if (res.status == "Cancelled") list.add(res)
                        }
                    }
                }
            } else if (userType == "Entrant") {
                for (res in reservationRepository.reservations) {
                    if (res.entrantId == userId) {
                        when (showScreen) {
                            "active" -> if (res.status == "Active") list.add(res)
                            "past" -> if (res.status == "Past" || res.status == "Cancelled") list.add(res)
                        }
                    }
                }
            }
            list
        }

        when (showScreen) {
            "active" -> activeReservationScreen(userType, filteredReservations, onClicked, onMessage, onReservation)
            "past" -> pastReservationScreen(userType, filteredReservations, onClicked, onMessage, onReservation)
            "cancelled" -> cancelledReservationScreen(userType, filteredReservations, onClicked, onMessage, onReservation)
        }
    }
}

@Composable
fun activeReservationScreen(
    userType: String,
    reservations: List<Reservation>,
    onClicked: (reservation: Reservation) -> Unit = {},
    onMessage: (reservation: Reservation) -> Unit = {},
    onReservation: (reservation: Reservation) -> Unit = {}
) {
    Column() {
        Row(){
            //TODO("add a calendar view")
        }

        if (reservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No active reservations.")
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(
                items = reservations,
                key = { reservation -> reservation.reservationId }
            ) { reservation ->
                reservationCard(
                    userType = userType,
                    reservation = reservation,
                    isActive = true,
                    onMessage = { onMessage(reservation) },
                    onReservation = { onReservation(reservation) },
                    onClicked = { onClicked(reservation) }
                )
            }
        }
    }
}

@Composable
fun pastReservationScreen(
    userType: String,
    reservations: List<Reservation>,
    onClicked: (reservation: Reservation) -> Unit = {},
    onMessage: (reservation: Reservation) -> Unit = {},
    onReservation: (reservation: Reservation) -> Unit = {}
) {
    if (reservations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No past reservations.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            items = reservations,
            key = { reservation -> reservation.reservationId }
        ) { reservation ->
            reservationCard(
                userType = userType,
                reservation = reservation,
                isActive = false,
                onMessage = { onMessage(reservation) },
                onReservation = { onReservation(reservation) },
                onClicked = { onClicked(reservation) }
            )
        }
    }
}

@Composable
fun cancelledReservationScreen(
    userType: String,
    reservations: List<Reservation>,
    onClicked: (reservation: Reservation) -> Unit = {},
    onMessage: (reservation: Reservation) -> Unit = {},
    onReservation: (reservation: Reservation) -> Unit = {}
) {
    if (reservations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No cancelled reservations.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            items = reservations,
            key = { reservation -> reservation.reservationId }
        ) { reservation ->
            reservationCard(
                userType = userType,
                reservation = reservation,
                isActive = false,
                onMessage = { onMessage(reservation) },
                onReservation = { onReservation(reservation) },
                onClicked = { onClicked(reservation) }
            )
        }
    }
}

@Composable
fun reservationCard(
    userType: String,
    reservation: Reservation,
    isActive: Boolean,
    onMessage: () -> Unit = {},
    onReservation: () -> Unit = {},
    onClicked: () -> Unit = {}
) {
    val isActiveStatus = reservation.status.equals("Active", ignoreCase = true)
    val badgeContainerColor = if (isActiveStatus) Color(0xFFD8F3DC) else Color(0xFFE0E0E0)
    val badgeTextColor = if (isActiveStatus) Color(0xFF1B5E20) else Color(0xFF424242)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        onClick = onClicked,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(105.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ImageNotSupported,
                    contentDescription = "Image not found",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                // TODO("Display Campsite Thumbnail image")
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reservation.campsiteName.ifBlank { "Campsite" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Box(
                        modifier = Modifier
                            .background(badgeContainerColor, shape = RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reservation.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }

                val location = reservation.getCampsiteLocation()
                Text(
                    text = "📍 $location",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )


                if (userType == "Entrant") {
                    val ownerName = reservation.getOwnerName()
                    Text(
                        text = "Owner: $ownerName",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                } else if (userType == "Owner") {
                    val entrantName = reservation.getEntrantName()
                    Text(
                        text = "Entrant: $entrantName",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "📅 ${reservation.startDate}  →  ${reservation.endDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (isActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onMessage,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Text("Message", fontSize = 11.sp, maxLines = 1)
                        }

                        Button(
                            onClick = onReservation,
                            enabled = reservation.isActive(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            val actionText = if (userType == "Owner") "Reject" else "Cancel"
                            Text(actionText, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
