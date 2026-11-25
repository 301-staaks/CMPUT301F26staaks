package com.example.staaks_campsite_app.ui.shared

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.staaks_campsite_app.data.model.Campsite
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle


/**
 * A calendar that displays booked dates and available dates for a campsite.
 * @param modifier Modifier for styling.
 * @param campsite The campsite for which the calendar is being displayed.
 * */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookedCalendar(
    modifier: Modifier = Modifier,
    campsite: Campsite
) {
    val configuration = LocalConfiguration.current
    val currentLocale = remember(configuration) {
        configuration.locales[0]
    }

    val availableDates = remember(campsite.date) {
        campsite.date.mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }.toSet()
    }
    val bookedDates = remember(campsite.booked) {
        campsite.booked.mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }.toSet()
    }

    val initialMonth = remember(availableDates, bookedDates) {
        val allDates = availableDates + bookedDates
        if (allDates.isNotEmpty()) {
            YearMonth.from(allDates.minOrNull()!!)
        } else {
            YearMonth.now()
        }
    }

    var currentMonth by remember(initialMonth) { mutableStateOf(initialMonth) }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month"
                    )
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, currentLocale)} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day-of-Week Labels
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            val firstDayOffset = currentMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7

            for (rowStart in 0 until totalCells step 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val day = (rowStart + col) - firstDayOffset + 1
                        if (day in 1..daysInMonth) {
                            val currentDate = currentMonth.atDay(day)
                            val isBooked = currentDate in bookedDates
                            val isAvailable = currentDate in availableDates && !isBooked

                            val bgColor = when {
                                isBooked -> MaterialTheme.colorScheme.errorContainer
                                isAvailable -> Color(0xFF63B267).copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                            val textColor = when {
                                isBooked -> MaterialTheme.colorScheme.onErrorContainer
                                isAvailable -> Color(0xFF1B5E20)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .then(
                                        if (isAvailable) {
                                            Modifier.border(1.dp, Color(0xFF63B267), RoundedCornerShape(8.dp))
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isBooked || isAvailable) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                )
                            }
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Legend Footer
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF63B267).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFF63B267), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {}
                    Text("Available", style = MaterialTheme.typography.labelSmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {}
                    Text("Booked", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun BookedCalendarPreview() {
    val sampleCampsite = Campsite(
        id = "sample1",
        name = "Jasper Lakes Campground",
        location = "Jasper National Park, AB",
        capacity = 6,
        price = 45.0,
        date = mutableListOf(
            "2025-06-01", "2025-06-02", "2025-06-03", "2025-06-04",
            "2025-06-05", "2025-06-10", "2025-06-11", "2025-06-12",
            "2025-06-15", "2025-06-16", "2025-06-17", "2025-06-18",
            "2025-06-20", "2025-06-21", "2025-06-22"
        ),
        booked = mutableListOf(
            "2025-06-02", "2025-06-03", "2025-06-16", "2025-06-17"
        ),
        description = "Scenic campsite with lake views and nearby hiking trails."
    )
    MaterialTheme {
        BookedCalendar(
            campsite = sampleCampsite,
            modifier = Modifier.padding(16.dp)
        )
    }
}
