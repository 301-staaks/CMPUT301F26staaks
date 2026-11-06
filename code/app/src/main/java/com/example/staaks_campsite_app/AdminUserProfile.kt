package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.model.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AdminUserProfileViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val repo = UserRepository()
    private val userId: String = checkNotNull(savedStateHandle["userId"])

    val user: StateFlow<User?> = repo.observeUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
@Composable
fun AdminUserProfile(
    navController: NavController,
    viewModel: AdminUserProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AdminTopRow(navController = navController, title = "User Profile") },
        bottomBar = { AdminBottomRow(navController = navController)}
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            user?.let {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(it.name)
                    Text(it.email)
                    Text(it.phone)
                }
            } ?: Text("Loading...")
        }
    }
}