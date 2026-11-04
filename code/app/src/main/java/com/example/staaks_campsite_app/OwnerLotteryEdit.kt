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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

// OWNER VIEW (preview only) - all data on this screen is placeholder
@Composable
fun OwnerLotteryEdit(navController: NavController, isNew: Boolean) {
    Column {

        // Pinned Top Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()            // Makes the row span the full width
                .background(color = Color(0xFF1B5E20))
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text("Back")
                }
            }

            Text(
                text = if (isNew) "New Lottery" else "Edit Lottery",
                fontSize = 25.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {

            }
        }

        // This is for when the admin is spoofing a different user. Logic must be implemented here to allow this to pop up
        if (false) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()            // Makes the row span the full width
                    .background(color = Color(0xFF63B267))
                    .heightIn(max = 45.dp)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "View:",
                    fontSize = 15.sp,
                    color = Color.White
                )
                Button(onClick = { /* ... */ }) { Text("Admin") }
                Button(onClick = { /* ... */ }) { Text("Owner") }
                Button(onClick = { /* ... */ }) { Text("Entree") }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
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
                    onClick = { navController.popBackStack("owner_lotteries", inclusive = false) }, // TODO: save lottery
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isNew) "Create Lottery" else "Save Changes")
                }
            }
        }

        // Pinned Bottom Row (Owner navigation)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("owner_reservations_active") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Reservations", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_notifications") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Notifs", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_messages") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Messages", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_lotteries") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Lotteries", fontSize = 11.sp) }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .heightIn(max = 16.dp)
                .heightIn(min = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

        }
    }
}
