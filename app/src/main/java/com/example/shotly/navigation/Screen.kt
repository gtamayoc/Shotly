package com.example.shotly.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AllScreens : Screen("all_screens")
    object Editor : Screen("editor")
    object Settings : Screen("settings")
}
