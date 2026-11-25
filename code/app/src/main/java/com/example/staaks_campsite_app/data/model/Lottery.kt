package com.example.staaks_campsite_app.data.model

/**
 * A lottery for one or more of an owner's campsites.
 *
 * Dates are YYYY-MM-DD strings, so comparing them as text compares them as dates.
 *
 * @property lotteryId the ID of this lottery
 * @property campsiteId the ID of the campsite offering the lottery
 * @property registrationStartDate the first day entrants can join
 * @property registrationEndDate the last day entrants can join
 * @property bookingStartDate the first day of the camping period
 * @property bookingEndDate the last day of the camping period
 * @property status the lottery's state, either Open or Closed
 * @property ownerId the ID of the owner of this lottery
 * @property campsiteIds every campsite in the lottery
 * @property waitlistCapacity null means no limit
 * @property numberOfWinners the number of winners to be chosen
 * @property winnerIds the IDs of the winners
 */
class Lottery(
    var lotteryId: String = "",
    var campsiteId: String = "",
    var registrationStartDate: String = "",
    var registrationEndDate: String = "",
    var bookingStartDate: String = "",
    var bookingEndDate: String = "",
    var status: String = "Open",
    var numberOfWinners: Int = 1,
    var registrationClosesAt: Long = 0L,
    var drawCompleted: Boolean = false,
    var drawnAt: Long = 0L,
    // new vars for lotteries - Szymon
    var ownerId: String = "",
    var campsiteIds: MutableList<String> = mutableListOf(),
    var waitlistCapacity: Int? = null,
    var winnerIds: MutableList<String> = mutableListOf(),
    var waitlistCount: Int = 0,
    var membershipVersion: Int = 0
) {
    /** true while entrants can join: open and inside the registration period */
    fun isRegistrationOpen(currentDate: String): Boolean {
        return status == "Open" &&
                !drawCompleted &&
                currentDate >= registrationStartDate &&
                currentDate <= registrationEndDate
    }

    /** true once the booking period is over */
    fun hasEnded(currentDate: String): Boolean {
        return bookingEndDate.isNotEmpty() &&
                currentDate > bookingEndDate
    }

    /** true before the registration start date */
    fun registrationNotStarted(currentDate: String): Boolean {
        return registrationStartDate.isNotEmpty() && currentDate < registrationStartDate
    }

    /** true after the registration end date */
    fun registrationEnded(currentDate: String): Boolean {
        return registrationEndDate.isNotEmpty() && currentDate > registrationEndDate
    }

    /** true for lotteries that belong under Active Lotteries */
    fun isActive(currentDate: String): Boolean {
        return (status == "Open" || status == "Drawn") && !hasEnded(currentDate)
    }

    /** true if this lottery includes the campsite (works for single-campsite lotteries too) */
    fun includesCampsite(id: String): Boolean {
        return id.isNotBlank() && (campsiteId == id || campsiteIds.contains(id))
    }

    /** true if the given booking dates overlap this lottery's booking period */
    fun bookingOverlaps(start: String, end: String): Boolean {
        return bookingStartDate <= end && start <= bookingEndDate
    }

    /** true if the date is inside the booking period (for the Lottery Win calendar) */
    fun isDateInBookingPeriod(date: String): Boolean {
        return date >= bookingStartDate && date <= bookingEndDate
    }

    /** true once the waitlist has reached its capacity (never full if there is no capacity) */
    fun isWaitlistFull(waitlistCount: Int): Boolean {
        val capacity = waitlistCapacity
        return capacity != null && waitlistCount >= capacity
    }

    /** "Waitlist: 15/30", or just "Waitlist: 15" when there is no capacity */
    fun waitlistLabel(waitlistCount: Int): String {
        val capacity = waitlistCapacity
        return if (capacity == null) "Waitlist: $waitlistCount"
        else "Waitlist: $waitlistCount/$capacity"
    }
    /**
     * checks whether the initial draw is due
     */
    fun isReadyToDraw(currentTime: Long): Boolean {
        return status == "Open" &&
                !drawCompleted &&
                numberOfWinners > 0 &&
                registrationClosesAt > 0L &&
                currentTime >= registrationClosesAt
    }
}