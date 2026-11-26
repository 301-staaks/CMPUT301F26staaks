package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.ReviewRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.model.Review
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviews(navController: NavController) {

    // one repository for the life of this screen (closed in onDispose below)
    val campsiteRepository = remember { CampsiteRepository() }
    val campsites = campsiteRepository.campsites


    val userRepository = remember { UserRepository() }
    val users = userRepository.users

    val reviewRepository = remember { ReviewRepository() }
    val reviews = reviewRepository.reviews


    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var screenActive by remember { mutableStateOf(true) }

    var deleteReview by remember { mutableStateOf<Review?>(null) }

    DisposableEffect(campsiteRepository) {
        screenActive = true
        onDispose {
            screenActive = false
            campsiteRepository.close()
        }
    }

    // val (not var) so it smart-casts to non-null inside lambdas


    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) showDeleteDialog = false
            },
            title = { Text("Delete Review?") },
            text = {
                Text(
                    "This will remove the review and update the campsite's rating"
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

                        reviewRepository.deleteReview(
                            review = deleteReview!!
                        )
                        isDeleting = false
                        showDeleteDialog = false

                    }
                ) { Text(if (isDeleting) "Deleting..." else "Delete") }
            }
        )
    }

    Scaffold(
        topBar = { AdminTopRow(navController = navController, title = "Reviews") },
        bottomBar = { AdminBottomRow(navController = navController) }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = reviews,
                key = { review -> review.id }
            ) { review ->

                val campsiteName = campsites.find{it.id == review.campsiteId}?.name ?: ""
                val username = users.find{it.id == review.userId}?.name ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color= Color(0xFFD8FAD0))
                        .border(width = 2.dp, color = Color.Gray)
                        .padding(4.dp)
                ) {
                    Row() {


                        Text(
                            modifier = Modifier
                                .padding(16.dp),
                            text = campsiteName,
                            fontSize = 30.sp
                        )

                        repeat(maxOf(0, review.rating.toInt())) {
                            Text(
                                modifier = Modifier.padding(top = 16.dp),
                                text = "⭐",
                                fontSize = 20.sp
                            )
                        }
                    }

                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = username,
                        fontSize = 20.sp
                    )

                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = review.comment,
                        fontSize = 15.sp
                    )

                    Button(
                        enabled = !isDeleting,
                        onClick = {
                            deleteError = null
                            showDeleteDialog = true
                            deleteReview = review

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(if (isDeleting) "Deleting..." else "Delete Review")
                    }



                }
            }
        }
    }
}