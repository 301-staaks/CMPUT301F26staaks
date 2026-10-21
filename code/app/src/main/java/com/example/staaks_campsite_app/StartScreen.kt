package com.example.staaks_campsite_app

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

@Composable
fun StartScreen(navController: NavController) {
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

            },

        ) {
            Text("Admin")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

            },

            ) {
            Text("Owner")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

            },

            ) {
            Text("Entree")
        }
    }
}