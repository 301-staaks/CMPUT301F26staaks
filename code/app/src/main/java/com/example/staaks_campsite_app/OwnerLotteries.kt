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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val buttonColors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    )

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("owner_lottery_new") },
                        colors = buttonColors
                    ) {
                        Text("+ Create Lottery")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("owner_lotteries_past") },
                        colors = buttonColors
                    ) {
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

            items(items = lotteries, key = { it.id }) { lottery ->
                val waitlistText = lottery.waitlistCapacity
                    ?.let { "${lottery.waitlistCount}/$it" }
                    ?: lottery.waitlistCount.toString()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF202124)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = lottery.campsiteNames.joinToString(", "),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFE6F4EC),
                                contentColor = Color(0xFF1B5E20)
                            ) {
                                Text(
                                    text = lottery.statusText,
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Registration\n${lottery.registrationStart} to ${lottery.registrationEnd}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Booking\n${lottery.bookingStart} to ${lottery.bookingEnd}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE6E6E6))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Winners: ${lottery.numberOfWinners}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "Waitlist: $waitlistText",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val actionColors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF009E60),
                                contentColor = Color.White
                            )

                            Button(
                                onClick = {
                                    navController.navigate("owner_lottery_edit/${lottery.id}")
                                },
                                colors = actionColors
                            ) {
                                Text("Edit")
                            }

                            Button(
                                onClick = {
                                    navController.navigate("owner_lottery_notification")
                                },
                                colors = actionColors
                            ) {
                                Text("New Notification")
                            }
                        }
                    }
                }
            }
        }
    }
}