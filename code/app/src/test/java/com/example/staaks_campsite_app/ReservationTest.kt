package com.example.staaks_campsite_app

import com.example.staaks_campsite_app.data.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Reservation.
 * Covers US 03.01.01: removing a campsite cancels its active reservations.
 */
class ReservationTest {

    private fun sampleReservation() = Reservation(
        reservationId = "r1",
        campsiteId = "c1",
        entrantId = "e1",
        startDate = "2027-05-21",
        endDate = "2027-05-23",
        status = "Active",
        campsiteName = "Lake Site"
    )

    @Test
    fun newReservationIsActive() {
        assertTrue(sampleReservation().isActive())
    }

    @Test
    fun cancelSetsStatusToCancelled() {
        val r = sampleReservation()
        r.cancel()
        assertEquals("Cancelled", r.status)
        assertFalse(r.isActive())
    }

    // Campsite name is kept for history after the campsite is removed
    @Test
    fun cancelKeepsCampsiteName() {
        val r = sampleReservation()
        r.cancel()
        assertEquals("Lake Site", r.campsiteName)
    }
}
