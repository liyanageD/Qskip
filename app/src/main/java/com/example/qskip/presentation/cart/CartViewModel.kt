package com.example.qskip.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Cart
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
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCart()
    }

    private fun loadCart() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            cartRepository.getCart().collect { cart ->
                _uiState.value = _uiState.value.copy(
                    cart = cart,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun updateQuantity(productId: String, variantId: String, quantity: Int) {
        viewModelScope.launch {
            val result = cartRepository.updateQuantity(productId, variantId, quantity)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message ?: "Failed to update quantity")
            }
        }
    }

    fun removeItem(productId: String, variantId: String) {
        viewModelScope.launch {
            val result = cartRepository.removeFromCart(productId, variantId)
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
            val result = cartRepository.setBudget(budget)
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
