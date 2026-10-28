package com.example.staaks_campsite_app.data.model

/**
 * stores a rating an entrant gave to a campsite
 *
 * @property ratingId the ID of this rating
 * @property campsiteId the ID of the campsite being rated
 * @property entrantId the ID of the entrant who gave the rating
 * @property score the rating from 1 to 5
 * @property comment any comments the entrant added
 * @property submittedDate the date the rating was submitted
 */
class Rating(
    var ratingId: String = "",
    var campsiteId: String = "",
    var entrantId: String = "",
    var score: Int = 0,
    var comment: String = "",
    var submittedDate: String = ""
) {
    /**
     * checks if the score is between 1 and 5
     *
     * @return true if the score is in the allowed range
     */
    fun isValid(): Boolean {
        return score >= 1 && score <= 5
    }
}