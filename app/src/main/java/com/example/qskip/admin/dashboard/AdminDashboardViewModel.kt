package com.example.qskip.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Order
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.domain.repository.OrderRepository
import com.example.qskip.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val todaySales: Double = 0.0,
    val totalOrdersCount: Int = 0,
    val pendingExitsCount: Int = 0,
    val lowStockCount: Int = 0,
    val recentOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { orders ->
                val todayStart = System.currentTimeMillis() - (1000 * 60 * 60 * 24)
                val todayOrders = orders.filter { it.createdAt >= todayStart && (it.paymentStatus == "MOCK_SUCCESS" || it.paymentStatus == "SUCCESS") }
                val sales = todayOrders.sumOf { it.total }
                val pendingExits = orders.count { it.exitStatus == "READY" }

                val productsResult = productRepository.getProducts()
                val lowStockTotal = if (productsResult.isSuccess) {
                    productsResult.getOrDefault(emptyList()).count { it.stockQuantity <= it.lowStockThreshold }
                } else 0

                _uiState.value = _uiState.value.copy(
                    todaySales = sales,
                    totalOrdersCount = orders.size,
                    pendingExitsCount = pendingExits,
                    lowStockCount = lowStockTotal,
                    recentOrders = orders.take(5),
                    isLoading = false
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
