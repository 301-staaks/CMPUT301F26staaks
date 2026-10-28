package com.example.staaks_campsite_app.data.model

/**
 * Represents an individual campsite listing available in the system.
 * @property id Unique identifier assigned to the campsite listing.
 * @property owner the owner, which is a reference to a User object.
 * @property name Human-readable name of the campsite (e.g., "Two Jack Lake").
 * @property location Geographical region or park area (e.g., "Banff National Park").
 * @property capacity Maximum number of guests allowed per booking.
 * @property price Nightly fee in CAD.
 * @property date List of dates available for booking.
 * @property description Detailed description including amenities and site facilities.
 * @property rating Average user rating score.
 * @property images Optional promotional images associated with the campsite..
 */
class Campsite(
    val id: String,
    val owner: User,
    var name: String,
    var location: String,
    var capacity: Int,
    val date: MutableList<String>,
    var price: Double,
    val images: MutableList<String>? = null,
    var description: String = "",
    var rating: Double = 0.0,
) {
    fun addImage(image: String) {
        this.images?.plusAssign(image)
    }
    fun removeImage(image: String) {
        this.images?.minusAssign(image)
    }
    fun addDate(date: String) {
        this.date += date
    }
    fun removeDate(date: String) {
        this.date -= date
    }
}