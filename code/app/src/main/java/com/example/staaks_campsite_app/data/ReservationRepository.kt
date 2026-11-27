package com.example.staaks_campsite_app.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.LotteryTime
import com.example.staaks_campsite_app.data.model.NotificationType
import com.example.staaks_campsite_app.data.model.Reservation
import com.example.staaks_campsite_app.data.model.SYSTEM_SENDER
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.time.LocalDate

/**
 * saves reservations in firestore
 *
 * we still need: check dates as well as status when showing
 * active and past reservations
 *
 * @property reservations the reservations loaded from firestore
 */

class ReservationRepository {
    private val db = Firebase.firestore
    private val reservationsRef = db.collection("reservations")

    private val _reservations = mutableStateListOf<Reservation>()

    val reservations: List<Reservation>
        get() = _reservations

    /**
     * saves a reservation in firestore
     *
     * creates an id if the reservation does not have one
     *
     * @param reservation the reservation we want to save
     */

    fun saveReservation(reservation: Reservation) {
        if (reservation.reservationId.isBlank()) {
            reservation.reservationId = reservationsRef.document().id
        }

        reservationsRef.document(reservation.reservationId)
            .set(reservation)
    }

    /**
     * finds the loaded reservations for an entrant
     *
     * @param entrantId the id of the entrant
     * @return their reservations or an empty list if none are loaded
     */

    fun getReservationsForEntrant(entrantId: String): List<Reservation> {
        val result = mutableListOf<Reservation>()

        for (reservation in _reservations) {
            if (reservation.entrantId == entrantId) {
                result.add(reservation)
            }
        }

        return result
    }

    /**
     * finds the loaded reservations for a campsite
     *
     * includes reservations with any status
     *
     * @param campsiteId the id of the campsite
     * @return its reservations or an empty list if none are loaded
     */

    fun getReservationsForCampsite(campsiteId: String): List<Reservation> {
        val result = mutableListOf<Reservation>()

        for (reservation in _reservations) {
            if (reservation.campsiteId == campsiteId) {
                result.add(reservation)
            }
        }

        return result
    }

    /**
     * finds the campsite's loaded reservations with an active status
     *
     * only checks the status
     *
     * @param campsiteId the id of the campsite
     * @return its active reservations, or an empty list if none are loaded
     */

    fun getActiveReservationsForCampsite(campsiteId: String): List<Reservation> {
        val result = mutableListOf<Reservation>()

        for (reservation in _reservations) {
            if (reservation.campsiteId == campsiteId &&
                reservation.status == "Active"
            ) {
                result.add(reservation)
            }
        }

        return result
    }

    /**
     * updates a reservation's status in firestore without replacing its other fields
     *
     * the reservation does not need to be loaded in the local list
     * the update fails if the document does not exist.
     *
     * @param reservationId the id of the reservation to update
     * @param status the new status, such as Active or Canceled
     */
    fun updateReservationStatus(
        reservationId: String,
        status: String
    ) {
        reservationsRef.document(reservationId)
            .update("status", status)
    }

