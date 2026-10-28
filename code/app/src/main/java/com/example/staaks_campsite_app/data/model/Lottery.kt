package com.example.staaks_campsite_app.data.model

/**
 * stores a campsite lottery and its registration and booking dates
 *
 * --------GUYS WRITE ALL DATES AS YYYY-MM-DD--------
 *
 * @property lotteryId the ID of this lottery
 * @property campsiteId the ID of the campsite offering the lottery
 * @property registrationStartDate the first day entrants can join
 * @property registrationEndDate the last day entrants can join
 * @property bookingStartDate the first day of the camping period
 * @property bookingEndDate the last day of the camping period
 */
class Lottery(
    var lotteryId: String = "",
    var campsiteId: String = "",
    var registrationStartDate: String = "",
    var registrationEndDate: String = "",
    var bookingStartDate: String = "",
    var bookingEndDate: String = ""
) {
    /**
     * Checks if entrants can join on the given date
     *
     * the first and last registration days are included
     * @param currentDate the date we want to check
     * @return true if the date is within the registration period
     */
    fun isRegistrationOpen(currentDate: String): Boolean {
        return currentDate >= registrationStartDate &&
                currentDate <= registrationEndDate
    }

    /**
     * checks if the camping period is over
     *
     * @param currentDate the date we want to check
     * @return true if a booking end date is saved and has already passed
     */
    fun hasEnded(currentDate: String): Boolean {
        return bookingEndDate.isNotEmpty() &&
                currentDate > bookingEndDate
    }
}