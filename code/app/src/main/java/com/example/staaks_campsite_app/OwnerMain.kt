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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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


private val ownerTabs = listOf(
    Tab("owner_main", "Main", Icons.Default.Home),
    Tab("owner_reservations_active", "Reservations", Icons.Default.CalendarMonth),
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

    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Owner Main", homeRoute = "owner_main", isAdmin = session!!.isAdmin) },
        bottomBar = { UiBottomRow(navController = navController, tabs = ownerTabs) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Replaced placeholders with actual campsite data -Szymon
            items(campsites, key = { it.id }) { campsite ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(campsite.name, fontSize = 20.sp)
                        Text("Location: ${campsite.location}")
                        Text("Capacity: ${campsite.capacity}  |  Price/night: ${campsite.price}")
                        Text("Upcoming reservations: —")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onEditCampsite(campsite) }) {
                            Text("Edit")
                        }
                    }
                }
            }
        }
    }
}
