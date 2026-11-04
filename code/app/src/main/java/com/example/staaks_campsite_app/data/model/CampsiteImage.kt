package com.example.staaks_campsite_app.data.model

/**
 * stores info about a campsite image
 * This holds the image's link, not the image file itself, didn't know how to do that
 *
 * @property imageId the ID of this image record
 * @property campsiteId the ID of the campsite the image belongs to
 * @property uploadedBy the ID of the user who uploaded it
 * @property imageUrl the link used to show the image
 */
class CampsiteImage(
    var imageId: String = "",
    var campsiteId: String = "",
    var uploadedBy: String = "",
    var imageUrl: String = ""
)