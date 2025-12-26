package com.example.shotly.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shotly.ui.home.HomeScreen
import com.example.shotly.ui.screenshots.ScreenshotsScreen
import com.example.shotly.ui.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when (currentRoute) {
                            "home" -> "Shotly"
                            "screenshots" -> "Capturas"
                            "settings" -> "Ajustes"
                            else -> "Shotly"
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = currentRoute == "screenshots",
                    onClick = {
                        navController.navigate("screenshots") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Capturas") }
                )

                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Ajustes") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("screenshots") { ScreenshotsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}