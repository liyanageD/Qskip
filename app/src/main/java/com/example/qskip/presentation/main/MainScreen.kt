package com.example.qskip.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qskip.presentation.components.BottomNavBar
import com.example.qskip.presentation.navigation.QskipNavGraph
import com.example.qskip.presentation.navigation.Screen

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val splashState by viewModel.splashState.collectAsState()

    when (val state = splashState) {
        is SplashState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is SplashState.Success -> {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

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
                    startDestination = state.startDestination,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
