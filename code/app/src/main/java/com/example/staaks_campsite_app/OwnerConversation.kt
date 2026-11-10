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
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.session.SessionManager


// Note: this screen is labeled as owner, but I'm guessing it will look the same for all types of
// user, so I am preemptively including some code for it to work for all user types.

/**
 * The owner's chat screen for a conversation with a guest.
 *
 * The messages shown are placeholder data. "Send" currently only clears the text field, and
 * messages are not saved or loaded from the database yet.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun OwnerConversation(navController: NavController) {

    val session by SessionManager.state.collectAsState()
    val userType = session!!.type


    Scaffold(
        topBar = {
            UiTopRow(
                navController = navController,
                title = "Messages",
                homeRoute = when (userType) {
                    UserType.ENTRANT -> "entrant_main"
                    UserType.OWNER -> "owner_main"
                    UserType.ADMIN -> "admin_main"
                }
            )
        },
        bottomBar = {
            UiBottomRow(
                navController = navController,
                tabs = when (userType) {
                    UserType.ENTRANT -> entrantTabs
                    UserType.OWNER -> ownerTabs
                    UserType.ADMIN -> adminTabs
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)){
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
    }
}
