package com.example.staaks_campsite_app

import androidx.navigation.compose.NavHost
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
    }
}