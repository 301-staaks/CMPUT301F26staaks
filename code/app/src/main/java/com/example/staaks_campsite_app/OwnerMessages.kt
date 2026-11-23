package com.example.staaks_campsite_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.staaks_campsite_app.data.createUser
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.data.model.Conversation
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.collections.lastIndex


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

    val users: Flow<List<User>> = userRepo.observeUsers()

    fun getUser(userId: String): User? {
        return userRepo.users.find { it.id == userId }
    }

    fun createConversation(fromId: String, toId: String) {
        repo.createConversation(fromId, toId)
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
    val users by viewModel.users.collectAsStateWithLifecycle(initialValue = emptyList())


    var showNewConversationDialog by remember {mutableStateOf(false)}

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
        },
        // no longer in use: button to create new conversation
        /*
        floatingActionButton = {
            NewConversationButton(onClick = { showNewConversationDialog = true })
        },
        */
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 300.dp)
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
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(0.dp, Color.LightGray),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 12.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("owner_conversation/${conversation.conversationId}")}
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                UserProfileImagePreview(size=80.dp, imageId = "")
                            }

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

        if (showNewConversationDialog) {
            NewConversationDialog(
                onDismiss = { showNewConversationDialog = false },
                onCreate = { userId ->
                    viewModel.createConversation(
                        fromId = currentUserId,
                        toId = userId
                    )
                },
                users = users
            )
        }
    }
}

@Composable
fun NewConversationButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color(0xFF63B267),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New conversation"
        )
    }
}

@Composable
fun NewConversationDialog(
    onDismiss: () -> Unit,
    onCreate: (userId: String) -> Unit,
    users: List<User>,
) {
    var userId by remember { mutableStateOf("") }
    val valid = userId.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW CONVERSATION: TESTING ONLY") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select a user")
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(users) { index, user ->
                        if (user.type == UserType.ENTRANT) {
                            Row(
                                modifier = Modifier
                                    .clickable { userId = user.id }
                                    .background(if (userId == user.id) Color.LightGray else Color.Transparent)
                            ) {
                                Text("User name: ${user.name}")
                            }
                            if (index < users.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
                Text("Selected user: $userId")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(userId)
                },
                enabled = valid
            ) { Text("Done") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) { Text("Cancel") }
        }
    )
}
