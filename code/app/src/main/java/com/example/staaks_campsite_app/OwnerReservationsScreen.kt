package com.example.staaks_campsite_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OwnerReservationsScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            UiTopRow(
                navController = navController,
                title = "Reservations",
                homeRoute = "owner_main"
            )
        },
        bottomBar = {
            UiBottomRow(
                navController = navController,
                tabs = ownerTabs
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ReservationScreen(navController)
        }
    }
}