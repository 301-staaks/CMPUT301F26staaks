package com.example.staaks_campsite_app.data

import com.example.staaks_campsite_app.data.model.Notification
import com.example.staaks_campsite_app.data.model.NotificationType
import com.example.staaks_campsite_app.data.model.SYSTEM_SENDER
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * saves and loads notifications in firestore
 *
 * saving a notification does not send a phone push notification. the entrant
 * sees it in their inbox.
 *
 * the lottery, reservation and owner code should call sendLotteryWin, sendNotSelected,
 * sendReservationConfirmed and sendOwnerMessage. they only need ids and names, so
 * they do not depend on the Lottery or WaitlistEntry models.
 *
 * the observe functions return flows. in a composable use
 * `remember(id) { repo.observeReceived(id) }.collectAsState(initial = null)`:
 * null means still loading, a failed Result means firestore returned an error.
 */
class NotificationRepository {
    private val db = Firebase.firestore
    private val notificationsRef = db.collection("notifications")

    /**
     * adds one notification per recipient to a firestore batch, without committing it
     *
     * used when notifications must be saved together with other changes, for example
     * a lottery draw saves the new waitlist statuses and the notifications at once,
     * so either everything is saved or nothing is.
     * a firestore batch holds at most 500 writes.
     *
     * @param batch the batch to add the notifications to
     * @param senderId the owner's id, or SYSTEM_SENDER for automatic notifications
     * @param recipientIds the users who receive it. duplicates are ignored
     * @param campsiteId the campsite the notification is about
     * @param campsiteName the campsite's name
     * @param type one of the NotificationType values
     * @param title the title shown on the notification
     * @param message the text of the notification
     * @param lotteryId the lottery it is about, if any
     * @param reservationId the reservation it is about, if any
     * @return the number of notifications added
     */
    fun addToBatch(
        batch: WriteBatch,
        senderId: String,
        recipientIds: List<String>,
        campsiteId: String,
        campsiteName: String,
        type: String,
        title: String,
        message: String,
        lotteryId: String = "",
        reservationId: String = ""
    ): Int {
        val notifications = newNotifications(
            senderId, recipientIds, campsiteId, campsiteName,
            type, title, message, lotteryId, reservationId
        )
        for ((reference, notification) in notifications) {
            batch.set(reference, notification)
        }
        return notifications.size
    }

    /**
     * same as [addToBatch], for code that saves its changes in a firestore transaction,
     * like confirming or cancelling a reservation
     *
     * @return the number of notifications added
     */
    fun addToTransaction(
        transaction: Transaction,
        senderId: String,
        recipientIds: List<String>,
        campsiteId: String,
        campsiteName: String,
        type: String,
        title: String,
        message: String,
        lotteryId: String = "",
        reservationId: String = ""
    ): Int {
        val notifications = newNotifications(
            senderId, recipientIds, campsiteId, campsiteName,
            type, title, message, lotteryId, reservationId
        )
        for ((reference, notification) in notifications) {
            transaction.set(reference, notification)
        }
        return notifications.size
    }

    // makes one notification per recipient, all sharing one batchId
    private fun newNotifications(
        senderId: String,
        recipientIds: List<String>,
        campsiteId: String,
        campsiteName: String,
        type: String,
        title: String,
        message: String,
        lotteryId: String,
        reservationId: String
    ): List<Pair<DocumentReference, Notification>> {
        val recipients = recipientIds.filter { it.isNotBlank() }.distinct()
        if (recipients.isEmpty()) {
            return emptyList()
        }

        val batchId = notificationsRef.document().id
        val now = Timestamp.now()

        return recipients.map { recipientId ->
            val reference = notificationsRef.document()
            reference to Notification(
                notificationId = reference.id,
                senderId = senderId,
                recipientId = recipientId,
                campsiteId = campsiteId,
                campsiteName = campsiteName,
                type = type,
                title = title,
                message = message,
                lotteryId = lotteryId,
                reservationId = reservationId,
                batchId = batchId,
                sentAt = now
            )
        }
    }

    /**
     * sends the same notification to every recipient
     *
     * the parameters are the same as [addToBatch]
     *
     * @return a task that succeeds once every notification is saved,
     * or fails if there are no recipients
     */
    fun send(
        senderId: String,
        recipientIds: List<String>,
        campsiteId: String,
        campsiteName: String,
        type: String,
        title: String,
        message: String,
        lotteryId: String = "",
        reservationId: String = ""
    ): Task<Void> {
        if (recipientIds.none { it.isNotBlank() }) {
            return Tasks.forException(IllegalArgumentException("No recipients were given."))
        }
        val batch = db.batch()
        addToBatch(
            batch, senderId, recipientIds, campsiteId, campsiteName,
            type, title, message, lotteryId, reservationId
        )
        return batch.commit()
    }

