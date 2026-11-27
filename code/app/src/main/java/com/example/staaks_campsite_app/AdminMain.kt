package com.example.staaks_campsite_app

import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.data.model.admin_campsite

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.remote.core.operations.ColorTheme
import androidx.compose.remote.creation.Rc
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.example.staaks_campsite_app.data.model.Campsite
import com.example.staaks_campsite_app.session.SessionManager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

// light green 0xFF63B267
// dark green 0xFF1B5E20
// shamrock 0xFF009E60

public val adminTabs = listOf(
    Tab("admin_reviews", "Reviews", Icons.Default.Star),
    Tab("admin_users", "Users", Icons.Default.Person),
    Tab("all_notifications", "Notifications", Icons.Default.Notifications),
    Tab("all_messages", "Messages", Icons.Default.Textsms),
    Tab("images_screen", "Images", Icons.Default.Image)
)

/**
 * displays the admin home screen and the campsite list
 *
 * lets the admin open a campsite to view its details and deletion option
 *
 * @param navController controls navigation between screens
 * @param campsites the campsite list provided by CampsiteApp
 * @param isLoading whether campsite data is still loading
 * @param loadError the loading error message or null when there is no error
 */
@Composable
fun AdminMain(
    navController: NavController,
    campsites: List<Campsite>,
    isLoading: Boolean,
    loadError: String?
) {
    Scaffold(
        topBar = {
            UiTopRow(
                navController = navController,
                title = "Admin Main",
                homeRoute = "admin_main"
            )
        },
        bottomBar = { UiBottomRow(navController = navController, tabs = adminTabs) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {

                if (isLoading) {
                    Text("Loading campsites...")
                }

                loadError?.let { message ->

                    Text(
                        text = message,
                        color = Color.Red
                    )
                }

                if (
                    !isLoading &&
                    loadError == null &&
                    campsites.isEmpty()
                ) {
                    Text("No campsites found.")
                }
            }

            items(
                items = campsites,
                key = { campsite -> campsite.id }
            ) { campsite ->
                AdminCampsiteCard(
                    campsite = campsite,
                    onViewCampsite = {
                        navController.navigate("admin_campsite/${campsite.id}")
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminCampsiteCard(
    campsite: Campsite,
    onViewCampsite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = campsite.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StoredImage(
                    imageId = campsite.images.firstOrNull() ?: "",
                    contentDescription = campsite.name,
                    modifier = Modifier
                        .size(100.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📍 ${campsite.location}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "👥 ${campsite.capacity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "💰 \$${String.format("%.2f", campsite.price)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            GenericButton(
                text = "View Campsite",
                onClick = onViewCampsite
            )
        }
    }
}

@Composable
fun AdminTopRow(navController: NavController, title: String = "Admin Main") {
    var changeViewVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF1B5E20))
                .padding(16.dp)

        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                TopRowHomeButton(onClick = { navController.navigate("admin_main") })
            }

            Text(
                text = title,
                fontSize = 25.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                AdminTopRowToggleChangeView { changeViewVisible = !changeViewVisible }
            }
        }
        if (changeViewVisible) {
            AdminViewChanger(navController = navController)
        }
    }
}

@Composable
fun AdminViewChanger(navController: NavController) {
    val session by SessionManager.state.collectAsState()
    val currentView = session?.adminView

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
        AdminChangeViewButton(text = "Admin", onClick = { session?.adminView="admin"; navController.navigate("admin_main") }, selected = currentView=="admin")
        AdminChangeViewButton(text = "Owner", onClick = { session?.adminView="owner"; navController.navigate("owner_main") }, selected = currentView=="owner")
        AdminChangeViewButton(text = "Entree", onClick = { session?.adminView="entrant"; navController.navigate("entree_main") }, selected = currentView=="entrant")
    }
}

@Composable
fun TopRowHomeButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterStart
    ) {

        IconButton(
            onClick = { onClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier.size(30.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun AdminTopRowToggleChangeView(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterStart
    ) {

        IconButton(
            onClick = {
                onClick()
            }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Change View"
            )
        }
    }
}

@Composable
fun AdminBottomRow(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar(containerColor = Color(0xFF1B5E20)) {
        adminTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(text = tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF63B267),
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.8f),
                    unselectedTextColor = Color.White.copy(alpha = 0.8f),
                )
            )
        }
    }
}

@Composable
fun GenericButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF009E60),
            contentColor = Color.White
        )
    ){
        Text(text = text)
    }
}

@Composable
fun AdminChangeViewButton(text: String, onClick: () -> Unit, selected: Boolean = false) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) { Color.DarkGray } else { Color.LightGray },
            contentColor = Color.White
        )
    ){
        Text(text = text)
    }
}