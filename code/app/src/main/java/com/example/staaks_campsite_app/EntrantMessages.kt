package com.example.staaks_campsite_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.session.SessionManager

@Composable
fun EntrantMessagesScreen(navController: NavController) {
    val context = LocalContext.current
    val session by SessionManager.state.collectAsState()
    val currentUserId = session?.userId ?: getDeviceId(context)
    val userType = session?.type ?: UserType.ENTRANT

    val viewModel: ConversationListViewModel = viewModel(factory = ConversationListViewModel.factory(currentUserId))
    val conversations by viewModel.conversations.collectAsStateWithLifecycle(initialValue = emptyList())


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(conversations) { conversation ->
            val otherUserId = conversation.participantIds.find { it != session?.userId }
            var otherUser: User? = null
            if (otherUserId != null) {
                otherUser = viewModel.getUser(otherUserId)
            }
            if (otherUser == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Error loading user", fontSize = 18.sp)
                        Text("Campsite: —")
                        Text("—", color = Color.Gray)
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("entrant_conversation/${conversation.conversationId}") }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(otherUser?.name?: "User", fontSize = 18.sp)
                    Text("Campsite: —")
                    Text("Last message preview…", color = Color.Gray)
                }
            }
        }
    }
}