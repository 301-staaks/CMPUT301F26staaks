package com.example.staaks_campsite_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.data.model.toUser
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.compareTo

class UsersListViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {
    val users: StateFlow<List<User>> = repo.observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteUser(user: User) {
        repo.deleteUser(user)
    }
}

@Composable
fun AdminUsersScreen(
    navController: NavController,
    viewModel: UsersListViewModel = viewModel()
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    var userToDelete by remember {mutableStateOf<User?>(null)}
    val userListFilter = remember { mutableStateListOf(UserType.ADMIN, UserType.OWNER, UserType.ENTRANT) }

    Scaffold(
        topBar = { AdminTopRow(navController = navController, title = "Users") },
        bottomBar = { AdminBottomRow(navController = navController)}
    ) {
        innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()            // Makes the row span the full width
                    .background(color = Color.LightGray)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly

            ) {
                Button(
                    onClick = { if (userListFilter.contains(UserType.ENTRANT)) { userListFilter.remove(UserType.ENTRANT) } else { userListFilter.add(UserType.ENTRANT) } },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userListFilter.contains(UserType.ENTRANT)) { Color(0xFF009E60) } else { Color.Gray },
                        contentColor = Color.White,
                    ),
                ) { Text("Entrees") }

                Button(
                    onClick = { if (userListFilter.contains(UserType.OWNER)) { userListFilter.remove(UserType.OWNER) } else { userListFilter.add(UserType.OWNER) } },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userListFilter.contains(UserType.OWNER)) { Color(0xFF009E60) } else { Color.Gray },
                        contentColor = Color.White
                    )
                ) { Text("Owners") }

                Button(
                    onClick = { if (userListFilter.contains(UserType.ADMIN)) { userListFilter.remove(UserType.ADMIN) } else { userListFilter.add(UserType.ADMIN) } },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userListFilter.contains(UserType.ADMIN)) { Color(0xFF009E60) } else { Color.Gray },
                        contentColor = Color.White
                    )
                ) { Text("Admins") }
            }



            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(users) { index, user ->
                    if (user.type in userListFilter) {
                        UserRow(
                            user = user,
                            canDelete = user.type != UserType.ADMIN,
                            onClick = {
                                navController.navigate("admin_user/${user.id}")
                            },
                            onDelete = {
                                userToDelete = user
                            }
                        )
                        if (index < users.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            userToDelete?.let { user ->
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteUser(user)
                            userToDelete = null
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            userToDelete = null
                        }) { Text("Cancel") }
                    },
                    title = { Text("Delete User") },
                    text = { Text("Are you sure you want to delete ${user.name}?") }

                )
            }
        }
    }
}



@Composable
fun UserRow(
    user: User,
    canDelete: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)

    ) {
        UserProfileImagePreview(60.dp, user.profileImageUrl)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = user.name,
                fontSize = 15.sp,
            )

            Text(
                text = user.type.toString(),
                fontSize = 15.sp,
            )

            Text(
                text = "Email: ${user.email}",
                fontSize = 15.sp,
            )

            Text(
                text = "Phone: ${user.phone}",
                fontSize = 15.sp,
            )
        }



        Spacer(modifier = Modifier.width(16.dp))

        DeleteButton(onDeleteClick = onDelete, enabled = canDelete)
    }
}

@Composable
fun CircularProfilePicture(
    size: Dp = 60.dp,
    imageUrl: String = "",
) {
    Card(
        modifier = Modifier.size(size),
        shape = CircleShape,
        colors = CardDefaults.cardColors(Color.LightGray),
    ) {
        Image(
            painter = ColorPainter(Color.Gray),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun DeleteButton(
    onDeleteClick: () -> Unit,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onDeleteClick,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete item"
        )
    }
}