package com.example.staaks_campsite_app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.getDeviceId
import androidx.compose.ui.platform.LocalContext
import com.example.staaks_campsite_app.data.CampsiteRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staaks_campsite_app.Entree.EntreeCampsite
import com.example.staaks_campsite_app.Entree.EntreeCampsite
import com.example.staaks_campsite_app.data.model.Campsite




val entrantTabs = listOf(
    Tab("entree_main", "Home", icon = Icons.Default.Home),
    Tab("entree_res_screen", "Reservations", icon = Icons.AutoMirrored.Filled.List),
    Tab("entree_inbox_screen", "Inbox", icon =Icons.Default.Search),
    Tab("entrant_messages_screen", "Messages", icon =Icons.Default.Textsms),
    Tab("qr_scanner_screen", "Scan", icon =Icons.Default.QrCode)
)

/**
 * Displays: a top bar that has a home and profile button, a bottom navigation bar that has
 * reservations, inbox, messages, and scan
 *
 * @param navController app navigation controller
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrantMain(
    navController: NavController,
    selectedTab: String,
) {
    // Selected inner screen (home, profile, reservations, inbox, messages, scan )
    var selectedTab by remember { mutableStateOf(selectedTab) }

    // Sample list of campsites
    val campsites = remember { CampsiteRepository() }
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }


    Scaffold(
        topBar = { UiTopRow(navController, selectedTab, "entree_main")},
        bottomBar = {UiBottomRow(navController, entrantTabs )}
            /*
            NavigationBar(containerColor = Color(0xFF1B5E20)) {
                NavigationBarItem(
                    selected = selectedTab == "Reservations",
                    onClick = { selectedTab = "Reservations" },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Reservations") },
                    label = { Text("Reservations")},
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF63B267),
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.8f),
                        unselectedTextColor = Color.White.copy(alpha = 0.8f),
                        )
                )
                NavigationBarItem(
                    selected = selectedTab == "Inbox",
                    onClick = { selectedTab = "Inbox" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Inbox") },
                    label = { Text("Inbox") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF63B267),
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.8f),
                        unselectedTextColor = Color.White.copy(alpha = 0.8f),
                    )

                )
                NavigationBarItem(
                    selected = selectedTab == "Messages",
                    onClick = { selectedTab = "Messages" },
                    icon = { Icon(Icons.Default.Textsms, contentDescription = "Message") },
                    label = { Text("Message") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF63B267),
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.8f),
                        unselectedTextColor = Color.White.copy(alpha = 0.8f),
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "Scan",
                    onClick = { selectedTab = "Scan" },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = "Scan") },
                    label = { Text("Scan") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF63B267),
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.8f),
                        unselectedTextColor = Color.White.copy(alpha = 0.8f),
                    )
                )
            }
        }

                 */
    ) { innerPadding ->
        // =======================================================
        // 3. INNER SCREEN CONTENT
        // =======================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Fits exactly between Top & Bottom menus
        ) {
             Home(
                    navController,
                    campsites,
                    onCampsiteChange = { selectedCampsite = it},
                    onSelectedTabChange = { selectedTab = it}
             )
        }
    }
}

/**
 * home tab for entrants
 *
 * shows campsite search bar and a scrolling list of campsites. If a search or filter has been
 * inputted, the filtered list will be displayed. Otherwise every campsite is just shown. Displays
 * "No campsites." if there is no campsites.
 *
 * @param campsites the repository that provides the full lis of campsites
 * @param onCampsiteChange called when a campsite is selected
 * @param onSelectedTabChange called when the selected tab is changed
 */
@Composable
fun Home(
    navController: NavController,
    campsites: CampsiteRepository,
    onCampsiteChange: (Campsite) -> Unit = {},
    onSelectedTabChange: (String) -> Unit = {}
){
    var filteredCampsites by remember { mutableStateOf<List<Campsite>?>(null) }
    val displayCampsites = filteredCampsites ?: campsites.campsites
    Column(modifier = Modifier.fillMaxSize()) {
        CampsiteSearchBar(
            campsites = campsites,
            onChangeCampsites = { filteredCampsites = it }
        )
        if (displayCampsites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No campsites.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // lazy column
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(displayCampsites) { index, campsite: Campsite ->
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {navController.navigate("entrant_campsite/${campsite.id}")},
                                enabled = campsite.name != "Loading Campsite..."
                            ),
                    ) {
                        EntrantCampsiteCard(
                            campsite = campsite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CampsiteCard(
    campsite: Campsite,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier){
        Column(
            modifier = Modifier
                .weight(0.4f)
        ) {
            StoredImage(
                imageId = campsite.images.firstOrNull() ?: "",
                contentDescription = campsite.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                placeholder = {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = "Image not supported",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .weight(0.6f)
        ) {
            // Title Header
            Text(
                text = campsite.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(0.1f)
                )
            }
        }
    }
}

@Composable
fun EntrantCampsiteCard(
    campsite: Campsite,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(0.dp, Color(0xFF3E8E45)),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    placeholder = {
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = "Image not supported",
                            modifier = Modifier.size(48.dp)
                        )
                    }
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
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View Details",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(0.1f)
                    )
                }
            }
        }
    }
}



@Composable
fun Profile(navController: NavController) {
    // TODO:    navController.navigate("profile")
    NotFoundScreen()
}
@Composable
fun ReservationScreenContent() {
    NotFoundScreen()
}
/**
 * the entrant's Inbox tab
 */
@Composable
fun InboxContent() {
    val context = LocalContext.current
    val entrantId = remember(context) { getDeviceId(context) }
    NotificationInbox(entrantId = entrantId)
}
@Composable
fun MessageScreenContent() {
    NotFoundScreen()
}
@Composable
fun QRCodeScreenContent() {
    NotFoundScreen()
}
@Composable
fun CampsiteDetailsScreenContent(navController: NavController, campsiteId: String, onSelectedTabChange: (String) -> Unit = {}){
    BackHandler(enabled = true) {
        onSelectedTabChange("Home")
    }
    EntreeCampsite(navController, campsiteId)
}

@Composable
fun NotFoundScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "404 Error",
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "404",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Page Not Found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}