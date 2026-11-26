package com.example.staaks_campsite_app.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * uses the same registration timezone on every device
 */
object LotteryTime {
    private fun format() =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("America/Edmonton")
            isLenient = false
        }

    fun today(now: Long = System.currentTimeMillis()): String {
        return format().format(Date(now))
    }

    /**
     * includes the entire last registration day
     */
    fun closingTime(lastDay: String): Long {
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(lastDay)) {
            "Use YYYY-MM-DD"
        }

        val date = requireNotNull(format().parse(lastDay))

        require(format().format(date) == lastDay) {
            "Invalid registration end date"
        }

        val calendar = Calendar.getInstance(
            TimeZone.getTimeZone("America/Edmonton")
        )

        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        return calendar.timeInMillis
    }
}