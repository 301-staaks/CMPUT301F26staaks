package com.example.staaks_campsite_app.data

import com.example.staaks_campsite_app.data.model.Notification
import com.example.staaks_campsite_app.data.model.NotificationType
import com.example.staaks_campsite_app.data.model.SYSTEM_SENDER
import com.google.firebase.Timestamp

/*
 * logic for the admin notification log (US 03.08.01) that does not touch firestore,
 * kept separate so it can be unit tested
 */

/**
 * one send as shown in the admin log
 *
 * a notification sent to several users is saved as one document per recipient.
 * the documents from one send share a batchId, and this joins them back together.
 *
 * @property batchId the id shared by the documents of this send
 * @property senderId the owner's id, or SYSTEM_SENDER
 * @property campsiteName the campsite the notification is about
 * @property type one of the NotificationType values
 * @property title the title of the notification
 * @property message the text of the notification
 * @property sentAt when it was sent
 * @property recipientIds everyone who received it
 */
data class NotificationSend(
    val batchId: String,
    val senderId: String,
    val campsiteName: String,
    val type: String,
    val title: String,
    val message: String,
    val sentAt: Timestamp?,
    val recipientIds: List<String>
)

/**
 * groups per-recipient notifications back into sends, newest first
 *
 * cleared notifications are included, because clearing only hides them from the
 * entrant's inbox. notifications saved without a batchId each count as their own send.
 *
 * @param notifications every notification in the app
 * @return one entry per send
 */
fun groupIntoSends(notifications: List<Notification>): List<NotificationSend> {
    return notifications
        .groupBy { it.batchId.ifBlank { it.notificationId } }
        .map { (key, group) ->
            val first = group.first()
            NotificationSend(
                batchId = key,
                senderId = first.senderId,
                campsiteName = first.campsiteName,
                type = first.type,
                title = first.title,
                message = first.message,
                sentAt = first.sentAt,
                recipientIds = group.map { it.recipientId }.distinct()
            )
        }
        .sortedByDescending { it.sentAt }
}

/**
 * the name to show for a sender or recipient
 *
 * @param userId the user's id, or SYSTEM_SENDER
 * @param namesById the names of the users that exist, by id
 * @return "System", the user's name, or the id if the user was deleted or has no name
 */
fun logName(userId: String, namesById: Map<String, String>): String {
    if (userId == SYSTEM_SENDER) {
        return SYSTEM_SENDER
    }
    return namesById[userId]?.ifBlank { null } ?: userId
}

/**
 * shortens a recipient list for a log entry
 *
 * @param names the recipient names
 * @param shown how many names to show before "+N more"
 * @return for example "Ana, Ben, Cai +2 more"
 */
fun recipientSummary(names: List<String>, shown: Int = 3): String {
    if (names.size <= shown) {
        return names.joinToString(", ")
    }
    return names.take(shown).joinToString(", ") + " +${names.size - shown} more"
}

/**
 * @param type one of the NotificationType values
 * @return a readable label for the log
 */
fun notificationTypeLabel(type: String): String {
    return when (type) {
        NotificationType.WIN -> "Lottery win"
        NotificationType.NOT_SELECTED -> "Not selected"
        NotificationType.CONFIRMATION -> "Reservation confirmed"
        NotificationType.OWNER -> "Owner message"
        NotificationType.CANCELLATION -> "Cancellation"
        NotificationType.REJECTION -> "Rejection"
        else -> type
    }
}
