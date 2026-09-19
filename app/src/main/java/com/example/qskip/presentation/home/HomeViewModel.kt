package com.example.qskip.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.domain.repository.CartRepository
import com.example.qskip.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recommendedProducts: List<Product> = emptyList(),
    val budget: Double = 0.0,
    val cartTotal: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUserLoggedIn: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
        observeRecommendedProducts()
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { user ->
                _uiState.value = _uiState.value.copy(budget = user?.budget ?: 0.0)
            }
        }
        viewModelScope.launch {
            cartRepository.getCart().collect { cart ->
                _uiState.value = _uiState.value.copy(cartTotal = cart.total)
            }
        }
    }

    private fun checkAuthStatus() {
        _uiState.value = _uiState.value.copy(
            isUserLoggedIn = authRepository.isUserLoggedIn()
        )
    }

    private fun observeRecommendedProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            productRepository.getProductsFlow().collect { products ->
                _uiState.value = _uiState.value.copy(
                    recommendedProducts = products.take(10),
                    isLoading = false
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            checkAuthStatus()
        }
    }
}
