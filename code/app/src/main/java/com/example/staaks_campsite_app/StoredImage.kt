package com.example.staaks_campsite_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.staaks_campsite_app.data.ImageStore

/**
 * shows a photo saved in the "images" collection by its id.
 * shows [placeholder] while loading, or if the photo is missing.
 *
 * the modifier should give it a size.
 *
 * @param imageId the id of the image document
 * @param contentDescription text for screen readers
 * @param modifier size and shape of the photo
 * @param contentScale how the photo fills its space
 * @param placeholder shown when there is no photo to draw
 */
@Composable
fun StoredImage(
    imageId: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit = { DefaultImagePlaceholder() }
) {
    val bitmap by produceState(ImageStore.cached(imageId), imageId) {
        value = ImageStore.cached(imageId)

        if (value == null) {
            value = ImageStore.loadImage(imageId)
        }
    }

    BitmapOrPlaceholder(bitmap, contentDescription, modifier, contentScale, placeholder)
}

/**
 * shows a photo from base64 text we already have,
 * like a photo the owner just picked or one on the admin images screen.
 *
 * @param cacheKey the image id, used to keep the decoded photo in memory
 * @param imageData the photo as base64 jpeg text
 */
@Composable
fun EncodedImage(
    cacheKey: String,
    imageData: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit = { DefaultImagePlaceholder() }
) {
    val bitmap by produceState(ImageStore.cached(cacheKey), cacheKey, imageData) {
        value = ImageStore.cached(cacheKey)

        if (value == null) {
            value = ImageStore.decodeAndCache(cacheKey, imageData)
        }
    }

    BitmapOrPlaceholder(bitmap, contentDescription, modifier, contentScale, placeholder)
}

@Composable
private fun BitmapOrPlaceholder(
    bitmap: ImageBitmap?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    placeholder: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            placeholder()
        }
    }
}

@Composable
fun DefaultImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8FAD0)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ImageNotSupported,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
