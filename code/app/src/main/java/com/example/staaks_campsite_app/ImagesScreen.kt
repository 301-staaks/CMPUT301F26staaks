package com.example.staaks_campsite_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.CampsiteImageRepository

/**
 * displays image records and lets the admin select an image for deletion
 *
 * asks for confirmation before deleting the selected image
 *
 * @param navController controls navigation between screens
 */
@Composable
fun ImagesScreen(navController: NavController) {

    val imageRepository = remember {
        CampsiteImageRepository()
    }

    var selectedImageId by remember {
        mutableStateOf("")
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }

    var deleteError by remember {
        mutableStateOf<String?>(null)
    }

    var screenActive by remember {
        mutableStateOf(true)
    }

    DisposableEffect(imageRepository) {
        screenActive = true

        onDispose {
            screenActive = false
            imageRepository.close()
        }
    }

    val selectedImage = imageRepository.images.firstOrNull {
        it.imageId == selectedImageId
    }

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            },

            title = {
                Text("Delete image?")
            },

            text = {
                Text(
                    "The image will be removed from its campsite or profile."
                )
            },

            dismissButton = {
                TextButton(
                    enabled = !isDeleting,

                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },

            confirmButton = {
                TextButton(
                    enabled = !isDeleting,

                    onClick = {
                        isDeleting = true
                        deleteError = null

                        imageRepository.deleteImage(
                            imageId = selectedImageId,

                            onSuccess = {
                                if (screenActive) {
                                    isDeleting = false
                                    showDeleteDialog = false
                                    selectedImageId = ""
                                }
                            },

                            onError = { message ->
                                if (screenActive) {
                                    isDeleting = false
                                    showDeleteDialog = false
                                    deleteError = message
                                }
                            }
                        )
                    }
                ) {
                    if (isDeleting) {
                        Text("Deleting...")
                    } else {
                        Text("Delete")
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = { AdminTopRow(navController = navController, title = "Images") },
        bottomBar = { AdminBottomRow(navController = navController) }
    ) {
            innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (imageRepository.isLoading) {
                Text("Loading images...")
            }

            imageRepository.loadError?.let { message ->
                Text(
                    text = message,
                    color = Color.Red
                )
            }
            imageRepository.loadError?.let { message ->
                Text(
                    text = message,
                    color = Color.Red
                )
            }

            deleteError?.let { message ->
                Text(
                    text = message,
                    color = Color.Red
                )
            }
            deleteError?.let { message ->
                Text(
                    text = message,
                    color = Color.Red
                )
            }

            if (
                !imageRepository.isLoading &&
                imageRepository.loadError == null &&
                imageRepository.images.isEmpty()
            ) {
                Text("No images found.")
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = imageRepository.images,
                    key = { image -> image.imageId }
                ) { image ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            CampsiteImagePreview(
                                imageId = image.imageId,
                                imageData = image.imageData
                            )

                            Text("Image ID: ${image.imageId}")

                            if (image.campsiteId.isNotBlank()) {
                                Text("Campsite: ${image.campsiteId}")
                            }

                            if (image.userId.isNotBlank()) {
                                Text("Profile: ${image.userId}")
                            }

                            Button(
                                enabled = !isDeleting,

                                onClick = {
                                    selectedImageId = image.imageId
                                    deleteError = null
                                }
                            ) {

                                if (selectedImageId == image.imageId) {
                                    Text("Selected")
                                } else {
                                    Text("Select")
                                }
                            }
                        }
                    }
                }
            }

            Button(
                enabled = selectedImage != null && !isDeleting,

                onClick = {
                    deleteError = null
                    showDeleteDialog = true
                }
            ) {
                Text("Delete Image")
            }
        }
    }
}

/**
 * displays a campsite photo in a 160dp box.
 *
 * uses [imageData] when we already have the photo text (admin images screen),
 * otherwise loads it by [imageId]. shows "No photo" when there isn't one.
 *
 * @param imageId the id of the image document
 * @param imageData the photo as base64 text, if already loaded
 */
@Composable
fun CampsiteImagePreview(imageId: String, imageData: String = "") {

    val modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)

    val placeholder: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFD8FAD0)),
            contentAlignment = Alignment.Center
        ) {
            Text("No photo")
        }
    }

    if (imageData.isNotBlank()) {
        EncodedImage(
            cacheKey = imageId,
            imageData = imageData,
            contentDescription = "Campsite or profile image",
            modifier = modifier,
            placeholder = placeholder
        )
    } else {
        StoredImage(
            imageId = imageId,
            contentDescription = "Campsite or profile image",
            modifier = modifier,
            placeholder = placeholder
        )
    }
}

/**
 * displays a user's profile photo as a circle, or a person icon if they have none.
 *
 * @param size the width and height of the circle
 * @param imageId the user's profileImageId
 */
@Composable
fun UserProfileImagePreview(
    size: Dp = 60.dp, imageId: String) {

    StoredImage(
        imageId = imageId,
        contentDescription = "Profile Picture",
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        placeholder = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(size)
            ) {
                Image(
                    painter = ColorPainter(Color.LightGray),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Icon(Icons.Default.Person, contentDescription = "Profile Picture")
            }
        }
    )
}
