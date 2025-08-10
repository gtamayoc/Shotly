package com.example.shotly.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shotly.components.AllCapturesScreen
import com.example.shotly.components.EditorScreen
import com.example.shotly.components.HomeScreen
import com.example.shotly.components.SettingsScreen
import com.example.shotly.ui.theme.ShotlyTheme
import com.example.shotly.viewModel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(sharedVM: SharedViewModel) {
    val navController = rememberNavController()

    ShotlyTheme {
        // No es necesario Surface aquí si Scaffold ya maneja el fondo
        val currentBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = currentBackStackEntry?.destination?.route

        // Decide en qué rutas mostrar barras
        val showTopBar = when (currentRoute) {
            Screen.Home.route, Screen.AllScreens.route, Screen.Editor.route, Screen.Settings.route -> true
            else -> false
        }
        val showBottomBar = showTopBar

        Scaffold(
            topBar = {
                if (showTopBar) {
                    CenterAlignedTopAppBar(
                        title = { Text(topBarTitleForRoute(currentRoute)) }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == Screen.Home.route,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                            label = { Text("Inicio") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.AllScreens.route,
                            onClick = {
                                navController.navigate(Screen.AllScreens.route) {
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(Icons.Default.List, contentDescription = "Capturas") },
                            label = { Text("Capturas") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Settings.route,
                            onClick = {
                                navController.navigate(Screen.Settings.route) {
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                            label = { Text("Ajustes") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Importante: aplicar el padding del Scaffold al NavHost
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen(sharedVM) }
                composable(Screen.AllScreens.route) { AllCapturesScreen() }
                composable(Screen.Editor.route) { EditorScreen(navController) }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }
    }
}

@Composable
private fun topBarTitleForRoute(route: String?): String = when (route) {
    Screen.Home.route -> "Inicio"
    Screen.AllScreens.route -> "Capturas"
    Screen.Editor.route -> "Editor"
    Screen.Settings.route -> "Ajustes"
    else -> ""
}