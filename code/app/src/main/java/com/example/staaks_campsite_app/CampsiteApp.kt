package com.example.staaks_campsite_app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.admin_campsite
import com.example.staaks_campsite_app.data.model.owner_campsite
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.staaks_campsite_app.Entree.EntreeCampsite
import com.example.staaks_campsite_app.Entree.OwnerCampsitePreview
import com.example.staaks_campsite_app.data.getDeviceId

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CampsiteApp() {
    /*
    This is where we should initialize variables to be passed to screens in the app
     */

    val navController = rememberNavController()
    val campsiteRepository = remember {
        CampsiteRepository()
    }


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
        composable("entree_main") { EntrantMain(navController, selectedTab = "Home") }
        composable("entrant_campsite/{campsiteId}") { backStackEntry ->
            val campsiteId =
                backStackEntry.arguments?.getString("campsiteId") ?: ""
        }
        composable("entrant_campsite_review/{campsiteId}") { backStackEntry ->
            val campsiteId =
                backStackEntry.arguments?.getString("campsiteId") ?: ""
            EntreeCampsite(
                navController = navController,
                campsiteID = campsiteId
            )
        }
        composable("profile_screen") { ProfileScreen(navController) }
        composable("policy_guidelines_screen") { PolicyGuidelinesPage(navController) }
        composable("entree_res_screen") { EntreeResScreen(navController) }
        composable("entree_inbox_screen") { EntreeInboxScreen(navController) }
        composable("entrant_messages_screen") { OwnerMessages(navController) }
        composable("qr_scanner_screen") { QRScannerScreen(navController) }
        composable("entrant_conversation/{conversationId}") { EntrantConversation(navController) }
        composable("entrant_campsite/{campsiteId}") { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
            EntrantCampsiteScreen(navController, campsiteId)
        }
//        composable<entree_campsite> { backStackEntry ->
//            val campsite = backStackEntry.toRoute<entree_campsite>()
//            EntreeCampsite(navController, campsiteID = campsite.campsiteID)
//        }

        //composable("sample_res_page") { SampleResPage(navController) }
        composable("admin_users") { AdminUsersScreen(navController) }
        composable("admin_user/{userId}") { AdminUserProfile(navController) }
        composable("admin_user/{userId}") { AdminUserProfile(navController) }
        composable("all_notifications") { AllNotifications(navController) }
        composable("all_messages") { AllMessages(navController) }
        composable("images_screen") { ImagesScreen(navController) }
        composable("admin_conversation/{conversationId}") { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            AdminConversation(navController, conversationId)
        }

        composable<admin_campsite> { backStackEntry ->
            val campsite = backStackEntry.toRoute<admin_campsite>()
            AdminCampsite(navController, campsiteId = campsite.campsiteID)
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
        composable("owner_reservations") { OwnerReservationsScreen(navController) }
        composable("owner_lotteries") { OwnerLotteries(navController) }
        composable("owner_lottery_new") { OwnerLotteryEdit(navController, isNew = true) }
        composable("owner_lottery_edit") { OwnerLotteryEdit(navController, isNew = false) }
        composable("owner_lotteries _past") { OwnerPastLotteries(navController) }
        composable("owner_lottery_notification") { OwnerLotteryNotification(navController) }
        composable("owner_messages") { OwnerMessages(navController) }
        composable("owner_conversation/{conversationId}") { OwnerConversation(navController) }
        composable("owner_notifications") { OwnerNotifications(navController) }
        composable(
            route = "owner_campsite_preview/{campsiteId}",
            arguments = listOf( navArgument("campsiteId") { type = NavType.StringType })
        ) { OwnerCampsitePreview(navController, it.arguments?.getString("campsiteId") ?: "",) }

        composable<owner_campsite> { backStackEntry ->
            val campsite = backStackEntry.toRoute<owner_campsite>()

            OwnerCampsite(navController, campsiteID = campsite.campsiteID)
        }
    }
}