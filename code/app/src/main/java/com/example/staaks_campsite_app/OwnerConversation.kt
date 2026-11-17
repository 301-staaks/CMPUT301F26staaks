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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.ConversationRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.model.Conversation
import com.example.staaks_campsite_app.data.model.Message
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


// Note: this screen is labeled as owner, but I'm guessing it will look the same for all types of
// user, so I am preemptively including some code for it to work for all user types.

class ConversationViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repo: ConversationRepository = ConversationRepository()
    private val userRepo: UserRepository = UserRepository()

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    val messages: Flow<List<Message>> = repo.getMessages(conversationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getUser(userId: String): User? {
        return userRepo.users.find { it.id == userId }
    }

    fun getUserFlow(userId: String) : Flow<User?> {
        return userRepo.observeUser(userId)
    }

    fun getConversation(): Conversation? {
        return repo.getConversation(conversationId)
    }

    fun sendMessage(senderId: String, content: String): Unit {
        val newMessage = Message(
            conversationId = conversationId,
            senderId = senderId,
            text = content
        )
        repo.sendMessage(message = newMessage)
    }
}

/**
 * The owner's chat screen for a conversation with a guest.
 *
 * The messages shown are placeholder data. "Send" currently only clears the text field, and
 * messages are not saved or loaded from the database yet.
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun OwnerConversation(
    navController: NavController,
    ) {

    val session by SessionManager.state.collectAsState()
    val userType = session?.type ?: UserType.OWNER

    val viewModel: ConversationViewModel = viewModel()
    val messages by viewModel.messages.collectAsStateWithLifecycle(initialValue = emptyList())

    val conversation = viewModel.getConversation()

    val otherUserId = conversation?.participantIds?.find { it != session?.userId }

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

            var draft by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                GenericButton(
                    text = "Back",
                    onClick = { navController.navigate("owner_messages") },
                )
                Text(
                    text = viewModel.getUser(otherUserId ?: "")?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            HorizontalDivider()


            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(messages) { index, message ->
                        val fromOwner = message.senderId == session?.userId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (fromOwner) Arrangement.End else Arrangement.Start,
                        ) {
                            Text(
                                text = message.text,
                                color = if (fromOwner) Color.White else Color.Black,
                                modifier = Modifier
                                    .background(
                                        color = if (fromOwner) Color(0xFF1B5E20) else Color.LightGray,
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    .padding(10.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
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

                    GenericButton(
                        text = "Send",
                        onClick = {
                            if (draft.isNotBlank()) {
                                viewModel.sendMessage(session?.userId ?: "", draft)
                                draft = ""
                            }
                        }
                    )
                }
            }
        }
    }
}
