package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.staaks_campsite_app.data.model.CampsiteImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * saves campsite image details and links in firestore
 *
 * still needed: upload and delete the image pictures themselves
 *
 * @property images the image records loaded from firesxtore
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
     * deletes an image record and removes its campsite or profile references.
     *
     * updates the related documents in the same transaction.
     * clears the profile image fields only if they match this image
     *
     * @param imageId the id of the image record to delete
     * @param onSuccess the action called after the deletion succeeds
     * @param onError the action that receives an error message
     */
    fun deleteImage(
        imageId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (imageId.isBlank()) {
            onError("Select an image first.")
            return
        }

        db.runTransaction { transaction ->

            val imageReference = imagesRef.document(imageId)
            val image = transaction.get(imageReference)

            if (image.exists()) {

                val campsiteId = image.getString("campsiteId") ?: ""
                val userId = image.getString("userId") ?: ""
                val imageUrl = image.getString("imageUrl") ?: ""

                var removeProfilePhoto = false

                if (userId.isNotBlank()) {

                    val userReference =
                        db.collection("users").document(userId)

                    val user = transaction.get(userReference)

                    if (user.exists()) {

                        val profileImageId =
                            user.getString("profileImageId") ?: ""

                        val profileImageUrl =
                            user.getString("profileImageUrl") ?: ""

                        if (profileImageId == imageId) {

                            removeProfilePhoto = true

                        } else if (
                            profileImageId.isBlank() &&
                            imageUrl.isNotBlank() &&
                            profileImageUrl == imageUrl
                        ) {

                            removeProfilePhoto = true
                        }
                    }
                }

                if (campsiteId.isNotBlank()) {

                    val campsiteReference =
                        db.collection("campsites").document(campsiteId)

                    val campsite = transaction.get(campsiteReference)

                    if (campsite.exists()) {

                        val savedImages = campsite.get("images")
                        val remainingImages = mutableListOf<String>()

                        if (savedImages is List<*>) {

                            for (savedImage in savedImages) {

                                if (savedImage is String) {

                                    if (
                                        savedImage != imageId &&
                                        (imageUrl.isBlank() || savedImage != imageUrl)
                                    ) {
                                        remainingImages.add(savedImage)
                                    }
                                }
                            }
                        }

                        transaction.update(
                            campsiteReference,
                            "images",
                            remainingImages
                        )
                    }
                }

                if (removeProfilePhoto) {

                    val userReference =
                        db.collection("users").document(userId)

                    transaction.update(
                        userReference,
                        "profileImageId",
                        "",
                        "profileImageUrl",
                        ""
                    )
                }

                transaction.delete(imageReference)

                true

            } else {

                false
            }

        }.addOnCompleteListener { task ->

            if (task.isSuccessful && task.result == true) {
                onSuccess()
            } else {
                onError("Could not delete the image.")
            }
        }
    }

    var isLoading by mutableStateOf(true)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    private val listener = imagesRef.addSnapshotListener { snapshot, error ->

        isLoading = false

        if (error != null) {
            loadError = error.localizedMessage ?: "Could not load images."
            return@addSnapshotListener
        }

        if (snapshot == null) {
            loadError = "Could not load images."
            return@addSnapshotListener
        }

        val loadedImages = mutableListOf<CampsiteImage>()

        for (document in snapshot.documents) {
            val image = document.toObject(CampsiteImage::class.java)

            if (image != null) {
                image.imageId = document.id
                loadedImages.add(image)
            }
        }

        _images.clear()
        _images.addAll(loadedImages)

        loadError = null
    }
    /**
     * stops listening when the repository is no longer needed.
     */
    fun close() {
        listener.remove()
    }
}