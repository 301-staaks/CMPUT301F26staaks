package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Rating
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * saves campsite ratings in firestore and keeps a local list updated
 *
 * uses the loaded ratings to calculate a campsite's average score
 *
 * @property ratings the ratings loaded from firestore
 */
class RatingRepository {
    private val db = Firebase.firestore
    private val ratingsRef = db.collection("ratings")

    private val _ratings = mutableStateListOf<Rating>()

    val ratings: List<Rating>
        get() = _ratings

    /**
     * saves a rating if its score is between 1 and 5
     *
     * creates an id if the rating does not have one
     * does nothing if the score is outside the allowed range
     *
     * @param rating the rating we want to save
     */
    fun saveRating(rating: Rating) {
        if (!rating.isValid()) {
            return
        }

        if (rating.ratingId.isBlank()) {
            rating.ratingId = ratingsRef.document().id
        }

        ratingsRef.document(rating.ratingId).set(rating)
    }

    /**
     * finds the loaded ratings for a campsite
     *
     * @param campsiteId the id of the campsite
     * @return its ratings, or an empty list if none are loaded
     */
    fun getRatingsForCampsite(campsiteId: String): List<Rating> {
        val result = mutableListOf<Rating>()

        for (rating in _ratings) {
            if (rating.campsiteId == campsiteId) {
                result.add(rating)
            }
        }

        return result
    }

    /**
     * calculates a campsite's average score from the loaded ratings
     *
     * @param campsiteId the id of the campsite
     * @return the average score or 0.0 if no ratings are loaded for it
     */
    fun getAverageRating(campsiteId: String): Double {
        var total = 0
        var count = 0

        for (rating in _ratings) {
            if (rating.campsiteId == campsiteId) {
                total += rating.score
                count++
            }
        }

        if (count == 0) {
            return 0.0
        }

        return total.toDouble() / count
    }

    init {
        ratingsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _ratings.clear()

            snapshot?.documents?.forEach { document ->
                val rating = document.toObject(Rating::class.java)

                if (rating != null) {
                    rating.ratingId = document.id
                    _ratings.add(rating)
                }
            }
        }
    }
}