package com.example.qskip.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.qskip.admin.dashboard.AdminDashboardScreen
import com.example.qskip.admin.exitverification.AdminExitScannerScreen
import com.example.qskip.admin.products.AdminAddProductScreen
import com.example.qskip.admin.products.AdminProductListScreen
import com.example.qskip.presentation.auth.LoginScreen
import com.example.qskip.presentation.auth.RegisterScreen
import com.example.qskip.presentation.cart.CartScreen
import com.example.qskip.presentation.checkout.CheckoutScreen
import com.example.qskip.presentation.exit.ExitQrScreen
import com.example.qskip.presentation.home.HomeScreen
import com.example.qskip.presentation.orders.OrderHistoryScreen
import com.example.qskip.presentation.product.ProductDetailsScreen
import com.example.qskip.presentation.receipt.ReceiptScreen
import com.example.qskip.presentation.receipt.SuccessScreen
import com.example.qskip.presentation.scanner.ScannerScreen
import com.example.qskip.presentation.wishlist.WishlistScreen

@Composable
fun QskipNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToScanner = {
                    navController.navigate(Screen.Scanner.route)
                },
                onNavigateToProduct = { productId ->
                    navController.navigate("${Screen.ProductDetails.route}/$productId")
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.OrderHistory.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminDashboard.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Scanner.route) {
            ScannerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProductScanned = { productId ->
                    navController.navigate("${Screen.ProductDetails.route}/$productId") {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.ProductDetails.route}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddToCart = { _ ->
                    navController.navigate(Screen.Cart.route)
                }
            )
        }

        composable(route = Screen.Cart.route) {
            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCheckout = {
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        composable(route = Screen.Wishlist.route) {
            WishlistScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProduct = { productId ->
                    navController.navigate("${Screen.ProductDetails.route}/$productId")
                }
            )
        }

        composable(route = Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentSuccess = {
                    navController.navigate(Screen.PaymentSuccess.route) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.PaymentSuccess.route) {
            SuccessScreen(
                onShowExitQr = {
                    navController.navigate(Screen.ExitQr.route)
                },
                onContinueShopping = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.ExitQr.route}?orderId={orderId}",
            arguments = listOf(navArgument("orderId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            ExitQrScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.OrderHistory.route) {
            OrderHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderClick = { orderId ->
                    navController.navigate("${Screen.Receipt.route}/$orderId")
                }
            )
        }

        composable(
            route = "${Screen.Receipt.route}/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            ReceiptScreen(
                orderId = orderId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onShowExitQr = { id ->
                    navController.navigate("${Screen.ExitQr.route}?orderId=$id")
                }
            )
        }

        // Admin Routes
        composable(route = Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToExitScanner = {
                    navController.navigate(Screen.AdminExitScanner.route)
                },
                onNavigateToProducts = {
                    navController.navigate(Screen.AdminProductList.route)
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.AdminProductList.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AdminExitScanner.route) {
            AdminExitScannerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AdminProductList.route) {
            AdminProductListScreen(
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AdminAddProduct.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AdminAddProduct.route) {
            AdminAddProductScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
