package com.example.staaks_campsite_app.data.model

/**
 * Represents an individual review for a campsite in the system.
 * @property id Unique identifier assigned to the review.
 * @property campsiteId ID of the campsite being reviewed.
 * @property userId ID of the user who wrote the review.
 * @property rating Rating given by the user (1-5).
 * @property comment Optional written review text.
 */

data class Review(
    var id: String = "",
    var campsiteId: String = "",
    var userId: String = "",
    var rating: Double = 0.0,
    var comment: String = ""
) {
}