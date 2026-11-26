package com.example.staaks_campsite_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.staaks_campsite_app.Entree.EntreeCampsite
import com.example.staaks_campsite_app.data.LotteryRepository


// this file just adapts older code to work with the new ui
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntrantCampsiteScreen(
    navController: NavController,
    campsiteId: String,
    lotteryRepository: LotteryRepository,
    entrantId: String
) {
    Scaffold(
        topBar = {
            UiTopRow(
                navController = navController,
                title = "Campsite",
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
        Column(modifier = Modifier.padding(innerPadding)) {
            EntreeCampsite(
                navController = navController,
                campsiteID = campsiteId,
                lotteryRepository = lotteryRepository,
                entrantId = entrantId
            )
        }
    }
}