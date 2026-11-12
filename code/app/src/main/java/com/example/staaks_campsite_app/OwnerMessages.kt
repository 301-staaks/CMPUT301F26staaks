package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.ConversationRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.Conversation
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


/**
 * The owner's messages screen, it lists conversations with guests.
 *
 * The conversations shown are placeholder data and are not loaded from the
 * database yet.
 *
 * @param navController controls navigation to other screens.
 */

class ConversationListViewModel(
    userId: String,
    private val repo: ConversationRepository = ConversationRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {
    val conversations: StateFlow<List<Conversation>> = repo.observeConversations(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getUser(userId: String): User? {
        return userRepo.users.find { it.id == userId }
    }

    fun deleteConversation(conversation: Conversation) {
        repo.deleteConversation(conversation)
    }


    companion object {
        fun factory(userId: String) = viewModelFactory {
            initializer { ConversationListViewModel(userId) }
        }
    }
}
@Composable
fun OwnerMessages(navController: NavController) {
    val context = LocalContext.current
    val session by SessionManager.state.collectAsState()
    val currentUserId = session?.userId ?: getDeviceId(context)
    val userType = session?.type ?: UserType.OWNER

    val viewModel: ConversationListViewModel = viewModel(factory = ConversationListViewModel.factory(currentUserId))
    val conversations by viewModel.conversations.collectAsStateWithLifecycle(initialValue = emptyList())

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
        Column(modifier = Modifier.padding(innerPadding)) {
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
                            .clickable { navController.navigate("owner_conversation/${conversation.conversationId}") }
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
    }
}
