package com.example.staaks_campsite_app.data.model

import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.UserRepository

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
 * @property campsiteName the saved campsite name, kept for booking history after the campsite is deleted
 * @property cancelledDate the day it was cancelled as YYYY-MM-DD, blank if it was not (US 01.07.02)
 */
class Reservation(
    var reservationId: String = "",
    var campsiteId: String = "",
    var entrantId: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var status: String = "Active",
    var campsiteName: String = "",
    var cancelledDate: String = ""
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

    /**
     * gets the name of the entrant
     * @return the name of the entrant
     */
    fun getEntrantName(): String {
        val users = UserRepository()
        for (user in users.users) {
            if (user.id == entrantId) {
                return user.name
            }
        }
        return ""
    }

    /**
     * gets the name of the Owner
     * @return the name of the Owner
     */
    fun getOwnerName(): String {
        val campsites = CampsiteRepository()
        val users = UserRepository()
        for (campsite in campsites.campsites) {
            if (campsite.id == campsiteId) {
                for (user in users.users) {
                    if (user.id == campsite.ownerId) {
                        return user.name
                    }
                }
            }
        }
        return ""
    }

    /**
     * gets the location of the campsite
     * @return the location of the campsite
     */
    fun getCampsiteLocation(): String {
        val campsites = CampsiteRepository()
        for (campsite in campsites.campsites) {
            if (campsite.id == campsiteId) {
                return campsite.location
            }
        }
        return ""
    }
}