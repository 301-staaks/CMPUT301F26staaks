package com.example.staaks_campsite_app.data.model

/**
 * stores a notification sent to a user
 *
 * @property notificationId the ID of this notification
 * @property senderId the ID of the user who sent it
 * @property recipientId the ID of the user receiving it
 * @property campsiteId the ID of the campsite the notification is about
 * @property title the title shown on the notification
 * @property message the text of the notification
 * @property sentDate the date it was sent
 * @property isRead whether the user has read i
 */
class Notification(
    var notificationId: String = "",
    var senderId: String = "",
    var recipientId: String = "",
    var campsiteId: String = "",
    var title: String = "",
    var message: String = "",
    var sentDate: String = "",
    var isRead: Boolean = false
) {
    /**
     * marks this notification as read
     */
    fun markAsRead() {
        isRead = true
    }
}