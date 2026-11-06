package com.example.staaks_campsite_app.Entree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.Campsite

/**
 * Looks up the campsite matching [campsiteID] in the [CampsiteRepository] and displays its name,
 * rating, image carousel, description and details (location, capacity, price per night). If no
 * campsite matches, an empty placeholder campsite is shown instead.
 *
 * The "View Lotteries" button is not implemented yet.
 *
 * @param navController controls navigation to other screens
 * @param campsiteID the id of the campsite to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntreeCampsite(navController: NavController, campsiteID: String) {

    val campRepo = CampsiteRepository()
    val campsitesList = campRepo.campsites
    val campsite = campsitesList.find { it.id == campsiteID } ?: Campsite(
        id = campsiteID,
        ownerId = campsitesList.firstOrNull()?.ownerId ?: "",
        name = "Loading Campsite...",
        location = "",
        capacity = 0,
        date = mutableListOf(),
        price = 0.0,
        images = mutableListOf(),
        description = "Please wait while campsite details load."
    )

    val imageNum = campsite.images.size

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = campsite.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "⭐ ${String.format("%.1f", campsite.rating)} (${campsite.ratingCount})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
        }

        if (imageNum > 0) {
            HorizontalMultiBrowseCarousel(
                state = rememberCarouselState { imageNum },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 8.dp, bottom = 16.dp),
                preferredItemWidth = 186.dp,
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) { i ->
                if (i in campsite.images.indices) {
                    val image = campsite.images[i]
                    AsyncImage(
                        model = image,
                        contentDescription = null
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(color = Color(0xFFF1F8E9), shape = RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text(text = "📍 Location: ${campsite.location}", fontSize = 14.sp)
                Text(text = "👥 Capacity: ${campsite.capacity} guests", fontSize = 14.sp)
                Text(text = "💰 Price per Night: \$${String.format("%.2f", campsite.price)}", fontSize = 14.sp)
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text(text = campsite.description.ifBlank { "No description available." }, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Rate this Campsite!", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            for (score in 1..5) {
                Button(
                    onClick = {
                        if (campsite.id.isNotBlank()) {
                            if (campsite.rating == 0.0) {
                                campsite.rating = score.toDouble()
                            } else {
                                campsite.rating = (campsite.rating * campsite.ratingCount + score) / (campsite.ratingCount + 1)
                            }
                            campsite.ratingCount++
                            campRepo.addOrUpdateCampsite(campsite)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) { Text(score.toString()) }
                if (score < 5) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        // TODO Implement lottery joining
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {/* ### */},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B5E20),
                    contentColor = Color.White
                )
            ) { Text("View Lotteries") }
        }
    }
}