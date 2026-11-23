package com.example.staaks_campsite_app

import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.admin_campsite
import com.example.staaks_campsite_app.data.model.owner_campsite
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.LotteryRepository

@Composable
fun CampsiteApp() {
    /*
    This is where we should initialize variables to be passed to screens in the app
     */

    val navController = rememberNavController()
    val campsiteRepository = remember { CampsiteRepository() }
    val lotteryRepository = remember { LotteryRepository() }

    val context = LocalContext.current
    val currentUserId = remember(context) { getDeviceId(context) }

    DisposableEffect(campsiteRepository) {
        onDispose {
            campsiteRepository.close()
        }
    }


    NavHost(navController = navController, startDestination = "start") {
        //Remember to pass needed variables to screens
        composable("start") { StartScreen(navController) }
        composable("admin_main") {
            AdminMain(
                navController = navController,
                campsites = campsiteRepository.campsites,
                isLoading = campsiteRepository.isLoading,
                loadError = campsiteRepository.loadError
            )
        }
        composable("entree_main") { EntreeMain(navController) }
        composable("profile_screen") { ProfileScreen(navController) }
        composable("policy_guidelines_screen") { PolicyGuidelinesPage(navController) }
        composable("entree_res_screen") { EntreeResScreen(navController) }
        composable("entree_inbox_screen") { EntreeInboxScreen(navController) }
        composable("messages_screen") { MessagesScreen(navController) }
        composable("qr_scanner_screen") { QRScannerScreen(navController) }
//        composable<entree_campsite> { backStackEntry ->
//            val campsite = backStackEntry.toRoute<entree_campsite>()
//            EntreeCampsite(navController, campsiteID = campsite.campsiteID)
//        }

        //composable("sample_res_page") { SampleResPage(navController) }
        composable("admin_users") { AdminUsersScreen(navController) }
        composable("admin_user/{userId}") { AdminUserProfile(navController) }
        composable("all_notifications") { AllNotifications(navController) }
        composable("all_messages") { AllMessages(navController) }
        composable("images_screen") { ImagesScreen(navController) }
        composable("admin_conversation") { AdminConversation(navController) }

        composable<admin_campsite> { backStackEntry ->
            val campsite = backStackEntry.toRoute<admin_campsite>()
            AdminCampsite(navController, campsiteId = campsite.campsiteID)
        }

        composable("admin_campsite/{campsiteId}") { backStackEntry ->

            val campsiteId =
                backStackEntry.arguments?.getString("campsiteId") ?: ""

            AdminCampsite(
                navController = navController,
                campsiteId = campsiteId
            )
        }

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
            ) { campsiteRepository.addOrUpdateCampsite(it) }
        }
        composable("owner_campsite_edit/{campsiteId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("campsiteId")
            OwnerCampsiteEdit(
                navController = navController,
                isNew = false,
                ownerId = currentUserId,
                existing = campsiteRepository.campsites.find { it.id == id },
            ) { campsiteRepository.addOrUpdateCampsite(it) }
        }
        composable("owner_reservations_active") { OwnerReservationsActive(navController) }
        composable("owner_reservations_past") { OwnerReservationsPast(navController) }
        composable("owner_reservations_cancelled") { OwnerReservationsCancelled(navController) }

        composable("owner_lotteries") {
            val today = todayString()

            val activeLotteries = lotteryRepository.lotteries
                .filter { it.ownerId == currentUserId && it.isActive(today) }
                .map { lottery ->
                    val ids = if (lottery.campsiteIds.isNotEmpty()) lottery.campsiteIds
                    else listOf(lottery.campsiteId)
                    lottery.toCardUi(
                        campsiteNames = ids.mapNotNull { id ->
                            campsiteRepository.campsites.find { it.id == id }?.name
                        },
                        waitlistCount = lotteryRepository.getWaitlistEntries(lottery.lotteryId).size,
                        today = today
                    )
                }

            OwnerLotteries(
                navController = navController,
                lotteries = activeLotteries,
                isLoading = false,  // TODO: LotteryRepository has no loading flag yet
                loadError = null    // TODO: LotteryRepository has no error state yet
            )
        }
        composable("owner_lottery_new") {
            OwnerLotteryEdit(
                navController = navController,
                isNew = true,
                ownerId = currentUserId,
                ownerCampsites = campsiteRepository.getCampsitesByOwner(currentUserId),
                existingLotteries = lotteryRepository.lotteries.filter { it.ownerId == currentUserId },
                onSave = { lotteryRepository.saveLottery(it) }   // writes to Firestore
            )
        }
        composable("owner_lottery_edit/{lotteryId}") { backStackEntry ->
            val lotteryId = backStackEntry.arguments?.getString("lotteryId") ?: ""
            OwnerLotteryEdit(
                navController = navController,
                isNew = false,
                ownerId = currentUserId,
                ownerCampsites = campsiteRepository.getCampsitesByOwner(currentUserId),
                existingLotteries = lotteryRepository.lotteries.filter { it.ownerId == currentUserId },
                existing = lotteryRepository.getLottery(lotteryId),
                currentWaitlistCount = lotteryRepository.getWaitlistEntries(lotteryId).size,
                onSave = { lotteryRepository.saveLottery(it) }
            )
        }
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