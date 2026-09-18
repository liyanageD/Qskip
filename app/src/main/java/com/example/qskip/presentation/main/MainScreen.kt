package com.example.qskip.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qskip.presentation.components.BottomNavBar
import com.example.qskip.presentation.navigation.QskipNavGraph
import com.example.qskip.presentation.navigation.Screen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define routes that should show the bottom bar
    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Cart.route,
        Screen.Wishlist.route
    )
    
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        QskipNavGraph(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
