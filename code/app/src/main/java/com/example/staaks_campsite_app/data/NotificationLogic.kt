package com.example.staaks_campsite_app.data

import com.example.staaks_campsite_app.data.model.Notification
import com.example.staaks_campsite_app.data.model.NotificationType
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/*
 * notification text and inbox logic that does not touch firestore,
 * kept separate so it can be unit tested
 */

/** the text of a confirmation notification (US 01.05.01) */
fun confirmationMessage(campsiteName: String, startDate: String, endDate: String): String =
    "Your reservation at $campsiteName from $startDate to $endDate is confirmed."

const val CANCELLATION_TITLE = "Reservation cancelled"

/** the text of a cancellation notification (US 01.07.02) */
fun cancellationMessage(
    campsiteName: String,
    startDate: String,
    endDate: String,
    cancelledDate: String
): String =
    "Your reservation at $campsiteName from $startDate to $endDate " +
            "was cancelled on $cancelledDate."

/** the owner's title, or "Message from <campsite>" if they left it blank (US 02.10.01) */
fun ownerTitle(title: String, campsiteName: String): String =
    title.trim().ifBlank { "Message from $campsiteName" }

/** the entrant's Reservations screen, which opens on its Reservations tab */
const val ENTRANT_RESERVATIONS_ROUTE = "entree_res_screen"

/**
 * true for notifications that open a dialog in the inbox: win (Confirm Reservation,
 * then Download CSV), confirmation and cancellation (Download CSV)
 */
fun opensDetail(notification: Notification): Boolean = notification.type in setOf(
    NotificationType.WIN,
    NotificationType.CONFIRMATION,
    NotificationType.CANCELLATION
)

/**
 * where tapping a notification that has no dialog goes
 *
 * not selected opens the Reservations screen (US 01.03.02). owner messages
 * already show their whole text, so they open nothing
 *
 * @param notification the notification that was tapped
 * @return the route to open, or null to stay in the inbox
 */
fun inboxRoute(notification: Notification): String? = when (notification.type) {
    NotificationType.NOT_SELECTED -> ENTRANT_RESERVATIONS_ROUTE
    else -> null
}

/**
 * @param notifications every notification sent to the entrant
 * @return the ones they have not cleared, newest first
 */
fun inboxNotifications(notifications: List<Notification>): List<Notification> {
    return notifications
        .filter { !it.cleared }
        .sortedByDescending { it.sentAt }
}

/**
 * @param sentAt when a notification was sent
 * @return the time as YYYY-MM-DD HH:mm, or an empty string if it has no time
 */
fun formatSentAt(sentAt: Timestamp?): String {
    if (sentAt == null) {
        return ""
    }
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(sentAt.toDate())
}

/**
 * the id of the reservation an entrant gets from a lottery (US 01.05.01)
 *
 * the same lottery and entrant always give the same id, so confirming twice
 * cannot create two reservations
 */
fun lotteryReservationId(lotteryId: String, entrantId: String): String = "${lotteryId}_$entrantId"

/**
 * the status the entrant sees for a lottery they joined (US 01.03.01, US 01.03.02)
 *
 * @param entryStatus the waitlist entry's status
 * @param drawCompleted whether the lottery has been drawn
 * @return Won, Confirmed, Declined, Lost (after the draw) or Pending (before it)
 */
fun entrantLotteryStatus(entryStatus: String, drawCompleted: Boolean): String = when (entryStatus) {
    "Selected" -> "Won"
    "Accepted" -> "Confirmed"
    "Declined" -> "Declined"
    else -> if (drawCompleted) "Lost" else "Pending"
}

/**
 * every day of a stay, first and last day included, as YYYY-MM-DD (US 01.05.01)
 *
 * saved in the campsite's booked list so the same days can't be reserved twice
 *
 * @param startDate the arrival date
 * @param endDate the departure date, the last day of the stay
 * @return the days in order, or an empty list if a date is invalid, the end is
 * before the start, or the stay is longer than a year
 */
fun datesBetween(startDate: String, endDate: String): List<String> {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
        isLenient = false
    }
    val start = try { format.parse(startDate) } catch (e: Exception) { null } ?: return emptyList()
    val end = try { format.parse(endDate) } catch (e: Exception) { null } ?: return emptyList()
    if (format.format(start) != startDate || format.format(end) != endDate || end.before(start)) {
        return emptyList()
    }

    val days = mutableListOf<String>()
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    calendar.time = start
    while (!calendar.time.after(end)) {
        days.add(format.format(calendar.time))
        if (days.size > 366) return emptyList()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

/**
 * checks the campsite and dates a winner picked (US 01.05.01)
 *
 * @param campsiteId the campsite picked, blank if none
 * @param startDate the arrival date picked, blank if none
 * @param endDate the departure date picked, blank if none
 * @param bookingStart the first day of the lottery's booking period
 * @param bookingEnd the last day of the lottery's booking period
 * @param booked the days already booked at the picked campsite
 * @return one line per problem, empty if the reservation can be confirmed
 */
fun winChoiceErrors(
    campsiteId: String,
    startDate: String,
    endDate: String,
    bookingStart: String,
    bookingEnd: String,
    booked: Collection<String>
): List<String> {
    val errors = mutableListOf<String>()
    if (campsiteId.isBlank()) errors.add("Pick a campsite.")
    if (startDate.isBlank()) errors.add("Pick an arrival date.")
    if (endDate.isBlank()) errors.add("Pick a departure date.")
    if (errors.isNotEmpty()) return errors

    if (endDate < startDate) {
        errors.add("The departure date must be on or after the arrival date.")
    } else if (startDate < bookingStart || endDate > bookingEnd) {
        errors.add("Pick dates from $bookingStart to $bookingEnd.")
    } else {
        val days = datesBetween(startDate, endDate)
        if (days.isEmpty()) {
            errors.add("Those dates are not valid.")
        } else {
            val taken = days.filter { it in booked }
            if (taken.isNotEmpty()) {
                errors.add("Already booked: ${taken.joinToString(", ")}.")
            }
        }
    }
    return errors
}
