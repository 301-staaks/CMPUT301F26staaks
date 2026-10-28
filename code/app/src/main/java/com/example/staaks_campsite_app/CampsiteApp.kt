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
        composable("entree_main") { EntreeMain(navController) }
        composable("profile_screen") { ProfileScreen(navController) }
        composable("policy_guidelines_screen") { PolicyGuidelinesPage(navController) }
        composable("entree_res_screen") { EntreeResScreen(navController) }
        composable("entree_inbox_screen") { EntreeInboxScreen(navController) }
        composable("messages_screen") { MessagesScreen(navController) }
        composable("qr_scanner_screen") { QRScannerScreen(navController) }

        //composable("sample_res_page") { SampleResPage(navController) }
        composable("admin_users") { AdminUsersScreen(navController) }
        composable("admin_user") { AdminUserProfile(navController) }
        composable("all_notifications") { AllNotifications(navController) }
        composable("all_messages") { AllMessages(navController) }
        composable("images_screen") { ImagesScreen(navController) }
        composable("admin_conversation") { AdminConversation(navController) }
        composable("admin_campsite") { AdminCampsite(navController) }


    }
}