package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.staaks_campsite_app.data.CampsiteRepository
import com.example.staaks_campsite_app.data.model.Campsite


/**
 * Looks up the campsite matching [campsiteID] in the [CampsiteRepository] and displays its name,
 * rating, image carousel, description and details (location, capacity, price per night). If no
 * campsite matches, an empty placeholder campsite is shown instead.
 *
 * The "View Lotteries" button is not implemented yet.
 *
 * @param navController controls navigation to other screens
 * @param campsiteID the id of the campsite to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntreeCampsite(navController: NavController, campsiteID: String) {

    val CampRepo = CampsiteRepository()
    val CampsitesList = CampRepo.campsites
    val campsite = CampsitesList.find({ it.id == campsiteID }) ?: Campsite(id = "", ownerId = CampRepo.campsites[0].ownerId, name = "", location = "", capacity = 0, date = mutableListOf(), price = 0.0)

    val ImageNum = campsite.images?.size

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
                        navController.navigate("entree_main")

                    },

                    ) {
                    Text("Home")
                }
            }


            Text(
                text = "Campsite",
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
            Column(modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(modifier = Modifier.padding(8.dp)) {
                    Button(
                        onClick = { navController.navigate("entree_main") },
                        modifier = Modifier
                            .size(
                                width = 100.dp,
                                height = 35.dp
                            ),
                        shape = RectangleShape

                    ) { Text(text = "Back") }
                    Spacer(modifier = Modifier
                        .width(1.dp)
                        .weight(1f))
                }
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier
                        .width(1.dp)
                        .weight(1f))
                    Text(
                        text = "${campsite?.name}",
                        modifier = Modifier.weight(1f)
                    )

                    Text(text = "${campsite?.rating} Stars")
                }

                if (ImageNum != null) {
                    HorizontalMultiBrowseCarousel(
                        state = rememberCarouselState { ImageNum },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 16.dp, bottom = 16.dp),
                        preferredItemWidth = 186.dp,
                        itemSpacing = 8.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { i ->
                        val image = campsite.images[i]
                        AsyncImage(
                            model = image,
                            contentDescription = null,
                        )


                    }

                }

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(color = Color.LightGray)
                        .fillMaxWidth()

                ){Text(text = "Description: ${campsite?.description}")}
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(color = Color.LightGray)
                        .fillMaxWidth()

                ){Text(text = "Details\n Location: ${campsite?.location}\nCapacity: ${campsite?.capacity}\nPrice per Night:$ ${campsite?.price}")}



                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Rate this Campsite!")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    Button(
                        onClick = {
                            if (campsite != null){
                                if (campsite.rating == 0.0) {campsite.rating = 1.0}
                                else {
                                    campsite.rating = (campsite.rating*campsite.ratingCount + 1)/(campsite.ratingCount + 1)
                                }

                                campsite.ratingCount = campsite.ratingCount + 1
                                CampRepo.addOrUpdateCampsite(campsite)
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White))
                    { Text("1") }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            if (campsite != null){
                                if (campsite.rating == 0.0) {campsite.rating = 2.0}
                                else {
                                    campsite.rating = (campsite.rating*campsite.ratingCount + 2)/(campsite.ratingCount + 1)
                                }

                                campsite.ratingCount = campsite.ratingCount + 1
                                CampRepo.addOrUpdateCampsite(campsite)
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White))
                    { Text("2") }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            if (campsite != null){
                                if (campsite.rating == 0.0) {campsite.rating = 3.0}
                                else {
                                    campsite.rating = (campsite.rating*campsite.ratingCount + 3)/(campsite.ratingCount + 1)
                                }

                                campsite.ratingCount = campsite.ratingCount + 1
                                CampRepo.addOrUpdateCampsite(campsite)
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White))
                    { Text("3") }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            if (campsite != null){
                                if (campsite.rating == 0.0) {campsite.rating = 4.0}
                                else {
                                    campsite.rating = (campsite.rating*campsite.ratingCount + 4)/(campsite.ratingCount + 1)
                                }

                                campsite.ratingCount = campsite.ratingCount + 1
                                CampRepo.addOrUpdateCampsite(campsite)
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White))
                    { Text("4") }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            if (campsite != null){
                                if (campsite.rating == 0.0) {campsite.rating = 5.0}
                                else {
                                    campsite.rating = (campsite.rating*campsite.ratingCount + 5)/(campsite.ratingCount + 1)
                                }

                                campsite.ratingCount = campsite.ratingCount + 1
                                CampRepo.addOrUpdateCampsite(campsite)
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White))
                    { Text("5") }

                }

                //TODO Implement lottery joining
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {Button(
                    onClick = {/* ### */},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White))
                { Text("View Lotteries") } }


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
            Button(
                onClick = { navController.navigate("owner_reservations_active") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Reservations", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_notifications") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Notifs", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_messages") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Messages", fontSize = 11.sp) }
            Button(
                onClick = { navController.navigate("owner_lotteries") { launchSingleTop = true } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text("Lotteries", fontSize = 11.sp) }
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