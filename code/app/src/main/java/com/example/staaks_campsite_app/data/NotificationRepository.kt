package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Notification
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * saves notifications in firestore
 *
 * the list starts empty while the data loads
 * saving a notification does not send a phone push notification
 *
 * @property notifications the notifications loaded from firestore
 */
class NotificationRepository {
    private val db = Firebase.firestore
    private val notificationsRef = db.collection("notifications")

    private val _notifications = mutableStateListOf<Notification>()

    val notifications: List<Notification>
        get() = _notifications

    /**
     * saves a notification in firestore
     *
     * creates an id if the notification does not have one
     * if the id already exists replaces its saved data
     *
     * @param notification the notification we want to save
     */
    fun saveNotification(notification: Notification) {
        if (notification.notificationId.isBlank()) {
            notification.notificationId = notificationsRef.document().id
        }

        notificationsRef.document(notification.notificationId)
            .set(notification)
    }

    /**
     * finds the loaded notifications sent by a user
     *
     * @param senderId the id of the user who sent them
     * @return their sent notifications or an empty list if none are loaded
     */
    fun getSentNotifications(senderId: String): List<Notification> {
        val result = mutableListOf<Notification>()

        for (notification in _notifications) {
            if (notification.senderId == senderId) {
                result.add(notification)
            }
        }

        return result
    }

    /**
     * finds the loaded notifications sent to a user
     *
     * @param recipientId the id of the user receiving them
     * @return their received notifications or an empty list if none are loaded
     */
    fun getReceivedNotifications(recipientId: String): List<Notification> {
        val result = mutableListOf<Notification>()

        for (notification in _notifications) {
            if (notification.recipientId == recipientId) {
                result.add(notification)
            }
        }

        return result
    }

    /**
     * @return the list of loaded notifications
     */
    fun getAllNotifications(): List<Notification> {
        return notifications
    }

    /**
     * marks a loaded notification as read and saves it in firestore
     *
     * does nothing if the notification is not loaded
     * @param notificationId the id of the notification
     */

    fun markNotificationAsRead(notificationId: String) {
        for (notification in _notifications) {
            if (notification.notificationId == notificationId) {
                val updatedNotification = Notification(
                    notificationId = notification.notificationId,
                    senderId = notification.senderId,
                    recipientId = notification.recipientId,
                    campsiteId = notification.campsiteId,
                    title = notification.title,
                    message = notification.message,
                    sentDate = notification.sentDate,
                    isRead = true
                )

                notificationsRef.document(notificationId)
                    .set(updatedNotification)

                return
            }
        }
    }

    init {
        notificationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _notifications.clear()

            snapshot?.documents?.forEach { document ->
                val notification = document.toObject(
                    Notification::class.java
                )

                if (notification != null) {
                    notification.notificationId = document.id
                    _notifications.add(notification)
                }
            }
        }
    }
}