package com.example.staaks_campsite_app

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.staaks_campsite_app.data.createUser
import com.example.staaks_campsite_app.data.getDeviceId
import com.example.staaks_campsite_app.data.model.Entrant
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.data.model.toUser

@Composable
fun StartScreen(navController: NavController) {
    var showNewUserDialog by remember {mutableStateOf(false)}
    var newUserType by remember {mutableStateOf(UserType.ADMIN)}

    val context = LocalContext.current
    var checkingUser by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("users")
            .document(getDeviceId(context))
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toUser()
                if (user != null) {
                    val route = when (user.type) {
                        UserType.ENTRANT -> "entrant_main"
                        UserType.OWNER -> "owner_main"
                        UserType.ADMIN -> "admin_main"
                    }
                    navController.navigate(route) {
                        popUpTo("start") { inclusive = true }
                    }
                } else {
                    checkingUser = false
                }
            }
            .addOnFailureListener { checkingUser = false }
    }

    if (checkingUser) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }



    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What User Are You?",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                newUserType = UserType.ADMIN
                showNewUserDialog = true
            },

        ) {
            Text("Admin")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                newUserType = UserType.OWNER
                showNewUserDialog = true
            },

            ) {
            Text("Owner")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                newUserType = UserType.ENTRANT
                showNewUserDialog = true
            },

            ) {
            Text("Entree")
        }
    }

    if (showNewUserDialog) {
        NewUserDialog(
            onDismiss = { showNewUserDialog = false },
            onCreate = { name, email, phone ->
                createUser(
                    context,
                    type = newUserType,
                    name = name,
                    email = email,
                    phone = phone
                )
                showNewUserDialog = false
                when (newUserType) {
                    UserType.ADMIN -> navController.navigate("admin_main")
                    UserType.OWNER -> navController.navigate("owner_main")
                    UserType.ENTRANT -> navController.navigate("entrant_main")
                }
            }
        )
    }
}

@Composable
fun NewUserDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(text = "Name") }, singleLine = true
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text(text = "Email") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text(text = "Phone") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(name, email, phone)
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