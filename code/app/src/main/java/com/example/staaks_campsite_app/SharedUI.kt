package com.example.staaks_campsite_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun UiTopRow(
    navController: NavController,
    title: String = "Title",
    homeRoute: String = ""
) {
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
                IconButton(onClick = {
                }) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }
            }
        }
    }
}

@Composable
fun UiBottomRow(
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