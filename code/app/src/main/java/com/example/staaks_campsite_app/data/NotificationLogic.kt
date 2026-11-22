package com.example.staaks_campsite_app.data

import com.example.staaks_campsite_app.data.model.Notification
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/*
 * notification text and inbox logic that does not touch firestore,
 * kept separate so it can be unit tested
 */

const val WIN_TITLE = "You won the lottery!"
const val NOT_SELECTED_TITLE = "Lottery result"
const val CONFIRMATION_TITLE = "Reservation confirmed"

/** the text of a win notification (US 01.03.01) */
fun winMessage(campsiteName: String): String =
    "You were selected for $campsiteName. Open this notification to confirm your reservation."

/** the text of a not-selected notification (US 01.03.02) */
fun notSelectedMessage(campsiteName: String): String =
    "You were not selected for $campsiteName this time, " +
            "but you may still be selected if a winner declines."

/** the text of a confirmation notification (US 01.05.01) */
fun confirmationMessage(campsiteName: String, startDate: String, endDate: String): String =
    "Your reservation at $campsiteName from $startDate to $endDate is confirmed."

/** the owner's title, or "Message from <campsite>" if they left it blank (US 02.10.01) */
fun ownerTitle(title: String, campsiteName: String): String =
    title.trim().ifBlank { "Message from $campsiteName" }

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
