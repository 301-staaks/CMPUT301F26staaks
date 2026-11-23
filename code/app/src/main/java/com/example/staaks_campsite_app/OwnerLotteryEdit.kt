package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.session.SessionManager

/**
 * The owner's screen for creating a new lottery or editing an existing one.
 *
 * Lottery data is placeholder only. Saving does not store anything yet, and an existing lottery is
 * not loaded from the database.
 *
 * @param navController controls navigation to other screens.
 * @param isNew true when creating a new lottery, which changes the title,
 * button text and starting field values.
 */
@Composable
fun OwnerLotteryEdit(navController: NavController, isNew: Boolean) {

    val session by SessionManager.state.collectAsState()

    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Owner Main", homeRoute = "owner_main", isAdmin = session!!.isAdmin) },
        bottomBar = { UiBottomRow(navController = navController, tabs = ownerTabs) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            /*
            -------------------------------------------------------------------------------------------------------------------------------------
            Main Content of Page
            -------------------------------------------------------------------------------------------------------------------------------------
             */

            // Same screen for create and edit: create starts empty, edit starts with placeholder values
            var name by remember { mutableStateOf(if (isNew) "" else "Lottery 1") }
            var campsite by remember { mutableStateOf(if (isNew) "" else "Campsite 1") }
            var openDate by remember { mutableStateOf(if (isNew) "" else "2026-11-01") }
            var drawDate by remember { mutableStateOf(if (isNew) "" else "2026-11-15") }
            var winners by remember { mutableStateOf(if (isNew) "" else "5") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Lottery Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = campsite, onValueChange = { campsite = it }, label = { Text("Campsite") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = openDate, onValueChange = { openDate = it }, label = { Text("Entries Open") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = drawDate, onValueChange = { drawDate = it }, label = { Text("Draw Date") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = winners, onValueChange = { winners = it }, label = { Text("Number of Winners") }, modifier = Modifier.fillMaxWidth())

                Button(
                    // Create/Save sends back to Lotteries
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    ),
                    onClick = { navController.popBackStack("owner_lotteries", inclusive = false) }, // TODO: save lottery
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isNew) "Create Lottery" else "Save Changes")
                }
            }
        }
    }
}
