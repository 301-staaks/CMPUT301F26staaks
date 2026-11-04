package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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
     * saves a reservation with a new status
     *
     * does nothing if the reservation is not loaded
     *
     * @param reservationId the id of the reservation
     * @param status the new status such as active or canceled
     */

    fun updateReservationStatus(reservationId: String, status: String) {
        for (reservation in _reservations) {
            if (reservation.reservationId == reservationId) {
                val updatedReservation = Reservation(
                    reservationId = reservation.reservationId,
                    campsiteId = reservation.campsiteId,
                    entrantId = reservation.entrantId,
                    startDate = reservation.startDate,
                    endDate = reservation.endDate,
                    status = status
                )

                reservationsRef.document(reservationId)
                    .set(updatedReservation)

                return
            }
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