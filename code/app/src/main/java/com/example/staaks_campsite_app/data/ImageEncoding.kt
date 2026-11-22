package com.example.staaks_campsite_app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * turns photos into text so they can be saved inside a firestore document.
 *
 * we can't use firebase storage, so the photo itself lives in the "images"
 * collection as a base64 jpeg string. firestore documents max out at 1 MiB,
 * so photos are shrunk and re-compressed until the text fits under
 * [MAX_ENCODED_LENGTH].
 */
object ImageEncoding {

    /** longest side of a stored photo, in pixels */
    const val MAX_DIMENSION = 1024

    /** max base64 length, leaves room under firestore's 1 MiB document limit */
    const val MAX_ENCODED_LENGTH = 700_000

    private val QUALITIES = intArrayOf(80, 65, 50, 35)

    /**
     * reads a photo the user picked and encodes it.
     * runs slow work, call it off the main thread.
     *
     * @param context any context, used for the content resolver
     * @param uri the photo picked with the photo picker
     * @return the base64 jpeg, or null if the photo could not be read
     */
    fun encodeFromUri(context: Context, uri: Uri): String? {
        val bitmap = decodeScaled(context, uri, MAX_DIMENSION) ?: return null
        return encodeBitmap(bitmap)
    }

    /**
     * compresses a bitmap to a base64 jpeg that fits in a firestore document.
     * lowers the quality first, then the size, until it fits.
     *
     * @return the base64 jpeg, or null if it never got small enough
     */
    fun encodeBitmap(source: Bitmap): String? {
        var bitmap = scaleDown(source, MAX_DIMENSION)

        repeat(4) {
            for (quality in QUALITIES) {
                val bytes = compress(bitmap, quality)

                if (encodedLength(bytes.size) <= MAX_ENCODED_LENGTH) {
                    return Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }

            bitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * 0.75).roundToInt().coerceAtLeast(1),
                (bitmap.height * 0.75).roundToInt().coerceAtLeast(1),
                true
            )
        }

        return null
    }

    /**
     * turns stored text back into a bitmap.
     *
     * @return the bitmap, or null if the text is not a valid image
     */
    fun decode(imageData: String): Bitmap? {
        if (imageData.isBlank()) {
            return null
        }

        return try {
            val bytes = Base64.decode(imageData, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * length of the base64 text for a given number of bytes
     */
    fun encodedLength(byteCount: Int): Int {
        return ((byteCount + 2) / 3) * 4
    }

    private fun compress(bitmap: Bitmap, quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        return output.toByteArray()
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longest = max(bitmap.width, bitmap.height)

        if (longest <= maxDimension) {
            return bitmap
        }

        val scale = maxDimension.toFloat() / longest

        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
            (bitmap.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
    }

    private fun decodeScaled(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // ImageDecoder also fixes the rotation of camera photos
                val source = ImageDecoder.createSource(context.contentResolver, uri)

                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

                    val longest = max(info.size.width, info.size.height)

                    if (longest > maxDimension) {
                        val scale = maxDimension.toFloat() / longest

                        decoder.setTargetSize(
                            (info.size.width * scale).roundToInt().coerceAtLeast(1),
                            (info.size.height * scale).roundToInt().coerceAtLeast(1)
                        )
                    }
                }
            } else {
                decodeScaledLegacy(context, uri, maxDimension)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeScaledLegacy(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        return scaleDown(decoded, maxDimension)
    }
}