    /**
     * tells entrants they won a lottery (US 01.03.01)
     *
     * @param entrantIds the winners
     * @param campsiteId the campsite the lottery is for
     * @param campsiteName the campsite's name, shown in the inbox
     * @param lotteryId the lottery they won, so the inbox can open it later
     */
    fun sendLotteryWin(
        entrantIds: List<String>,
        campsiteId: String,
        campsiteName: String,
        lotteryId: String
    ): Task<Void> = send(
        senderId = SYSTEM_SENDER,
        recipientIds = entrantIds,
        campsiteId = campsiteId,
        campsiteName = campsiteName,
        type = NotificationType.WIN,
        title = WIN_TITLE,
        message = winMessage(campsiteName),
        lotteryId = lotteryId
    )

    /**
     * tells entrants they were not selected in a lottery (US 01.03.02)
     *
     * @param entrantIds the entrants who were not selected
     * @param campsiteId the campsite the lottery is for
     * @param campsiteName the campsite's name, shown in the inbox
     * @param lotteryId the lottery that was drawn
     */
    fun sendNotSelected(
        entrantIds: List<String>,
        campsiteId: String,
        campsiteName: String,
        lotteryId: String
    ): Task<Void> = send(
        senderId = SYSTEM_SENDER,
        recipientIds = entrantIds,
        campsiteId = campsiteId,
        campsiteName = campsiteName,
        type = NotificationType.NOT_SELECTED,
        title = NOT_SELECTED_TITLE,
        message = notSelectedMessage(campsiteName),
        lotteryId = lotteryId
    )

    /**
     * tells an entrant their reservation is confirmed (US 01.05.01)
     *
     * call this only after the reservation was saved, so no confirmation is sent
     * for a reservation that failed
     *
     * @param entrantId the entrant who reserved
     * @param campsiteId the campsite reserved
     * @param campsiteName the campsite's name
     * @param startDate the arrival date, YYYY-MM-DD
     * @param endDate the departure date, YYYY-MM-DD
     * @param lotteryId the lottery the reservation came from, if any
     * @param reservationId the reservation that was created
     */
    fun sendReservationConfirmed(
        entrantId: String,
        campsiteId: String,
        campsiteName: String,
        startDate: String,
        endDate: String,
        lotteryId: String = "",
        reservationId: String = ""
    ): Task<Void> = send(
        senderId = SYSTEM_SENDER,
        recipientIds = listOf(entrantId),
        campsiteId = campsiteId,
        campsiteName = campsiteName,
        type = NotificationType.CONFIRMATION,
        title = CONFIRMATION_TITLE,
        message = confirmationMessage(campsiteName, startDate, endDate),
        lotteryId = lotteryId,
        reservationId = reservationId
    )

    /**
     * adds a confirmation notification to a batch without committing it (US 01.05.01)
     *
     * for whoever builds the confirm flow: add the reservation to the same batch,
     * then commit once, so the confirmation is only sent if the reservation saves.
     *
     *     val batch = Firebase.firestore.batch()
     *     batch.set(reservationRef, reservation)
     *     Repositories.notifications.addReservationConfirmed(batch, ...)
     *     batch.commit()
     *
     * the parameters are the same as [sendReservationConfirmed]
     *
     * @return the number of notifications added, 0 if entrantId is blank
     */
    fun addReservationConfirmed(
        batch: WriteBatch,
        entrantId: String,
        campsiteId: String,
        campsiteName: String,
        startDate: String,
        endDate: String,
        lotteryId: String = "",
        reservationId: String = ""
    ): Int = addToBatch(
        batch = batch,
        senderId = SYSTEM_SENDER,
        recipientIds = listOf(entrantId),
        campsiteId = campsiteId,
        campsiteName = campsiteName,
        type = NotificationType.CONFIRMATION,
        title = CONFIRMATION_TITLE,
        message = confirmationMessage(campsiteName, startDate, endDate),
        lotteryId = lotteryId,
        reservationId = reservationId
    )

