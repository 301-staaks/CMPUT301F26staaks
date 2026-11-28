package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staaks_campsite_app.data.LotteryRepository
import com.example.staaks_campsite_app.data.model.Lottery

// US 01.01.06 As an entrant, I want to join an active lottery from the campsite details screen.
@Composable
fun EntrantLotteryJoin (
    campsiteId: String,
    lotteryRepository: LotteryRepository,
    entrantId: String
) {
    val today = todayString()
    val lottery = findLotteryToShow(lotteryRepository.getLotteriesForCampsite(campsiteId), today)
    var joining by remember(campsiteId) { mutableStateOf(false) }
    var leaving by remember(campsiteId) { mutableStateOf(false) }
    var message by remember(campsiteId) { mutableStateOf<String?>(null) }
    var isErrorMessage by remember(campsiteId) { mutableStateOf(false) }
    var confirmLeave by remember(campsiteId) { mutableStateOf(false) }

    if (lottery == null) return

    Box(
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
            .background(color = Color(0xFFF1F8E9), shape = RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Reservation Draw",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            if (lottery.registrationNotStarted(today) == true) {
                Text(text = "Opens on ${lottery.registrationStartDate}", fontSize = 14.sp)
            } else {
                // read from the repository, so this flips to "Entered" as soon as the
                // waitlist entry shows up
                val alreadyEntered =
                    lotteryRepository.hasEntrantAlreadyJoined(lottery.lotteryId, entrantId)

                Text(
                    text = "Registration is open until ${lottery.registrationEndDate}.",
                    fontSize = 14.sp
                )

                Button(
                    onClick = {
                        if (alreadyEntered) { // leave lottery
                            confirmLeave = true
                        }
                        else { // enter lottery
                            joining = true
                            message = null
                            lotteryRepository.addEntrantToWaitlist(
                                lotteryId = lottery.lotteryId,
                                entrantId = entrantId,
                                joinedDate = today,
                                onSuccess = {
                                    joining = false
                                    isErrorMessage = false
                                    message = "You have been added to the waitlist."
                                },
                                onFailure = { reason ->
                                    joining = false
                                    isErrorMessage = true
                                    message = reason
                                }
                            )
                        }
                    },
                    enabled = !joining && !leaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alreadyEntered) Color(0xFFB71C1C) else Color(0xFF1B5E20),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        when {
                            leaving -> "Leaving..."
                            joining -> "Entering..."
                            alreadyEntered -> "Leave lottery"
                            else -> "Enter reservation draw"
                        }
                    )
                }

                message?.let { text ->
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        color = if (isErrorMessage) MaterialTheme.colorScheme.error
                        else Color(0xFF1B5E20)
                    )
                }
            }
        }
    }

    if (confirmLeave) {
        LeaveLotteryDialog(
            onConfirm = {
                confirmLeave = false
                leaving = true
                message = null
                lotteryRepository.removeEntrantFromWaitlist(
                    lotteryId = lottery.lotteryId,
                    entrantId = entrantId,
                    onSuccess = {
                        leaving = false
                        isErrorMessage = false
                        message = "You have been removed from the waitlist."
                    },
                    onFailure = { reason ->
                        leaving = false
                        isErrorMessage = true
                        message = reason
                    }
                )
            },
            onDismiss = { confirmLeave = false }
        )
    }
}


@Composable
fun LeaveLotteryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave lottery waitlist?") },
        text = {
            Text("Are you sure you want to leave the lottery waitlist?")
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Leave") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


private fun findLotteryToShow(lotteries: List<Lottery>, today: String): Lottery? {
    val openLotteries = lotteries.filter { it.status == "Open" }

    val running = openLotteries
        .filter { it.isRegistrationOpen(today) }
        .minByOrNull { it.registrationEndDate }
    if (running != null) {
        return running
    }

    return openLotteries
        .filter { it.registrationNotStarted(today) }
        .minByOrNull { it.registrationStartDate }
}