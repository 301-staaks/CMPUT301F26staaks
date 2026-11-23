package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.session.SessionManager

@Composable
fun AdminConversation(navController: NavController, conversationId: String) {

    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Conversation", homeRoute = "admin_main") },
        bottomBar = { UiBottomRow(navController = navController, tabs = adminTabs)}
    ) {
        innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val viewModel: ConversationViewModel = viewModel()
            val messages by viewModel.messages.collectAsStateWithLifecycle(initialValue = emptyList())

            val conversation = viewModel.getConversation()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("all_messages")
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF009E60),
                        contentColor = Color.White
                    )
                ){
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(
                        text = viewModel.getUser(conversation?.participantIds[0] ?: "")?.name ?: "",
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = viewModel.getUser(conversation?.participantIds[1] ?: "")?.name ?: "",
                        fontWeight = FontWeight.Bold,
                    )
                }



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
                        val isOnRight = message.senderId == conversation!!.participantIds[0]
                        val timestampFull = message.sentDate?.toDate().toString()
                        val timestamp = if (message.sentDate == null) {""} else {timestampFull.substring(4, 16)}
                        MessageBubble(
                            text = message.text,
                            timestamp = timestamp,
                            isFromMe = isOnRight,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}