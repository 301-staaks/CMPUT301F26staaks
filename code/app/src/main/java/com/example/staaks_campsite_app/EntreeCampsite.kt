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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.ConversationViewModel
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.Campsite
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import com.example.staaks_campsite_app.data.LotteryRepository
import com.example.staaks_campsite_app.EntrantLotteryJoin
import com.example.staaks_campsite_app.StoredImage
import com.example.staaks_campsite_app.data.ConversationRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.model.Conversation
import com.example.staaks_campsite_app.data.model.Message
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class CampsiteViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repo: CampsiteRepository = CampsiteRepository()

    private val campsiteId: String = checkNotNull(savedStateHandle["campsiteId"])
    val campsite: Campsite?
        get() = repo.campsites.find { it.id == campsiteId }

    fun addOrUpdateCampsite(campsite: Campsite) {
        repo.addOrUpdateCampsite(campsite)
    }
}

/**
 * Looks up the campsite matching [campsiteID] in the [CampsiteRepository] and displays its name,
 * rating, image carousel, description and details (location, capacity, price per night). If no
 * campsite matches, an empty placeholder campsite is shown instead.
 *
 * The "View Lotteries" button is not implemented yet.
 *
 * @param navController controls navigation to other screens
 * @param campsiteID the id of the campsite to display.
 * @param lotteryRepository repository for lottery data
 * @param entrantId the id of the current user
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntreeCampsite(
    navController: NavController,
    campsiteID: String,
    lotteryRepository: LotteryRepository,
    entrantId: String,
    previewMode: Boolean = false
) {

    // this whole new block is for when an entrant is seeing the campsite and the admin deletes it
    // it changes to This campsite is no longer available
    // keeps one repository while this campsite screen is open
    val campRepo = remember(campsiteID) {
        CampsiteRepository()
    }

    // stops listening when the screen leaves
    DisposableEffect(campRepo) {
        onDispose {
            campRepo.close()
        }
    }

    // waits for the campsite data before checking whether it exists
    if (campRepo.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading campsite...")
        }

        return
    }

    val loadError = campRepo.loadError

    // shows a connection error instead of saying the campsite was deleted
    if (loadError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(loadError)
        }

        return
    }

    // finds the campsite in the current list
    val campsite = campRepo.campsites.find {
        it.id == campsiteID
    }


    // hides the details and buttons if the campsite no longer exists
    if (campsite == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("This campsite is no longer available.")
        }

        return
    }
// until here
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
            IconButton(
                onClick = {
                    navController.navigate("entree_main")
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF009E60),
                    contentColor = Color.White
                )
            ){
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

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
                    StoredImage(
                        imageId = image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .maskClip(RoundedCornerShape(8.dp))
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
                    ),
                    enabled = !previewMode
                ) { Text(score.toString()) }
                if (score < 5) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        if (!previewMode) {
            EntrantLotteryJoin(
                campsiteId = campsite.id,
                lotteryRepository = lotteryRepository,
                entrantId = entrantId
            )
        } else {
            Text("Lottery joining is not available in preview mode.")
        }
    }
}