package com.example.staaks_campsite_app.data

import com.example.staaks_campsite_app.data.model.WaitlistEntry

/*
 * rules for the owner's Send Notification form (US 02.10.01)
 *
 * no firestore or compose here, so the rules can be unit tested
 */

/**
 * the entrants an owner can notify for one lottery
 *
 * the waitlist is the lottery's entry list, so this is everyone who joined
 * whose account still exists. entries the draw marked Removed are skipped too
 *
 * @param entries the lottery's waitlist entries
 * @param activeUserIds the ids of every user account that exists
 * @return each entrant id once, in the order they joined
 */
fun notifiableEntrants(entries: List<WaitlistEntry>, activeUserIds: Set<String>): List<String> =
    entries
        .filter { it.status != "Removed" && it.entrantId.isNotBlank() && it.entrantId in activeUserIds }
        .sortedBy { it.joinedDate }
        .map { it.entrantId }
        .distinct()

/**
 * @param waitlist the entrants returned by [notifiableEntrants]
 * @param sendToAll true when "All waitlist entrants" is chosen
 * @param selected the entrants ticked when choosing specific ones
 * @return who the notification goes to. ticked entrants who are no longer on
 * the waitlist are dropped
 */
fun chosenRecipients(
    waitlist: List<String>,
    sendToAll: Boolean,
    selected: Set<String>
): List<String> =
    if (sendToAll) waitlist else waitlist.filter { it in selected }

/**
 * @param lotteryId the selected lottery, blank if none
 * @param recipients the result of [chosenRecipients]
 * @param message what the owner wrote
 * @return one line per missing piece, empty if the notification can be sent
 */
fun ownerNotificationErrors(
    lotteryId: String,
    recipients: List<String>,
    message: String
): List<String> {
    val errors = mutableListOf<String>()
    if (lotteryId.isBlank()) {
        errors.add("Select a lottery.")
    } else if (recipients.isEmpty()) {
        errors.add("Choose at least one entrant.")
    }
    if (message.isBlank()) {
        errors.add("Write a message.")
    }
    return errors
}

/** "Notification sent to 1 entrant." or "Notification sent to 3 entrants." */
fun sentConfirmation(count: Int): String =
    if (count == 1) "Notification sent to 1 entrant." else "Notification sent to $count entrants."
