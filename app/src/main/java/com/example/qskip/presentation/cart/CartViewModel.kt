package com.example.qskip.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Cart
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val cart: Cart = Cart(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showBudgetDialog: Boolean = false
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCart()
        observeUserBudget()
    }

    private fun loadCart() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            cartRepository.getCart().collect { cart ->
                val currentBudget = _uiState.value.cart.budget
                _uiState.value = _uiState.value.copy(
                    cart = cart.copy(budget = if (currentBudget > 0) currentBudget else cart.budget),
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun observeUserBudget() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { user ->
                val budget = user?.budget ?: 0.0
                _uiState.value = _uiState.value.copy(
                    cart = _uiState.value.cart.copy(budget = budget)
                )
            }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            val result = cartRepository.updateQuantity(productId, quantity)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message ?: "Failed to update quantity")
            }
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            val result = cartRepository.removeFromCart(productId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message ?: "Failed to remove item")
            }
        }
    }

    fun setShowBudgetDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBudgetDialog = show)
    }

    fun updateBudget(budget: Double) {
        viewModelScope.launch {
            val result = authRepository.updateUserBudget(budget)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(showBudgetDialog = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to set budget"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
