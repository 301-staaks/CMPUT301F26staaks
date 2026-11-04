package com.example.staaks_campsite_app

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        //Pinned Top Row

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()            // Makes the row span the full width
                .background(color = Color(0xFF1B5E20))
                .padding(16.dp)

        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Button(
                    onClick = {
                        navController.navigate("admin_main")

                    },

                    ) {
                    Text("Home")
                }
            }


            Text(
                text = "Images",
                fontSize = 25.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {

            }
        }


        if (true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()            // Makes the row span the full width
                    .background(color = Color(0xFF63B267))
                    .heightIn(max = 45.dp)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly

            ) {
                Text(
                    text = "View:",
                    fontSize = 15.sp,
                    color = Color.White
                )
                Button(onClick = { navController.navigate("admin_main") }) { Text("Admin") }
                Button(onClick = { /* ... */ }) { Text("Owner") }
                Button(onClick = { /* ... */ }) { Text("Entree") }


            }
        }







        Box(
            modifier = Modifier
                .weight(1f)

        ) {
            /*
            -------------------------------------------------------------------------------------------------------------------------------------
            Create Main Content of Page Here
            -------------------------------------------------------------------------------------------------------------------------------------
             */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                                    imageUrl = image.imageUrl
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


        // Pinned Bottom Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .padding(5.dp),

            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("admin_users") },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Users",
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(1.dp))
            Button(
                onClick = { navController.navigate("all_notifications") },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.width(1.dp))
            Button(
                onClick = { navController.navigate("all_messages") },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Messages",
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(1.dp))
            Button(
                onClick = { navController.navigate("images_screen") },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Images",
                    fontSize = 10.sp
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .heightIn(max = 16.dp)
                .heightIn(min = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

        }
    }
}

/**
 * displays a photo from its url.
 *
 * shows a placeholder while loading, when the url is missing,
 * or when the image cannot be loaded.
 */
@Composable
fun CampsiteImagePreview(imageUrl: String) {

    var showPlaceholder by remember(imageUrl) {
        mutableStateOf(true)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.LightGray),

        contentAlignment = Alignment.Center
    ) {

        if (showPlaceholder) {
            Text("No photo")
        }

        if (imageUrl.startsWith("https://")) {

            AsyncImage(
                model = imageUrl,
                contentDescription = "Campsite or profile image",
                modifier = Modifier.fillMaxSize(),

                onLoading = {
                    showPlaceholder = true
                },

                onSuccess = {
                    showPlaceholder = false
                },

                onError = {
                    showPlaceholder = true
                }
            )
        }
    }
}