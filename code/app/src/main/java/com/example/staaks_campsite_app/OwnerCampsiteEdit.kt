package com.example.staaks_campsite_app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.ImageEncoding
import com.example.staaks_campsite_app.data.ImageStore
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.data.model.CampsiteImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.staaks_campsite_app.session.SessionManager


/**
 * The owner's screen for creating a new campsite or editing an existing one
 *
 * @param navController controls navigation to other screens.
 * @param isNew true when creating a new campsite, which changes the title and button text.
 * @param ownerId the id of the owner who will own a newly created campsite.
 * @param existing the campsite being edited, or null when creating a new one.
 * @param onSave called with the campsite to store when the user saves.
 */
@Composable
fun OwnerCampsiteEdit(
    navController: NavController,
    isNew: Boolean,
    ownerId: String,
    existing: Campsite? = null,
    onSave: (Campsite) -> Unit
) {
    val session by SessionManager.state.collectAsState()

    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var location by remember(existing?.id) { mutableStateOf(existing?.location ?: "") }
    var capacity by remember(existing?.id) { mutableStateOf(existing?.capacity?.toString() ?: "") }
    var price by remember(existing?.id) { mutableStateOf(existing?.price?.toString() ?: "") }
    var description by remember(existing?.id) { mutableStateOf(existing?.description ?: "") }

    // photos already saved on the campsite. old "https://" links are dropped since they can't load
    val savedImageIds = remember(existing?.id) {
        mutableStateListOf<String>().apply {
            addAll(existing?.images.orEmpty().filter { ImageStore.isValidImageId(it) })
        }
    }
    // saved photos the owner removed, deleted from firestore on save
    val removedImageIds = remember(existing?.id) { mutableStateListOf<String>() }
    // photos picked on this screen, uploaded on save
    val newImages = remember(existing?.id) { mutableStateListOf<CampsiteImage>() }

    var isProcessingPhotos by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult

        isProcessingPhotos = true
        photoError = null

        scope.launch {
            val appContext = context.applicationContext
            var failed = 0

            for (uri in uris) {
                val imageData = withContext(Dispatchers.Default) {
                    ImageEncoding.encodeFromUri(appContext, uri)
                }

                if (imageData == null) {
                    failed++
                } else {
                    newImages.add(
                        CampsiteImage(
                            imageId = ImageStore.newImageId(),
                            imageData = imageData
                        )
                    )
                }
            }

            if (failed > 0) {
                photoError = "Couldn't read $failed of the photos you picked."
            }
            isProcessingPhotos = false
        }
    }


    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Owner Main", homeRoute = "owner_main", isAdmin = session!!.isAdmin) },
        bottomBar = { UiBottomRow(navController = navController, tabs = ownerTabs) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            /*
            -------------------------------------------------------------------------------------------------------------------------------------
            Main Content of Page
            -------------------------------------------------------------------------------------------------------------------------------------
             */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Campsite Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Maximum Occupancy") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per Night") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())

                // Photos
                Text("Photos", fontSize = 18.sp)

                if (savedImageIds.isEmpty() && newImages.isEmpty()) {
                    Text("No photos yet.", color = Color.Gray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(savedImageIds.toList(), key = { it }) { imageId ->
                            PhotoThumbnail(
                                onRemove = {
                                    savedImageIds.remove(imageId)
                                    removedImageIds.add(imageId)
                                }
                            ) {
                                StoredImage(
                                    imageId = imageId,
                                    contentDescription = "Campsite photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        items(newImages.toList(), key = { it.imageId }) { image ->
                            PhotoThumbnail(onRemove = { newImages.remove(image) }) {
                                EncodedImage(
                                    cacheKey = image.imageId,
                                    imageData = image.imageData,
                                    contentDescription = "New campsite photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    enabled = !isProcessingPhotos,
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isProcessingPhotos) "Processing photos..." else "Add Photos")
                }

                photoError?.let { Text(it, color = Color.Red) }

                Button( // creates a new campsite owned by owner or updates changed values of an existing one -Szymon
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    ),
                    enabled = !isProcessingPhotos,
                    onClick = {
                        val imageIds = (savedImageIds + newImages.map { it.imageId }).toMutableList()

                        val campsiteToSave = if (existing != null) {
                            existing.copy(
                                name = name,
                                location = location,
                                capacity = capacity.toIntOrNull() ?: 0,
                                price = price.toDoubleOrNull() ?: 0.0,
                                description = description,
                                images = imageIds
                            )
                        } else {
                            Campsite(
                                ownerId = ownerId,
                                name = name,
                                location = location,
                                capacity = capacity.toIntOrNull() ?: 0,
                                price = price.toDoubleOrNull() ?: 0.0,
                                description = description,
                                images = imageIds
                            )
                        }

                        if (newImages.isNotEmpty() || removedImageIds.isNotEmpty()) {
                            // photos need the campsite id, so a new campsite gets one now
                            if (campsiteToSave.id.isBlank()) {
                                campsiteToSave.id = Firebase.firestore.collection("campsites").document().id
                            }

                            ImageStore.saveCampsiteImages(
                                campsiteId = campsiteToSave.id,
                                uploadedBy = ownerId,
                                added = newImages.toList(),
                                removedIds = removedImageIds.toList()
                            )
                        }

                        onSave(campsiteToSave)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isNew) "Create Campsite" else "Save Changes")
                }
            }
        }
    }
}

/**
 * a square photo with an X button to remove it, used on the campsite edit screen
 */
@Composable
private fun PhotoThumbnail(
    onRemove: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        content()

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                tint = Color.White
            )
        }
    }
}
