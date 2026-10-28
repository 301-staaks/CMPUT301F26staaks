package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.CampsiteImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * saves campsite image details and links in firestore
 *
 * still needed: upload and delete the image pictures themselves
 *
 * @property images the image records loaded from firestore
 */
class CampsiteImageRepository {
    private val db = Firebase.firestore
    private val imagesRef = db.collection("images")

    private val _images = mutableStateListOf<CampsiteImage>()

    val images: List<CampsiteImage>
        get() = _images

    /**
     * saves an image's details and link in firestore
     *
     * creates an id if the image record does not have one
     * does not upload the image file itself
     *
     * @param image the image details we want to save
     */
    fun saveImage(image: CampsiteImage) {
        if (image.imageId.isBlank()) {
            image.imageId = imagesRef.document().id
        }

        imagesRef.document(image.imageId).set(image)
    }

    /**
     * finds the loaded image records for a campsite
     *
     * @param campsiteId the id of the campsite
     * @return its image records or an empty list if none are loaded
     */
    fun getCampsiteImages(campsiteId: String): List<CampsiteImage> {
        val result = mutableListOf<CampsiteImage>()

        for (image in _images) {
            if (image.campsiteId == campsiteId) {
                result.add(image)
            }
        }

        return result
    }

    /**
     * gets all loaded image records
     *
     * @return the list of loaded image records
     */
    fun getAllImages(): List<CampsiteImage> {
        return images
    }

    /**
     * deletes an image record from firestore
     *
     *
     * @param imageId the id of the image record
     */
    fun deleteImage(imageId: String) {
        imagesRef.document(imageId).delete()
    }

    init {
        imagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _images.clear()

            snapshot?.documents?.forEach { document ->
                val image = document.toObject(CampsiteImage::class.java)

                if (image != null) {
                    image.imageId = document.id
                    _images.add(image)
                }
            }
        }
    }
}