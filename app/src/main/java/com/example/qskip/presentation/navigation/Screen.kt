package com.example.qskip.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object ProductDetails : Screen("product_details")
    object Cart : Screen("cart")
}
