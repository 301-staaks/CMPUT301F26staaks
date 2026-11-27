package com.example.staaks_campsite_app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * stores one notification sent to one user
 *
 * a notification sent to several users is saved as one document per recipient,
 * so each recipient has their own read state. all the documents from one send
 * share the same batchId, which the admin log uses to group them.
 *
 * @property notificationId the ID of this notification
 * @property senderId the ID of the user who sent it, or [SYSTEM_SENDER]
 * @property recipientId the ID of the user receiving it
 * @property campsiteId the ID of the campsite the notification is about
 * @property campsiteName the campsite name, saved so the inbox and log can show it without a lookup
 * @property type one of the [NotificationType] values
 * @property title the title shown on the notification
 * @property message the text of the notification
 * @property lotteryId the lottery this notification is about, if any
 * @property reservationId the reservation this notification is about, if any
 * @property batchId shared by every notification created by the same send
 * @property sentAt when it was sent
 * @property isRead whether the recipient has opened it
 * @property cleared whether the recipient removed it from their inbox. cleared
 * notifications still show in the admin log
 */
@IgnoreExtraProperties
class Notification(
    var notificationId: String = "",
    var senderId: String = "",
    var recipientId: String = "",
    var campsiteId: String = "",
    var campsiteName: String = "",
    var type: String = NotificationType.OWNER,
    var title: String = "",
    var message: String = "",
    var lotteryId: String = "",
    var reservationId: String = "",
    var batchId: String = "",
    var sentAt: Timestamp? = null,
    // without these annotations firestore saves this field as "read" instead of "isRead"
    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false,
    var cleared: Boolean = false
) {
    /**
     * marks this notification as read
     */
    fun markAsRead() {
        isRead = true
    }
}
