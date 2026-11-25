package com.example.staaks_campsite_app

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staaks_campsite_app.session.SessionManager

data class Tab(val route: String, val label: String, val icon: ImageVector)

// OLD CODE
@Composable
fun UiTopRow2(
    navController: NavController,
    title: String = "Title",
    homeRoute: String = "",
    isAdmin: Boolean = false
) {
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
                TopRowHomeButton(onClick = { navController.navigate(homeRoute) })
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
                if (isAdmin) {
                    AdminTopRowToggleChangeView { changeViewVisible = !changeViewVisible }
                }
            }
        }
        if (changeViewVisible) {
            AdminViewChanger(navController = navController)
        }
    }
}

@Composable
fun UiTopRow(
    navController: NavController,
    title: String = "Title",
    homeRoute: String = "",
    isAdmin: Boolean = false
) {
    var changeViewVisible by remember { mutableStateOf(false) }
    val session by SessionManager.state.collectAsState()
    val isAdmin = session?.isAdmin

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
            .background(
                brush = Brush.verticalGradient(
                    // darker 0xFF1B5E20
                    // dark 0xFF3E8E45
                    // lighter 0xFF7BC67F
                    // light 0xFF63B267
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF3E8E45))
                ),
                shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
            )
            .statusBarsPadding()
            .padding(vertical = 0.dp, horizontal = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Transparent)
                    .padding(4.dp, bottom = 8.dp)

            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TopRowHomeButton(onClick = { navController.navigate(homeRoute) })
                }

                Text(
                    text = title,
                    fontSize = 25.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Box(
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (isAdmin != null && isAdmin) {
                        AdminTopRowToggleChangeView { changeViewVisible = !changeViewVisible }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f).padding(8.dp)
                        .clickable {
                            navController.navigate("profile_screen")
                        },
                    contentAlignment = Alignment.CenterEnd

                ) {
                    UserProfileImagePreview(size = 40.dp, imageId="")
                }
            }
            if (changeViewVisible) {
                AdminViewChanger(navController = navController)
            }
        }
    }
}

// OLD CODE
@Composable
fun UiBottomRow2(
    navController: NavController,
    tabs: List<Tab>
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar(containerColor = Color(0xFF1B5E20)) {
        tabs.forEach { tab ->
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
fun UiBottomRow(
    navController: NavController,
    tabs: List<Tab>
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .background(
                brush = Brush.verticalGradient(
                    // darker 0xFF1B5E20
                    // dark 0xFF3E8E45
                    // lighter 0xFF7BC67F
                    // light 0xFF63B267
                    colors = listOf(Color(0xFF3E8E45), Color(0xFF1B5E20))
                ),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        NavigationBar(containerColor = Color.Transparent, modifier = Modifier.padding(all = 0.dp)) {
            tabs.forEach { tab ->
                val tabGradient = Brush.radialGradient(
                    colors = listOf(Color(0xFF7BC67F), Color.Transparent)
                )

                NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = if (currentRoute == tab.route) {tabGradient} else {
                                        SolidColor(Color.Transparent)
                                    }
                                )
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }



                    },
                    label = { Text(text = tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        //indicatorColor = Color(0xFF63B267),
                        indicatorColor = Color.Transparent,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.8f),
                        unselectedTextColor = Color.White.copy(alpha = 0.8f),
                    )
                )
            }
        }
    }
}
