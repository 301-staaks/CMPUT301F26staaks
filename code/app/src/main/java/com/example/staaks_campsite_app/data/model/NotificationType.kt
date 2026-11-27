package com.example.staaks_campsite_app.data.model

/*
 * moved here from Notification.kt unchanged. this file has no firebase imports,
 * so the lottery draw worker can compile it and save the same type strings
 */

/**
 * the kinds of notifications the app sends
 *
 * stored as plain strings in firestore, like the status fields on the other models
 */
object NotificationType {
    /** the entrant won a lottery (US 01.03.01) */
    const val WIN = "WIN"

    /** the entrant was not selected in a lottery draw (US 01.03.02) */
    const val NOT_SELECTED = "NOT_SELECTED"

    /** the entrant confirmed a reservation (US 01.05.01) */
    const val CONFIRMATION = "CONFIRMATION"

    /** an owner wrote this notification by hand (US 02.10.01) */
    const val OWNER = "OWNER"

    /** reserved for the cancellation stories */
    const val CANCELLATION = "CANCELLATION"

    /** reserved for the rejection stories */
    const val REJECTION = "REJECTION"
}

/** the sender id used for notifications the app sends automatically */
const val SYSTEM_SENDER = "System"
