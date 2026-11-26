package com.example.staaks_campsite_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.data.model.toUser
import com.example.staaks_campsite_app.session.SessionManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


val ownerTabs = listOf(
    Tab("owner_main", "Main", Icons.Default.Home),
    Tab("owner_reservations", "Reservations", Icons.Default.CalendarMonth),
    Tab("owner_notifications", "Notifications", Icons.Default.Notifications),
    Tab("owner_messages", "Messages", Icons.Default.Textsms),
    Tab("owner_lotteries", "Lotteries", Icons.Default.ConfirmationNumber)
)

/**
 * The owner's main screen, which lists the owner's campsites.
 *
 * The "Upcoming reservations" line on each card is a placeholder and is not
 * loaded from the database yet.
 *
 * @param navController controls navigation to other screens.
 * @param campsites the campsites to list, normally those owned by the current owner.
 * @param onEditCampsite called with the campsite whose "Edit" button was pressed.
 */


@Composable
fun OwnerMain(
    navController: NavController,
    campsites: List<Campsite>,
    onEditCampsite: (Campsite) -> Unit
) {
    val session by SessionManager.state.collectAsState()
    var expandedCampsite by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Home", homeRoute = "owner_main") },
        bottomBar = { UiBottomRow(navController = navController, tabs = ownerTabs) },
        floatingActionButton = {
            NewCampsiteButton(onClick = { navController.navigate(route = "owner_campsite_new") })
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            // darker 0xFF1B5E20
                            // dark 0xFF3E8E45
                            // lighter 0xFF7BC67F
                            // light 0xFF63B267
                            colors = listOf(Color(0xFF63B267), Color(0xFF63B267))
                        ),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(top=8.dp, bottom=4.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Forest,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Campsites",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(0.dp))
                    Text(
                        text = "${campsites.size} saved",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 300.dp)
            ) {
                // Replaced placeholders with actual campsite data -Szymon
                itemsIndexed(campsites) { index, campsite ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { if (expandedCampsite == index) {expandedCampsite = -1} else {expandedCampsite = index} })
                    ) {
                        OwnerCampsiteCard(
                            campsite = campsite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp)
                                .padding(horizontal = 8.dp),
                            isExpanded = expandedCampsite == index,
                            onPreviewCampsite = { navController.navigate("owner_campsite_preview/${campsite.id}") },
                            onEditCampsite = { onEditCampsite(campsite) },
                            onAddToLottery = { /* TODO : Add campsite to existing lottery */ },
                        )
                    }
//                    OwnerCampsiteCardActions(
//                        campsite = campsite,
//                        onPreviewCampsite = { navController.navigate("owner_campsite_preview/${campsite.id}") },
//                        onEditCampsite = { onEditCampsite(campsite) },
//                        onAddToLottery = { /* TODO : Add campsite to existing lottery */ },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(color = Color.LightGray)
//                            .padding(5.dp)
//                            .align(Alignment.CenterHorizontally),
//                    )
                }
            }
        }

    }
}

@Composable
fun OwnerCampsiteCardActions(
    campsite: Campsite,
    onPreviewCampsite: (Campsite) -> Unit,
    onEditCampsite: (Campsite) -> Unit,
    onAddToLottery: (Campsite) -> Unit
) {
    Row(
        modifier = Modifier
        .fillMaxWidth()
        .background(color = Color.LightGray)
        .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        GenericButton(text="Preview",onClick = { onPreviewCampsite(campsite) })
        GenericButton(text="Edit",onClick = { onEditCampsite(campsite) })
        GenericButton(text="Delete",onClick = { onAddToLottery(campsite) })
    }
}

@Composable
fun NewCampsiteButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color(0xFF63B267),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New campsite"
        )
    }
}

@Composable
fun OwnerCampsiteCard(
    campsite: Campsite,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onPreviewCampsite: (Campsite) -> Unit,
    onEditCampsite: (Campsite) -> Unit,
    onAddToLottery: (Campsite) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(0.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ){
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(
                text = campsite.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            CampsiteStatusIndicator(status = "Booked")
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                StoredImage(
                    imageId = campsite.images.firstOrNull() ?: "",
                    contentDescription = campsite.name,
                    modifier = Modifier.fillMaxSize()
                )

            }
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.6f)
            ) {
                // Title Header


                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Details Column
                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Location with pin icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = campsite.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Stats row (Capacity + Rating side-by-side)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Capacity
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Capacity",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${campsite.capacity} guests",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            // Rating
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = campsite.rating.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Chevron icon replacing standard text ">"
                    Icon(
                        imageVector = if (isExpanded) {Icons.Default.KeyboardArrowUp} else {Icons.Default.KeyboardArrowDown},
                        contentDescription = "View Details",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(0.1f)
                    )
                }
            }
        }
        if (isExpanded) {
            OwnerCampsiteCardActions(
                campsite = campsite,
                onPreviewCampsite = onPreviewCampsite,
                onEditCampsite = onEditCampsite,
                onAddToLottery = onAddToLottery
            )
            // shows the qr controls for this campsite
            CampsiteQrSection(campsiteId = campsite.id)
        }
    }
}

@Composable
fun CampsiteStatusIndicator(status: String) {
    val color = if (status == "Available") Color(0xFF2E7D32) else Color(0xFFC62828) // green / red
    val label = if (status == "Available") "Available" else "Booked"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
