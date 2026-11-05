package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.staaks_campsite_app.data.CampsiteRepository
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.staaks_campsite_app.data.model.Campsite
import kotlin.collections.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCampsite(navController: NavController, campsiteId: String) {

    val CampRepo = CampsiteRepository()
    val CampsitesList = CampRepo.campsites

    val campsiteRepository = remember {
        CampsiteRepository()
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

    DisposableEffect(campsiteRepository) {
        screenActive = true

        onDispose {
            screenActive = false
            campsiteRepository.close()
        }
    }

    // finds the campsite with the selected id
    var campsite: Campsite? = null

    for (savedCampsite in campsiteRepository.campsites) {
        if (savedCampsite.id == campsiteId) {
            campsite = savedCampsite
            break
        }
    }

    val ImageNum = campsite?.images?.size

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            },

            title = {
                Text("Delete campsite?")
            },

            text = {
                Text(
                    "This will remove the campsite, close its lotteries " +
                            "and cancel its active reservations."
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

                        campsiteRepository.removeCampsiteByAdmin(
                            campsiteId = campsiteId,

                            onSuccess = {
                                if (screenActive) {
                                    isDeleting = false
                                    showDeleteDialog = false

                                    navController.navigate("admin_main") {
                                        popUpTo("admin_main") {
                                            inclusive = false
                                        }

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
                ) {
                    Text(
                        if (isDeleting) {
                            "Deleting..."
                        } else {
                            "Delete"
                        }
                    )
                }
            }
        )
    }
    Column(

    ) {

        //Pinned Top Row

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()            // Makes the row span the full width
                .background(color = Color(0xFF1B5E20))
                .padding(16.dp)

        ){
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
                text = "Campsite",
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


        if (true){
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
                Button(onClick = {navController.navigate("admin_main")}) { Text("Admin") }
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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                if (campsiteRepository.isLoading) {
                    Text("Loading campsite...")
                }

                campsiteRepository.loadError?.let { message ->
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

                if (campsite != null) {

                    CampsiteImagePreview(
                        imageUrl = campsite.images?.firstOrNull() ?: ""
                    )

                    Text(
                        text = campsite.name,
                        fontSize = 24.sp
                    )

                    Text("Location: ${campsite.location}")

                    Text("Capacity: ${campsite.capacity}")

                    Text("Price: CAD ${campsite.price} / night")

                    Text(campsite.description)

                    Button(
                        enabled = !isDeleting,

                        onClick = {
                            deleteError = null
                            showDeleteDialog = true
                        }
                    ) {
                        Text(
                            if (isDeleting) {
                                "Deleting..."
                            } else {
                                "Delete Campsite"
                            }
                        )
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








        // Pinned Bottom Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .padding(5.dp),

            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {navController.navigate("admin_users")},
                modifier = Modifier.weight(1f)) { Text(text ="Users",
                fontSize = 10.sp) }
            Spacer(modifier = Modifier.width(1.dp))
            Button(onClick = {navController.navigate("all_notifications")},
                modifier = Modifier.weight(1f)) { Text(text ="Notifications",
                fontSize = 9.sp) }
            Spacer(modifier = Modifier.width(1.dp))
            Button(onClick = {navController.navigate("all_messages")},
                modifier = Modifier.weight(1f)) { Text(text ="Messages",
                fontSize = 10.sp) }
            Spacer(modifier = Modifier.width(1.dp))
            Button(onClick = {navController.navigate("images_screen")},
                modifier = Modifier.weight(1f)) { Text(text ="Images",
                fontSize = 10.sp) }
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