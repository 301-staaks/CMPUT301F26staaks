package com.example.staaks_campsite_app


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.inboxRoute


/**
 * Displays: a pinned top bar with a back button (to the entree main screen) and a profile button,
 * the entrant's notifications with a "Clear All" button, and a pinned bottom row of navigation
 * buttons to the reservations, messages and QR scanner screens.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun EntreeInboxScreen(navController: NavController) {
    val context = LocalContext.current
    val entrantId = remember(context) { getDeviceId(context) }

    Scaffold(
        topBar = {UiTopRow(navController = navController, title = "Inbox", homeRoute = "entree_main")},
        bottomBar = {UiBottomRow(navController = navController, tabs = entrantTabs)}
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NotificationInbox(
                entrantId = entrantId,
                onOpenNotification = { notification ->
                    inboxRoute(notification)?.let { navController.navigate(it) }
                }
            )
        }
    }
}