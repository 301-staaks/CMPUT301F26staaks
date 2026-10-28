package com.example.staaks_campsite_app.data.model

/**
 * stores the details of an entrant campsite reservation
 *
 * write the start and end dates as YYYY-MM-DD
 *
 * @property reservationId the ID of this reservation
 * @property campsiteId the ID of the campsite being reserved
 * @property entrantId the ID of the entrant making the reservation
 * @property startDate the first day of the stay
 * @property endDate the last day of the stay
 * @property status the reservation's status such as active or canceled
 */
class Reservation(
    var reservationId: String = "",
    var campsiteId: String = "",
    var entrantId: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var status: String = "Active"
) {
    /**
     * marks this reservation as canceled
     */
    fun cancel() {
        status = "Cancelled"
    }

    /**
     * Checks if the reservation has an active status
     *
     * @return true if the status is Active
     */
    fun isActive(): Boolean {
        return status == "Active"
    }

    /**
     * checks if the last day of the reservation has passed
     *
     * this checks the date even if the reservation was canceled
     *
     * @param currentDate the date we want to check written as YYYY-MM-DD
     * @return true if an end date is saved and is before the given date
     */
    fun isPast(currentDate: String): Boolean {
        return endDate.isNotEmpty() && endDate < currentDate
    }
}