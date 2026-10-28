package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun OwnerConversation(navController: NavController) {
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
                text = "Chat",
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

            // placeholder: true = sent by owner, false = sent by guest
            val messages = listOf(
                false to "Placeholder guest message",
                true to "Placeholder owner reply",
                false to "Placeholder guest message"
            )
            var draft by remember { mutableStateOf("") }

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { (fromOwner, text) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (fromOwner) Arrangement.End else Arrangement.Start
                        ) {
                            Text(
                                text = text,
                                color = if (fromOwner) Color.White else Color.Black,
                                modifier = Modifier
                                    .background(
                                        color = if (fromOwner) Color(0xFF1B5E20) else Color.LightGray,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(10.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("Message") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { draft = "" }) { // TODO: send message
                        Text("Send")
                    }
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
