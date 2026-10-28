package com.example.staaks_campsite_app.data.model

/**
 * keep track of an entrant who joined a lottery waiting list
 *
 * @property entryId the ID of this waiting list entry
 * @property lotteryId the ID of the lottery the entrant joined
 * @property entrantId the ID of the entrant who joined
 * @property joinedDate the date they joined written as YYYY-MM-DD
 */
class WaitlistEntry(
    var entryId: String = "",
    var lotteryId: String = "",
    var entrantId: String = "",
    var joinedDate: String = ""
)