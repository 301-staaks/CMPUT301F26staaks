package com.example.staaks_campsite_app.data

import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.staaks_campsite_app.data.model.CampsiteImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * loads and saves photos kept in the "images" collection.
 *
 * campsites keep a list of image ids and users keep a profileImageId.
 * decoded photos are cached in memory so lists don't re-download them.
 */
object ImageStore {

    private val imagesRef
        get() = Firebase.firestore.collection("images")

    // uses up to 1/8 of the app's memory, sizes are in KB
    private val cache = object : LruCache<String, ImageBitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return (value.width * value.height * 4 / 1024).coerceAtLeast(1)
        }
    }

    /**
     * checks that an id can point to an image document.
     * old campsites may still hold "https://" links, those are skipped.
     */
    fun isValidImageId(imageId: String): Boolean {
        return imageId.isNotBlank() && !imageId.contains('/')
    }

    /**
     * @return a new id for an image document
     */
    fun newImageId(): String {
        return imagesRef.document().id
    }

    /**
     * @return the photo if it's already in memory, null otherwise
     */
    fun cached(imageId: String): ImageBitmap? {
        if (imageId.isBlank()) {
            return null
        }
        return cache.get(imageId)
    }

    /**
     * removes a photo from memory, use after deleting it
     */
    fun forget(imageId: String) {
        if (imageId.isNotBlank()) {
            cache.remove(imageId)
        }
    }

    /**
     * gets a photo by its image id, from memory or firestore.
     *
     * @return the photo, or null if it is missing or broken
     */
    suspend fun loadImage(imageId: String): ImageBitmap? {
        if (!isValidImageId(imageId)) {
            return null
        }

        cached(imageId)?.let { return it }

        val imageData = fetchImageData(imageId) ?: return null

        return decodeAndCache(imageId, imageData)
    }

    /**
     * decodes photo text we already have, like on the admin images screen
     * or a photo the owner just picked.
     *
     * @param key the id used to cache the photo
     * @param imageData the base64 jpeg text
     */
    suspend fun decodeAndCache(key: String, imageData: String): ImageBitmap? {
        cached(key)?.let { return it }

        return withContext(Dispatchers.Default) {
            val bitmap = ImageEncoding.decode(imageData)?.asImageBitmap()

            if (bitmap != null && key.isNotBlank()) {
                cache.put(key, bitmap)
            }

            bitmap
        }
    }

    /**
     * saves the photos added on the campsite edit screen and deletes
     * the ones the owner removed, all in one batch.
     *
     * @param campsiteId the campsite the photos belong to
     * @param uploadedBy the id of the owner saving them
     * @param added new photos, each with an imageId and imageData
     * @param removedIds ids of saved photos the owner removed
     * @param onComplete called with true if everything saved
     */
    fun saveCampsiteImages(
        campsiteId: String,
        uploadedBy: String,
        added: List<CampsiteImage>,
        removedIds: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val batch = Firebase.firestore.batch()

        for (image in added) {
            image.campsiteId = campsiteId
            image.uploadedBy = uploadedBy
            batch.set(imagesRef.document(image.imageId), image)
        }

        for (imageId in removedIds) {
            if (isValidImageId(imageId)) {
                batch.delete(imagesRef.document(imageId))
                forget(imageId)
            }
        }

        batch.commit().addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    /**
     * replaces a user's profile photo and deletes the old one.
     *
     * @param userId the user whose photo changes
     * @param imageData the new photo as base64 jpeg text
     * @param onComplete called with true if it saved
     */
    fun setProfileImage(
        userId: String,
        imageData: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val db = Firebase.firestore
        val userReference = db.collection("users").document(userId)
        val newImageId = newImageId()

        db.runTransaction { transaction ->
            val user = transaction.get(userReference)
            val oldImageId = user.getString("profileImageId") ?: ""

            transaction.set(
                imagesRef.document(newImageId),
                CampsiteImage(
                    imageId = newImageId,
                    uploadedBy = userId,
                    imageData = imageData,
                    userId = userId
                )
            )

            transaction.update(userReference, "profileImageId", newImageId)

            if (isValidImageId(oldImageId)) {
                transaction.delete(imagesRef.document(oldImageId))
            }

            oldImageId

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                forget(task.result ?: "")
            }
            onComplete(task.isSuccessful)
        }
    }

    private suspend fun fetchImageData(imageId: String): String? {
        return suspendCancellableCoroutine { continuation ->
            imagesRef.document(imageId).get()
                .addOnSuccessListener { document ->
                    if (continuation.isActive) {
                        continuation.resume(document.getString("imageData"))
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }
    }
}
