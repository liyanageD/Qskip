package com.example.qskip.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object ProductDetails : Screen("product_details")
    object Cart : Screen("cart")
    object Wishlist : Screen("wishlist")
    object Checkout : Screen("checkout")
    object PaymentSuccess : Screen("payment_success")
    object ExitQr : Screen("exit_qr")
    object OrderHistory : Screen("order_history")
    object Receipt : Screen("receipt")
    object Budget : Screen("budget")
    
    // Admin routes
    object AdminLogin : Screen("admin_login")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminExitScanner : Screen("admin_exit_scanner")
    object AdminProductList : Screen("admin_product_list")
    object AdminAddProduct : Screen("admin_add_product")
    object AdminInventory : Screen("admin_inventory")
    object AdminOrders : Screen("admin_orders")
    object AdminCustomers : Screen("admin_customers")
    object AdminPromotions : Screen("admin_promotions")
    object AdminRewards : Screen("admin_rewards")
    object AdminFlyers : Screen("admin_flyers")
}
