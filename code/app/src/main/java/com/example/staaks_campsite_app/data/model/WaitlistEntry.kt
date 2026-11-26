package com.example.staaks_campsite_app.data.model

/**
 * keep track of an entrant who joined a lottery waiting list
 *
 * @property entryId the ID of this waiting list entry
 * @property lotteryId the ID of the lottery the entrant joined
 * @property entrantId the ID of the entrant who joined
 * @property joinedDate the date they joined written as YYYY-MM-DD
 * @property status the current participation state
 * @property selectedAt the selection timestamp, or zero if never selected
 * @property replacementProcessed whether a released invitation was handled
 */

class WaitlistEntry(
    var entryId: String = "",
    var lotteryId: String = "",
    var entrantId: String = "",
    var joinedDate: String = "",
    var status: String = "Waiting",
    var selectedAt: Long = 0L,
    var replacementProcessed: Boolean = false
)