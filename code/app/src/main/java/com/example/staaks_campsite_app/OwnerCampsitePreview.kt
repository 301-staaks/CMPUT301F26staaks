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
import androidx.compose.material3.Scaffold
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
import com.example.staaks_campsite_app.UiBottomRow
import com.example.staaks_campsite_app.UiTopRow
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.LotteryRepository
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.ownerTabs

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
fun OwnerCampsitePreview(navController: NavController, campsiteID: String, lotteryRepository: LotteryRepository, entrantId: String) {
    Scaffold(
        topBar = { UiTopRow(navController, "Campsite Preview", "owner_main") },
        bottomBar = { UiBottomRow(navController, ownerTabs) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            EntreeCampsite(navController, campsiteID, lotteryRepository, entrantId, previewMode = true)
        }
    }
}