    /**
     * adds a cancellation notification to a batch without committing it (US 01.07.02)
     *
     * for whoever builds the cancel flow (US 01.07.01): put the status change in the
     * same batch and commit once, so no notification is sent if the cancellation
     * does not save.
     *
     *     val batch = Firebase.firestore.batch()
     *     batch.update(reservationRef, "status", "Cancelled")
     *     Repositories.notifications.addReservationCancelled(batch, ...)
     *     batch.commit()
     *
     * @param batch the batch to add the notification to
     * @param entrantId the entrant whose reservation was cancelled
     * @param campsiteId the campsite that was reserved
     * @param campsiteName the campsite's name
     * @param startDate the original arrival date, YYYY-MM-DD
     * @param endDate the original departure date, YYYY-MM-DD
     * @param cancelledDate the day it was cancelled, YYYY-MM-DD
     * @param reservationId the reservation that was cancelled
     * @return the number of notifications added, 0 if entrantId is blank
     */
    fun addReservationCancelled(
        batch: WriteBatch,
        entrantId: String,
        campsiteId: String,
        campsiteName: String,
        startDate: String,
        endDate: String,
        cancelledDate: String,
        reservationId: String = ""
    ): Int = addToBatch(
        batch = batch,
        senderId = SYSTEM_SENDER,
        recipientIds = listOf(entrantId),
        campsiteId = campsiteId,
        campsiteName = campsiteName,
        type = NotificationType.CANCELLATION,
        title = CANCELLATION_TITLE,
        message = cancellationMessage(campsiteName, startDate, endDate, cancelledDate),
        reservationId = reservationId
    )

    /**
     * tells an entrant their reservation was cancelled (US 01.07.02)
     *
     * call this only after the cancellation was saved. prefer [addReservationCancelled]
     * so both save together
     *
     * the parameters are the same as [addReservationCancelled]
     */
    fun sendReservationCancelled(
        entrantId: String,
        campsiteId: String,
        campsiteName: String,
        startDate: String,
        endDate: String,
        cancelledDate: String,
        reservationId: String = ""
    ): Task<Void> = send(
        senderId = SYSTEM_SENDER,
        recipientIds = listOf(entrantId),
        campsiteId = campsiteId,
        campsiteName = campsiteName,
        type = NotificationType.CANCELLATION,
        title = CANCELLATION_TITLE,
        message = cancellationMessage(campsiteName, startDate, endDate, cancelledDate),
        reservationId = reservationId
    )

    /**
     * sends an owner's message to entrants (US 02.10.01)
     *
     * fails without saving anything if there are no recipients or the message is blank
     *
     * @param ownerId the owner sending it
     * @param recipientIds the entrants chosen from the waitlist
     * @param campsiteId the campsite the lottery is for
     * @param campsiteName the campsite's name
     * @param title the title, or blank to use "Message from <campsite>"
     * @param message what the owner wrote
     * @param lotteryId the lottery whose waitlist was messaged
     */
    fun sendOwnerMessage(
        ownerId: String,
        recipientIds: List<String>,
        campsiteId: String,
        campsiteName: String,
        title: String,
        message: String,
        lotteryId: String = ""
    ): Task<Void> {
        if (message.isBlank()) {
            return Tasks.forException(IllegalArgumentException("The message is empty."))
        }
        return send(
            senderId = ownerId,
            recipientIds = recipientIds,
            campsiteId = campsiteId,
            campsiteName = campsiteName,
            type = NotificationType.OWNER,
            title = ownerTitle(title, campsiteName),
            message = message.trim(),
            lotteryId = lotteryId
        )
    }

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
     * marks a notification as read without changing its other fields
     *
     * @param notificationId the id of the notification
     */
    fun markAsRead(notificationId: String) {
        notificationsRef.document(notificationId)
            .update("isRead", true)
    }

    /**
     * removes notifications from the recipient's inbox
     *
     * they are hidden, not deleted, so the admin log still shows them
     *
     * @param notificationIds the notifications to clear
     */
    fun clearAll(notificationIds: List<String>): Task<Void> {
        val batch = db.batch()
        for (id in notificationIds) {
            batch.update(notificationsRef.document(id), "cleared", true)
        }
        return batch.commit()
    }

    /**
     * @param recipientId the user receiving the notifications
     * @return the notifications sent to this user, updated live
     */
    fun observeReceived(recipientId: String): Flow<Result<List<Notification>>> =
        observe(notificationsRef.whereEqualTo("recipientId", recipientId))

    /**
     * @param senderId the user who sent the notifications
     * @return the notifications sent by this user, updated live
     */
    fun observeSent(senderId: String): Flow<Result<List<Notification>>> =
        observe(notificationsRef.whereEqualTo("senderId", senderId))

    /**
     * @return every notification in the app, updated live. used by the admin log
     */
    fun observeAll(): Flow<Result<List<Notification>>> =
        observe(notificationsRef)

    // sorting happens in the app, so these queries do not need a firestore index
    private fun observe(query: Query): Flow<Result<List<Notification>>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            val notifications = mutableListOf<Notification>()
            snapshot?.documents?.forEach { document ->
                val notification = document.toObject(Notification::class.java)
                if (notification != null) {
                    notification.notificationId = document.id
                    notifications.add(notification)
                }
            }

            trySend(Result.success(notifications))
        }

        awaitClose { registration.remove() }
    }
}
