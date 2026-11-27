package com.example.staaks_campsite_app


import android.R.attr.top
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.UserRepository
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.UserType
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import com.example.staaks_campsite_app.data.ImageEncoding
import com.example.staaks_campsite_app.data.ImageStore
import com.example.staaks_campsite_app.data.NotificationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Profile screens should be generalizable to all the types of users.
 *
 * Delete profile button is yet to be implemented
 *
 * @param navController controls navigation to other screens.
 */
@Composable
fun ProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val currentUserId = remember(context) { getDeviceId(context) }
    val userRepo = remember { UserRepository() }
    val users = userRepo.users
    val currentUser = users.find { it.id == currentUserId }


    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.name
            email = it.email
            phone = it.phone
        }
    }
    var home by remember { mutableStateOf("start") }
    var bottomTabs by remember { mutableStateOf(adminTabs) }

    if (currentUser?.type == UserType.ADMIN) {home = "admin_main"
        bottomTabs = adminTabs}
    else if (currentUser?.type == UserType.OWNER) {home = "owner_main"
        bottomTabs = ownerTabs}
    else if (currentUser?.type == UserType.ENTRANT) {home = "entree_main"
        bottomTabs = entrantTabs}


    var notiToggle by remember { mutableStateOf(true) }
    var editToggle by remember { mutableStateOf(false) }

    // US 01.10.01: the toggle follows the value saved on the user document,
    // so it is the same after reopening the app and on every device
    var notiError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentUser?.notificationsEnabled) {
        currentUser?.let { notiToggle = it.notificationsEnabled }
    }

    // profile photo upload. the photo is saved in the "images" collection
    // and the user keeps its id in profileImageId, same as campsite photos
    var photoUploading by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        photoUploading = true
        photoError = null

        scope.launch {
            val imageData = withContext(Dispatchers.Default) {
                ImageEncoding.encodeFromUri(context.applicationContext, uri)
            }

            if (imageData == null) {
                photoUploading = false
                photoError = "Couldn't read that photo. Try a different one."
                return@launch
            }

            // also deletes the old photo
            ImageStore.setProfileImage(currentUserId, imageData) { saved ->
                photoUploading = false
                if (!saved) {
                    photoError = "Couldn't save your photo. Try again."
                }
            }
        }
    }


    Scaffold(
        topBar = { UiTopRow(navController = navController, title = "Profile", homeRoute = home) },
        bottomBar = { UiBottomRow(navController = navController, tabs = bottomTabs) }
    ) { innerPadding ->







        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            contentAlignment = Alignment.TopCenter

        ) {
            /*
            -------------------------------------------------------------------------------------------------------------------------------------
            Create Main Content of Page Here
            -------------------------------------------------------------------------------------------------------------------------------------
             */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserProfileImagePreview(
                            size = 100.dp,
                            imageId = currentUser?.profileImageId.orEmpty()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentUser?.name.orEmpty(),
                            style = MaterialTheme.typography.titleLarge
                        )

                        TextButton(
                            enabled = currentUser != null && !photoUploading,
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text(
                                when {
                                    photoUploading -> "Uploading..."
                                    currentUser?.profileImageId.isNullOrBlank() -> "Add Photo"
                                    else -> "Change Photo"
                                }
                            )
                        }

                        photoError?.let {
                            Text(text = it, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (!editToggle) {
                    Text(
                        text = "Name: $name",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Email: $email",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, top = 8.dp)
                    ) {
                        Text(
                            text = "Phone: $phone",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { editToggle = !editToggle }
                        ) {
                            Text("Edit")
                        }
                    }
                } else if (editToggle) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text(text = "Name") }, singleLine = true
                    )

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text(text = "Email") }, singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = phone, onValueChange = { phone = it },
                            label = { Text(text = "Phone") }, singleLine = true
                        )
                        Button(
                            onClick = {
                                currentUser?.name = name
                                currentUser?.email = email
                                currentUser?.phone = phone
                                userRepo.addUser(currentUser!!)
                                editToggle = !editToggle }
                        ) {
                            Text("Save")
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Notifications: ",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        enabled = currentUser != null,
                        onClick = {
                            // switch right away, go back if the save fails
                            val previous = notiToggle
                            notiToggle = !previous
                            notiError = null
                            NotificationPreferences.setEnabled(currentUserId, notiToggle) { saved ->
                                if (!saved) {
                                    notiToggle = previous
                                    notiError = "Couldn't save your notification setting. Try again."
                                }
                            }
                        }
                    ) {
                        Text(if (notiToggle) "On" else "Off")
                    }
                }

                if (!notiToggle) {
                    Text(
                        text = "Pop-ups are off. New notifications still go to your Inbox.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                notiError?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Policy & Guidelines",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("policy_guidelines_screen") }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        onClick = {
                            userRepo.deleteUser(currentUser!!)
                            navController.navigate("start") }
                    ) {
                        Text("Delete Profile")
                    }
                }
            }
        }


    }
}