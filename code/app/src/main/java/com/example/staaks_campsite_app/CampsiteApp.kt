package com.example.staaks_campsite_app

import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.getDeviceId

@Composable
fun CampsiteApp() {
    /*
    This is where we should initialize variables to be passed to screens in the app
     */
    val navController = rememberNavController()
    val context = LocalContext.current
    val campsiteRepository = remember { CampsiteRepository() }
    val userRepository = remember { UserRepository() }
    val currentUserId = remember { getDeviceId(context) }


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


        // Owner screens
        composable("owner_main") {
            OwnerMain(
                navController = navController,
                campsites = campsiteRepository.getCampsitesByOwner(currentUserId),
                onEditCampsite = { navController.navigate("owner_campsite_edit/${it.id}") }
            )
        }
        composable("owner_campsite_new") {
            OwnerCampsiteEdit(
                navController = navController,
                isNew = true,
                ownerId = currentUserId,
                onSave = { campsiteRepository.addOrUpdateCampsite(it) }
            )
        }
        composable("owner_campsite_edit/{campsiteId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("campsiteId")
            OwnerCampsiteEdit(
                navController = navController,
                isNew = false,
                ownerId = currentUserId,
                existing = campsiteRepository.campsites.find { it.id == id },
                onSave = { campsiteRepository.addOrUpdateCampsite(it) }
            )
        }
        composable("owner_reservations_active") { OwnerReservationsActive(navController) }
        composable("owner_reservations_past") { OwnerReservationsPast(navController) }
        composable("owner_reservations_cancelled") { OwnerReservationsCancelled(navController) }
        composable("owner_lotteries") { OwnerLotteries(navController) }
        composable("owner_lottery_new") { OwnerLotteryEdit(navController, isNew = true) }
        composable("owner_lottery_edit") { OwnerLotteryEdit(navController, isNew = false) }
        composable("owner_lotteries_past") { OwnerPastLotteries(navController) }
        composable("owner_lottery_notification") { OwnerLotteryNotification(navController) }
        composable("owner_messages") { OwnerMessages(navController) }
        composable("owner_conversation") { OwnerConversation(navController) }
        composable("owner_notifications") { OwnerNotifications(navController) }
    }
}