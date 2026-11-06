package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.staaks_campsite_app.data.CampsiteRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCampsite(navController: NavController, campsiteId: String) {

    // One repository for the life of this screen (closed in onDispose below)
    val campsiteRepository = remember { CampsiteRepository() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var screenActive by remember { mutableStateOf(true) }

    DisposableEffect(campsiteRepository) {
        screenActive = true
        onDispose {
            screenActive = false
            campsiteRepository.close()
        }
    }

    // val (not var) so it smart-casts to non-null inside lambdas
    val campsite = campsiteRepository.campsites.firstOrNull { it.id == campsiteId }
    val images = campsite?.images.orEmpty()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) showDeleteDialog = false
            },
            title = { Text("Delete campsite?") },
            text = {
                Text(
                    "This will remove the campsite, close its lotteries " +
                            "and cancel its active reservations."
                )
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = { showDeleteDialog = false }
                ) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        deleteError = null

                        campsiteRepository.removeCampsiteByAdmin(
                            campsiteId = campsiteId,
                            onSuccess = {
                                if (screenActive) {
                                    isDeleting = false
                                    showDeleteDialog = false
                                    navController.navigate("admin_main") {
                                        popUpTo("admin_main") { inclusive = false }
                                        launchSingleTop = true
                                    }
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
                ) { Text(if (isDeleting) "Deleting..." else "Delete") }
            }
        )
    }

    Scaffold(
        topBar = { AdminTopRow(navController = navController, title = "Campsite") },
        bottomBar = { AdminBottomRow(navController = navController) }
    ) { innerPadding ->
        // Single scrollable column — Scaffold content must have ONE root
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navController.popBackStack() },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF009E60),
                    contentColor = Color.White
                )
            ) { Text("Back") }

            if (campsiteRepository.isLoading) {
                Text("Loading campsite...")
            }

            campsiteRepository.loadError?.let { message ->
                Text(text = message, color = Color.Red)
            }

            deleteError?.let { message ->
                Text(text = message, color = Color.Red)
            }

            if (campsite != null) {

                // Name + rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = campsite.name,
                        fontSize = 24.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${campsite.rating} Stars")
                }

                // Image carousel
                if (images.isNotEmpty()) {
                    HorizontalMultiBrowseCarousel(
                        state = rememberCarouselState { images.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        preferredItemWidth = 186.dp,
                        itemSpacing = 8.dp
                    ) { i ->
                        AsyncImage(
                            model = images[i],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .maskClip(MaterialTheme.shapes.medium)
                        )
                    }
                }

                // Description
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(12.dp)
                ) {
                    Text("Description", fontSize = 18.sp)
                    Text(campsite.description)
                }

                // Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(12.dp)
                ) {
                    Text("Details", fontSize = 18.sp)
                    Text("Location: ${campsite.location}")
                    Text("Capacity: ${campsite.capacity}")
                    Text("Price: CAD ${campsite.price} / night")
                }

                // Delete
                Button(
                    enabled = !isDeleting,
                    onClick = {
                        deleteError = null
                        showDeleteDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (isDeleting) "Deleting..." else "Delete Campsite")
                }

            } else if (
                !campsiteRepository.isLoading &&
                campsiteRepository.loadError == null &&
                !isDeleting
            ) {
                Text("This campsite is no longer available.")
            }
        }
    }
}