package com.example.staaks_campsite_app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun CampsiteApp() {
    /*
    This is where we should initialize variables to be passed to screens in the app
     */
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = "start") {
        //Remember to pass needed variables to screens
        composable("start") { StartScreen(navController) }
        composable("admin_main") { AdminMain(navController) }

    }
}