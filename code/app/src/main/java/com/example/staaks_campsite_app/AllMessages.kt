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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AllMessages(navController: NavController) {
    Scaffold(
        topBar = { AdminTopRow(navController = navController, title = "All Messages") },
        bottomBar = { AdminBottomRow(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ){
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

                    Button(onClick = {navController.navigate("admin_conversation")}) {Text("Temp Conversation Screen") }

                }


            }
        }
    }
}