package com.example.staaks_campsite_app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.CampsiteRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.staaks_campsite_app.data.model.Campsite


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntreeMain(
    navController: NavController
) {
    // Selected inner screen (home, profile, reservations, inbox, messages, scan )
    var selectedTab by remember { mutableStateOf("Home") }

    // Sample list of campsites
    val campsites = remember { CampsiteRepository() }
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        selectedTab = "Home"
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }},
                title = {
                    Text(
                        text = selectedTab,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                actions = {
                    IconButton(onClick = {
                        selectedTab = "Profile"
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },

        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "Reservations",
                    onClick = { selectedTab = "Reservations" },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Reservations") },
                    label = { Text("Reservations") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Inbox",
                    onClick = { selectedTab = "Inbox" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Inbox") },
                    label = { Text("Inbox") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Messages",
                    onClick = { selectedTab = "Messages" },
                    icon = { Icon(Icons.Default.Textsms, contentDescription = "Message") },
                    label = { Text("Message") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Scan",
                    onClick = { selectedTab = "Scan" },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = "Scan") },
                    label = { Text("Scan") }
                )
            }
        }
    ) { innerPadding ->

        // =======================================================
        // 3. INNER SCREEN CONTENT
        // =======================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Fits exactly between Top & Bottom menus
        ) {
            when (selectedTab) {
                "Home" -> Home(campsites, onCampsiteChange = { selectedCampsite = it}, onSelectedTabChange = { selectedTab = it})
                "Profile" -> Profile(navController)
                "Reservations" -> ReservationScreenContent()
                "Inbox" -> InboxContent()
                "Messages" -> MessageScreenContent()
                "Scan" -> QRCodeScreenContent()
                "Campsite Details" -> {
                    val currentCampsite = selectedCampsite
                    if (currentCampsite != null) {
                        CampsiteDetailsScreenContent(currentCampsite.id, onSelectedTabChange = { selectedTab = it})
                    } else {
                        LaunchedEffect(Unit) {
                            selectedTab = "Home"
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Home(
    campsites: CampsiteRepository,
    onCampsiteChange: (Campsite) -> Unit = {},
    onSelectedTabChange: (String) -> Unit = {}
){
    var filteredCampsites by remember { mutableStateOf<List<Campsite>?>(null) }
    val displayCampsites = filteredCampsites ?: campsites.campsites
    Column(modifier = Modifier.fillMaxSize()) {
        CampsiteSearchBar(
            campsites = campsites,
            onChangeCampsites = {filteredCampsites = it}
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(displayCampsites) { index, campsite: Campsite ->
                    CampsiteCard(
                        campsite = campsite,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
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
            /* TODO: Image*/
        }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .weight(0.6f)

        ) {
            Text(text = campsite.name)

            Row() {
                Column(modifier = Modifier.weight(0.9f)) {
                    // TODO: format the card
                    Text(text = campsite.location)
                    Text(text = campsite.capacity.toString())
                    Text(text = campsite.rating.toString())
                }
                Column(
                    modifier = Modifier
                        .weight(0.1f)
                ) {
                    Text(text = ">", textAlign = TextAlign.Center)
                }
            }
        }
    }
}



fun Profile(navController: NavController) {
    // TODO:    navController.navigate("profile")
}
fun ReservationScreenContent() {}
fun InboxContent() {}
fun MessageScreenContent() {}
fun QRCodeScreenContent() {}
@Composable
fun CampsiteDetailsScreenContent(campsiteId: String, onSelectedTabChange: (String) -> Unit = {}){
    BackHandler(enabled = true) {
        onSelectedTabChange("Home")
    }
    // TODO: pass campsiteId to the CampsiteDetailsScreen
}