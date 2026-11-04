package com.example.staaks_campsite_app

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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


    Column(

    ) {

        //Pinned Top Row

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()            // Makes the row span the full width
                .background(color = Color(0xFF1B5E20))
                .padding(16.dp)

        ){
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Button(
                    onClick = {
                        navController.navigate("admin_main")

                    },

                    ) {
                    Text("Home")
                }
            }


            Text(
                text = "Users",
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


        if (true){
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
                Button(onClick = {navController.navigate("admin_main")}) { Text("Admin") }
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
            Create Main Content of Page Here
            -------------------------------------------------------------------------------------------------------------------------------------
             */
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()            // Makes the row span the full width
                        .background(color = Color.LightGray)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {
                    Button(onClick = { /* ... */ },
                        shape = RectangleShape) { Text("All") }
                    Button(onClick = { /* ... */ },
                        shape = RectangleShape) { Text("Entrees") }
                    Button(onClick = { /* ... */ },
                        shape = RectangleShape) { Text("Owners") }
                    Button(onClick = { /* ... */ },
                        shape = RectangleShape) { Text("Admins") }
                }



                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(users) { index, user ->
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







        // Pinned Bottom Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.LightGray)
                .padding(5.dp),

            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {navController.navigate("admin_users")},
                modifier = Modifier.weight(1f)) { Text(text ="Users",
                fontSize = 10.sp) }
            Spacer(modifier = Modifier.width(1.dp))
            Button(onClick = {navController.navigate("all_notifications")},
                modifier = Modifier.weight(1f)) { Text(text ="Notifications",
                fontSize = 9.sp) }
            Spacer(modifier = Modifier.width(1.dp))
            Button(onClick = {navController.navigate("all_messages")},
                modifier = Modifier.weight(1f)) { Text(text ="Messages",
                fontSize = 10.sp) }
            Spacer(modifier = Modifier.width(1.dp))
            Button(onClick = {navController.navigate("images_screen")},
                modifier = Modifier.weight(1f)) { Text(text ="Images",
                fontSize = 10.sp) }
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
        CircularProfilePicture()

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
                text = user.email,
                fontSize = 15.sp,
            )

            Text(
                text = user.phone,
                fontSize = 15.sp,
            )
        }



        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = {
                onDelete()
            },
            enabled = canDelete,
            modifier = Modifier.width(90.dp).height(45.dp)
        ) {
            Text(text = "Delete")
        }
    }
}

@Composable
fun CircularProfilePicture(
    size: Dp = 60.dp
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