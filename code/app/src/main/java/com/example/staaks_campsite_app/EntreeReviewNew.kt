package com.example.staaks_campsite_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.Entree.EntreeCampsite
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.ReviewRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.Review


@Composable
fun EntreeReviewNew(
    navController: NavController,
    campsiteId: String
){
    val campRepo = remember(campsiteId) {
        CampsiteRepository()
    }

    val reviewRepository = remember { ReviewRepository() }
    val userRepository = remember { UserRepository() }
    val context = LocalContext.current
    val currentUserId = remember(context) { getDeviceId(context) }

    var rating by remember { mutableStateOf(0.0) }
    var comment by remember { mutableStateOf("") }

    // stops listening when the screen leaves
    DisposableEffect(campRepo) {
        onDispose {
            campRepo.close()
        }
    }

    Scaffold(
        topBar = {
            UiTopRow(
                navController = navController,
                title = "Review",
                homeRoute = "entree_main"
            )
        },
        bottomBar = {
            UiBottomRow(
                navController = navController,
                tabs = entrantTabs
            )
        }
    ) { innerPadding ->
        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    )
                ) { Text("Back") }

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Rating")

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically

            ){
                for (i in 1..5) {
                    Column() {
                        Text("  ${i}")
                        Text(
                            modifier = Modifier.padding(top = 2.dp)
                                .clickable { rating = i.toDouble() },
                            text = "⭐",
                            fontSize = 20.sp

                        )
                    }

                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = comment, onValueChange = { comment = it },
                    label = { Text(text = "Comment") }, singleLine = true
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = (rating > 0),
                    onClick = {
                        var newReview = Review(
                            campsiteId = campsiteId,
                            userId = currentUserId,
                            rating = rating,
                            comment = comment
                        )
                        reviewRepository.saveReview(newReview)
                        navController.popBackStack()
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    )
                ) { Text("Post") }
            }


        }
    }
}