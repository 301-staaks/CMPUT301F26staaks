package com.example.staaks_campsite_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.session.SessionManager
import com.example.staaks_campsite_app.data.model.Lottery

// data displayed on one lottery card
data class LotteryCardUi(
    val id: String,
    val campsiteNames: List<String>,
    val registrationStart: String,
    val registrationEnd: String,
    val bookingStart: String,
    val bookingEnd: String,
    val numberOfWinners: Int,
    val waitlistCount: Int,
    val waitlistCapacity: Int?,
    val statusText: String
)

// converts a lottery into the data displayed on its card
fun Lottery.toCardUi(
    campsiteNames: List<String>,
    waitlistCount: Int,
    today: String
): LotteryCardUi {
    val statusText = when {
        status == "Drawn" -> "Winners drawn"
        registrationNotStarted(today) -> "Opens on $registrationStartDate"
        registrationEnded(today) -> "Registration closed"
        else -> "Registration open"
    }

    return LotteryCardUi(
        id = lotteryId,
        campsiteNames = campsiteNames,
        registrationStart = registrationStartDate,
        registrationEnd = registrationEndDate,
        bookingStart = bookingStartDate,
        bookingEnd = bookingEndDate,
        numberOfWinners = numberOfWinners,
        waitlistCount = waitlistCount,
        waitlistCapacity = waitlistCapacity,
        statusText = statusText
    )
}

private val lotteryTabs = listOf(
    Tab("owner_main", "Main", Icons.Default.Home),
    Tab("owner_reservations_active", "Reservations", Icons.Default.CalendarMonth),
    Tab("owner_notifications", "Notifications", Icons.Default.Notifications),
    Tab("owner_messages", "Messages", Icons.Default.Textsms),
    Tab("owner_lotteries", "Lotteries", Icons.Default.ConfirmationNumber)
)
/**
 * displays the owner's Lotteries screen and the Active Lotteries list
 *
 * lets the owner start creating a lottery, open past lotteries, and edit a lottery
 *
 * @param navController controls navigation between screens
 * @param lotteries the owner's active lotteries, provided by CampsiteApp
 * @param isLoading whether lottery data is still loading
 * @param loadError the loading error message or null when there is no error
 */
@Composable
fun OwnerLotteries(
    navController: NavController,
    lotteries: List<LotteryCardUi>,
    isLoading: Boolean,
    loadError: String?
) {
    /*
    -------------------------------------------------------------------------------------------------------------------------------------
    Main Content of Page
    -------------------------------------------------------------------------------------------------------------------------------------
    */

    val session by SessionManager.state.collectAsState()

    Scaffold(
        topBar = {
            UiTopRow(
                navController = navController,
                title = "Lotteries",
                homeRoute = "owner_main",
                isAdmin = session?.isAdmin == true
            )
        },
        bottomBar = { UiBottomRow(navController = navController, tabs = lotteryTabs) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Button(onClick = { navController.navigate("owner_lottery_new") }) {
                        Text("+ Create Lottery")
                    }
                    Button(onClick = { navController.navigate("owner_lotteries_past") }) {
                        Text("Past Lotteries")
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(text = "Active Lotteries", fontSize = 20.sp)
                    if (isLoading) {
                        Text("Loading lotteries...")
                    }
                    loadError?.let { message ->
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                    }
                    if (!isLoading && loadError == null && lotteries.isEmpty()) {
                        Text("You have no active lotteries. Tap + Create Lottery to start one.")
                    }
                }
            }

            items(items = lotteries, key = { lottery -> lottery.id }) { lottery ->
                // "Waitlist: 15/30", or just "Waitlist: 15" when there is no capacity
                val waitlistText = lottery.waitlistCapacity
                    ?.let { "Waitlist: ${lottery.waitlistCount}/$it" }
                    ?: "Waitlist: ${lottery.waitlistCount}"

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // every campsite in the lottery
                        Text(
                            text = lottery.campsiteNames.joinToString(", "),
                            fontSize = 20.sp
                        )
                        Text(lottery.statusText)
                        Text("Registration: ${lottery.registrationStart} to ${lottery.registrationEnd}")
                        Text("Booking: ${lottery.bookingStart} to ${lottery.bookingEnd}")
                        Text("Winners: ${lottery.numberOfWinners}")
                        Text(waitlistText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                navController.navigate("owner_lottery_edit/${lottery.id}")
                            }) { Text("Edit") }
                            Button(onClick = {
                                // TODO: open the notification screen for this lottery
                                navController.navigate("owner_lottery_notification")
                            }) { Text("New Notification") }
                        }
                    }
                }
            }
        }
    }
}