package com.example.staaks_campsite_app.data

/**
 * shared repository instances for the whole app
 *
 * screens use these instead of creating their own, so each firestore
 * listener is only added once
 */
object Repositories {
    val notifications by lazy { NotificationRepository() }

    /** user names for the owner Send Notification form (US 02.10.01) */
    val users by lazy { UserRepository() }

    /** confirm and cancel reservations from the inbox and Reservations tab */
    val reservations by lazy { ReservationRepository() }
}
