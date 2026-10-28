package com.example.staaks_campsite_app


import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MessagesScreen(navController: NavController) {
    Column(

    ) {
        /*
        Pinned Top Row
         */
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
                        navController.navigate("start")

                    },

                    ) {
                    Text("Back")
                }
            }


            Text(
                text = "Messages",
                fontSize = 25.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {

                Button(
                    onClick = { navController.navigate("profile_screen") }
                ) {
                    Text("Profile")
                }

            }
        }

        // This is for when the admin is spoofing a different user. Logic must be implemented here to allow this to pop up
        if (false){
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
                Button(onClick = { /* ... */ }) { Text("Admin") }
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
            Button(onClick = { navController.navigate("entree_res_screen") }) { Text("1") }
            Button(onClick = { navController.navigate("entree_inbox_screen") }) { Text("2") }
            Button(onClick = { /* ... */ }) { Text("3") }
            Button(onClick = { navController.navigate("qr_scanner_screen") }) { Text("4") }
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