    /**
     * checks if the last day of a reservation has passed
     * and updates its status accordingly
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkReservationPast() {
        for (reservation in _reservations) {
            if (reservation.status == "Active" && reservation.isPast(LocalDate.now().toString())) {
                updateReservationStatus(reservation.reservationId, "Past")
            }
        }
    }

    /**
     * confirms a lottery win as a reservation and sends the confirmation (US 01.05.01)
     *
     * the winner picks one of the lottery's campsites and their dates inside the
     * booking period. everything is saved in one transaction: the reservation, the
     * waitlist entry changing from Selected to Accepted, the days added to the
     * campsite's booked list, and the confirmation notification. it fails if any of
     * the days are already booked. the reservation id is <lotteryId>_<entrantId>,
     * so confirming twice cannot book twice.
     *
     * the reservation is saved as a map, not the Reservation object, because firestore
     * would otherwise call getEntrantName() and getCampsiteLocation() and save their results
     *
     * @param lotteryId the lottery the entrant won
     * @param entrantId the entrant confirming
     * @param campsiteId the campsite they picked from the lottery
     * @param startDate the arrival date they picked, YYYY-MM-DD
     * @param endDate the departure date they picked, YYYY-MM-DD
     * @return a task that fails with a message to show if it could not be confirmed
     */
    fun confirmReservation(
        lotteryId: String,
        entrantId: String,
        campsiteId: String,
        startDate: String,
        endDate: String
    ): Task<Unit> {
        val reservationId = lotteryReservationId(lotteryId, entrantId)
        val reservationRef = reservationsRef.document(reservationId)
        val lotteryRef = db.collection("lotteries").document(lotteryId)

        // transactions can't run queries, so find the entrant's entries first
        return db.collection("waitlist")
            .whereEqualTo("lotteryId", lotteryId)
            .whereEqualTo("entrantId", entrantId)
            .get()
            .continueWithTask { query ->
                val entryRefs = query.result.documents.map { it.reference }

                db.runTransaction { transaction ->
                    val lottery = transaction.get(lotteryRef)
                    check(lottery.exists()) { "This lottery no longer exists." }

                    val entries = entryRefs.map { transaction.get(it) }
                    val selected = entries.filter { it.getString("status") == "Selected" }
                    check(transaction.get(reservationRef).exists().not()) {
                        "This reservation is already confirmed."
                    }
                    check(selected.isNotEmpty()) { "You don't have a spot to confirm in this lottery." }

                    val lotteryCampsites = listOf(lottery.getString("campsiteId") ?: "") +
                            ((lottery.get("campsiteIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList())
                    check(campsiteId.isNotBlank() && campsiteId in lotteryCampsites) {
                        "Pick one of this lottery's campsites."
                    }
                    val campsiteRef = db.collection("campsites").document(campsiteId)
                    val campsite = transaction.get(campsiteRef)
                    check(campsite.exists()) { "This campsite no longer exists." }

                    // checked again here in case the campsite was booked after the dialog loaded
                    val booked = (campsite.get("booked") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val errors = winChoiceErrors(
                        campsiteId, startDate, endDate,
                        lottery.getString("bookingStartDate") ?: "",
                        lottery.getString("bookingEndDate") ?: "",
                        booked
                    )
                    check(errors.isEmpty()) { errors.joinToString(" ") }

                    val campsiteName = campsite.getString("name") ?: ""
                    val days = datesBetween(startDate, endDate)

                    transaction.set(
                        reservationRef,
                        mapOf(
                            "reservationId" to reservationId,
                            "campsiteId" to campsiteId,
                            "entrantId" to entrantId,
                            "startDate" to startDate,
                            "endDate" to endDate,
                            "status" to "Active",
                            "campsiteName" to campsiteName,
                            "cancelledDate" to ""
                        )
                    )
                    for (entry in selected) {
                        transaction.update(entry.reference, "status", "Accepted")
                    }
                    transaction.update(campsiteRef, "booked", FieldValue.arrayUnion(*days.toTypedArray()))
                    Repositories.notifications.addToTransaction(
                        transaction = transaction,
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
                    Unit
                }
            }
    }

    /**
     * cancels an entrant's reservation and sends the cancellation (US 01.07.01, US 01.07.02)
     *
     * saves the Cancelled status, today's date as cancelledDate and the notification
     * in one transaction, so no notification is sent if the cancellation fails.
     * only the entrant's own Active reservations can be cancelled.
     *
     * @param reservationId the reservation to cancel
     * @param entrantId the entrant cancelling it
     * @return a task that fails with a message to show if it could not be cancelled
     */
    fun cancelReservation(reservationId: String, entrantId: String): Task<Unit> {
        val reservationRef = reservationsRef.document(reservationId)

        return db.runTransaction { transaction ->
            val reservation = transaction.get(reservationRef)
            check(reservation.exists()) { "This reservation no longer exists." }
            check(reservation.getString("entrantId") == entrantId) {
                "You can only cancel your own reservation."
            }
            check(reservation.getString("status") == "Active") {
                "Only active reservations can be cancelled."
            }

            val cancelledDate = LotteryTime.today()
            val campsiteName = reservation.getString("campsiteName") ?: ""
            val campsiteId = reservation.getString("campsiteId") ?: ""
            val startDate = reservation.getString("startDate") ?: ""
            val endDate = reservation.getString("endDate") ?: ""

            // frees the days on the campsite's calendar, if the campsite still exists
            val campsiteRef = if (campsiteId.isBlank()) null else db.collection("campsites").document(campsiteId)
            val campsiteExists = campsiteRef != null && transaction.get(campsiteRef).exists()

            transaction.update(reservationRef, "status", "Cancelled", "cancelledDate", cancelledDate)
            val days = datesBetween(startDate, endDate)
            if (campsiteExists && campsiteRef != null && days.isNotEmpty()) {
                transaction.update(campsiteRef, "booked", FieldValue.arrayRemove(*days.toTypedArray()))
            }
            Repositories.notifications.addToTransaction(
                transaction = transaction,
                senderId = SYSTEM_SENDER,
                recipientIds = listOf(entrantId),
                campsiteId = campsiteId,
                campsiteName = campsiteName,
                type = NotificationType.CANCELLATION,
                title = CANCELLATION_TITLE,
                message = cancellationMessage(campsiteName, startDate, endDate, cancelledDate),
                reservationId = reservationId
            )
            Unit
        }
    }

    init {
        reservationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _reservations.clear()

            snapshot?.documents?.forEach { document ->
                val reservation = document.toObject(
                    Reservation::class.java
                )

                if (reservation != null) {
                    reservation.reservationId = document.id
                    _reservations.add(reservation)
                }
            }
        }
    }
}