package com.example.staaks_campsite_app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun CampsiteApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "start") {
        composable("start") { StartScreen(navController) }
    }
}