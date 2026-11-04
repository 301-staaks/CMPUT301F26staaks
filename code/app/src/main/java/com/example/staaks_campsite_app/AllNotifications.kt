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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AllNotifications(navController: NavController) {
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
                text = "All Notifications",
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