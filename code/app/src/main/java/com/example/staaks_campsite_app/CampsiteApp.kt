package com.example.staaks_campsite_app

import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.admin_campsite
import com.example.staaks_campsite_app.data.model.entree_campsite
import com.example.staaks_campsite_app.data.model.owner_campsite

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
        composable<entree_campsite> { backStackEntry ->
            val campsite = backStackEntry.toRoute<entree_campsite>()

            EntreeCampsite(navController, campsiteID = campsite.campsiteID)
        }

        //composable("sample_res_page") { SampleResPage(navController) }
        composable("admin_users") { AdminUsersScreen(navController) }
        composable("admin_user") { AdminUserProfile(navController) }
        composable("all_notifications") { AllNotifications(navController) }
        composable("all_messages") { AllMessages(navController) }
        composable("images_screen") { ImagesScreen(navController) }
        composable("admin_conversation") { AdminConversation(navController) }

        composable<admin_campsite> { backStackEntry ->
            val campsite = backStackEntry.toRoute<admin_campsite>()

            AdminCampsite(navController, campsiteID = campsite.campsiteID)
        }



        // Owner screens
        composable("owner_main") { OwnerMain(navController) }
        composable("owner_campsite_new") { OwnerCampsiteEdit(navController, isNew = true) }
        composable("owner_campsite_edit") { OwnerCampsiteEdit(navController, isNew = false) }
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

        composable<owner_campsite> { backStackEntry ->
            val campsite = backStackEntry.toRoute<owner_campsite>()

            OwnerCampsite(navController, campsiteID = campsite.campsiteID)
        }
    }
}