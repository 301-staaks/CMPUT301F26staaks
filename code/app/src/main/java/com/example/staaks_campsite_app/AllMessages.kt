package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.ConversationRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.model.Conversation
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AdminConversationListViewModel(
    private val repo: ConversationRepository = ConversationRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {
    val conversations: StateFlow<List<Conversation>> = repo.observeAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val users: Flow<List<User>> = userRepo.observeUsers()

    fun deleteConversation(conversation: Conversation) {
        repo.deleteConversation(conversation)
    }
}

@Composable
fun AllMessages(navController: NavController) {
    val viewModel: AdminConversationListViewModel = viewModel()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    var conversationToDelete by remember {mutableStateOf<Conversation?>(null)}


    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Users", homeRoute = "admin_main") },
        bottomBar = { UiBottomRow(navController = navController, tabs = adminTabs)}
    ) {
            innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.LightGray)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly

            ) {
                Button(
                    onClick = {  },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White,
                    ),
                ) { Text("Select User") }
            }



            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(conversations) { index, conversation ->
                    ConversationRow(
                        conversation = conversation,
                        onClick = {
                            navController.navigate("admin_conversation/${conversation.conversationId}")
                        },
                        onDelete = {
                            conversationToDelete = conversation
                        }
                    )
                    if (index < conversations.lastIndex) {
                        HorizontalDivider()
                    }

                }
            }

            conversationToDelete?.let { conversation ->
                AlertDialog(
                    onDismissRequest = { conversationToDelete = null },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteConversation(conversation)
                            conversationToDelete = null
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            conversationToDelete = null
                        }) { Text("Cancel") }
                    },
                    title = { Text("Delete Conversation") },
                    text = { Text("Are you sure you want to delete this conversation?") }

                )
            }
        }
    }
}

@Composable
fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val participant1 = conversation.participantIds[0]
    val participant2 = conversation.participantIds[1]

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)

    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = "Conversation ID: ${conversation.conversationId}",
                fontSize = 15.sp,
            )

            Text(
                text = "Participants: $participant1, $participant2",
                fontSize = 15.sp,
            )

            Spacer(modifier = Modifier.width(16.dp))

            DeleteButton(onDeleteClick = onDelete)
        }
    }
}