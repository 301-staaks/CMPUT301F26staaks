package com.example.staaks_campsite_app.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * stores a campsite or profile photo in the "images" collection.
 *
 * the photo itself is saved as base64 jpeg text in [imageData] because we
 * can't use firebase storage. campsites and users only keep the [imageId].
 *
 * @property imageId the ID of this image record
 * @property campsiteId the ID of the campsite the image belongs to
 * @property uploadedBy the ID of the user who uploaded it
 * @property imageData the photo as base64 jpeg text, see ImageEncoding
 * @property userId the id of the user whose profile uses the image, or empty for a campsite image
 */
@IgnoreExtraProperties
class CampsiteImage(
    var imageId: String = "",
    var campsiteId: String = "",
    var uploadedBy: String = "",
    var imageData: String = "",
    var userId: String = ""
)
