package com.example.staaks_campsite_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.staaks_campsite_app.Entree.EntreeCampsite
import com.example.staaks_campsite_app.Entree.OwnerCampsitePreview
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.LotteryRepository
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.admin_campsite
import com.example.staaks_campsite_app.data.model.owner_campsite

@RequiresApi(Build.VERSION_CODES.O)
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
    LaunchedEffect(currentUserId) { android.util.Log.d("MyUserId", currentUserId) }

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
        composable("entree_main") {
            EntrantMain(
                navController = navController,
                lotteryRepository = lotteryRepository,
                currentUserId = currentUserId,
                selectedTab = "Home"
            )
        }

        composable("entrant_campsite/{campsiteId}") { backStackEntry ->
            val campsiteId =
                backStackEntry.arguments?.getString("campsiteId") ?: ""

            EntrantCampsiteScreen(
                navController = navController,
                campsiteId = campsiteId,
                lotteryRepository = lotteryRepository,
                entrantId = currentUserId
            )
        }

        composable("entrant_campsite_review/{campsiteId}") { backStackEntry ->
            val campsiteId =
                backStackEntry.arguments?.getString("campsiteId") ?: ""

            EntreeCampsite(
                navController = navController,
                campsiteID = campsiteId,
                lotteryRepository = lotteryRepository,
                entrantId = currentUserId
            )
        }
        composable("profile_screen") { ProfileScreen(navController) }
        composable("policy_guidelines_screen") { PolicyGuidelinesPage(navController) }
        composable("entree_res_screen") { EntreeResScreen(navController) }
        composable("entree_inbox_screen") { EntreeInboxScreen(navController) }
        composable("entrant_messages_screen") { OwnerMessages(navController) }
        composable("qr_scanner_screen") { QRScannerScreen(navController) }
        composable("entrant_conversation/{conversationId}") { EntrantConversation(navController) }
//        composable<entree_campsite> { backStackEntry ->
//            val campsite = backStackEntry.toRoute<entree_campsite>()
//            EntreeCampsite(navController, campsiteID = campsite.campsiteID)
//        }
        // opens the campsite found by the qr scanner
        composable("entree_campsite/{campsiteId}") { entry ->
            val campsiteId = entry.arguments?.getString("campsiteId") ?: ""

            EntreeCampsite(navController, campsiteId, lotteryRepository, currentUserId)
        }

        //composable("sample_res_page") { SampleResPage(navController) }
        composable("admin_users") { AdminUsersScreen(navController) }
        composable("admin_user/{userId}") { AdminUserProfile(navController) }
        composable("all_notifications") { AllNotifications(navController) }
        composable("all_messages") { AllMessages(navController) }
        composable("images_screen") { ImagesScreen(navController) }
        composable("admin_conversation/{conversationId}") { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            AdminConversation(navController, conversationId)
        }
        composable("admin_reviews") { AdminReviews(navController) }

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
        // prepares and displays the owner's active lotteries
        composable("owner_lotteries") {
            val today = todayString()

            val cards = lotteryRepository.lotteries
                .filter {
                    it.ownerId == currentUserId && it.isActive(today)
                }
                .map { lottery ->
                    val ids = (lottery.campsiteIds + lottery.campsiteId)
                        .filter { it.isNotBlank() }
                        .distinct()

                    lottery.toCardUi(
                        campsiteNames = ids.mapNotNull { id ->
                            campsiteRepository.campsites.find { it.id == id }?.name
                        },
                        waitlistCount = lotteryRepository
                            .getWaitlistEntries(lottery.lotteryId)
                            .size,
                        today = today
                    )
                }

            OwnerLotteries(
                navController = navController,
                lotteries = cards,
                isLoading = lotteryRepository.lotteriesLoading,
                loadError = lotteryRepository.lotteryError
            )
        }

        // opens the form and saves a new lottery
        composable("owner_lottery_new") {
            OwnerLotteryEdit(
                navController = navController,
                isNew = true,
                ownerId = currentUserId,
                ownerCampsites = campsiteRepository.getCampsitesByOwner(currentUserId),
                existingLotteries = lotteryRepository.lotteries,
                onSave = { lottery, done ->
                    lotteryRepository.saveLottery(lottery)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                done(null)
                            } else {
                                done(
                                    task.exception?.localizedMessage
                                        ?: "Could not save lottery"
                                )
                            }
                        }
                }
            )
        }

        // loads the selected lottery and opens the edit form
        composable("owner_lottery_edit/{lotteryId}") { entry ->
            val lotteryId = entry.arguments?.getString("lotteryId") ?: ""
            val existing = lotteryRepository.getLottery(lotteryId)

            when {
                lotteryRepository.lotteriesLoading -> {
                    androidx.compose.material3.Text("Loading lottery...")
                }

                lotteryRepository.lotteryError != null -> {
                    androidx.compose.material3.Text(
                        lotteryRepository.lotteryError ?: "Could not load lottery"
                    )
                }

                existing == null || existing.ownerId != currentUserId -> {
                    androidx.compose.material3.Text("Lottery not found for this owner")
                }

                else -> {
                    OwnerLotteryEdit(
                        navController = navController,
                        isNew = false,
                        ownerId = currentUserId,
                        ownerCampsites = campsiteRepository
                            .getCampsitesByOwner(currentUserId),
                        existingLotteries = lotteryRepository.lotteries,
                        existing = existing,
                        currentWaitlistCount = lotteryRepository
                            .getWaitlistEntries(lotteryId)
                            .size,
                        onSave = { lottery, done ->
                            lotteryRepository.saveLottery(lottery)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        done(null)
                                    } else {
                                        done(
                                            task.exception?.localizedMessage
                                                ?: "Could not save lottery"
                                        )
                                    }
                                }
                        }
                    )
                }
            }
        }
        composable("owner_lotteries_past") { OwnerPastLotteries(navController) }
        composable("owner_lottery_notification") { OwnerLotteryNotification(navController) }
        composable("owner_messages") { OwnerMessages(navController) }
        composable("owner_conversation") { OwnerConversation(navController) }
        composable("owner_notifications") { OwnerNotifications(navController) }
        composable(
            route = "owner_campsite_preview/{campsiteId}",
            arguments = listOf(
                navArgument("campsiteId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val campsiteId = entry.arguments?.getString("campsiteId") ?: ""

            OwnerCampsitePreview(navController, campsiteId)
        }

        composable<owner_campsite> { backStackEntry ->
            val campsite = backStackEntry.toRoute<owner_campsite>()

            OwnerCampsite(navController, campsiteID = campsite.campsiteID)
        }
    }
}