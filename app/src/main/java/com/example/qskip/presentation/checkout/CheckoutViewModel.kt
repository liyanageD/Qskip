package com.example.qskip.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Order
import com.example.qskip.domain.repository.CheckoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    val isPaymentProcessing: Boolean = false,
    val paymentSuccess: Boolean = false,
    val error: String? = null,
    val availableRewardPoints: Int = 0
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val checkoutRepository: CheckoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        createOrder()
        loadRewardPoints()
    }

    private fun createOrder() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = checkoutRepository.createOrderFromCart()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    order = result.getOrNull(),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to create order"
                )
            }
        }
    }

    private fun loadRewardPoints() {
        viewModelScope.launch {
            checkoutRepository.getUserRewardPoints().collect { points ->
                _uiState.value = _uiState.value.copy(availableRewardPoints = points)
            }
        }
    }

    fun applyRewardPoints(points: Int) {
        viewModelScope.launch {
            val result = checkoutRepository.applyRewardPoints(points)
            if (result.isSuccess) {
                // Re-create the order to calculate new totals based on the newly applied points
                createOrder()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to apply rewards"
                )
            }
        }
    }

    fun processMockPayment() {
        val currentOrder = _uiState.value.order ?: return
        
        _uiState.value = _uiState.value.copy(isPaymentProcessing = true, error = null)
        viewModelScope.launch {
            val result = checkoutRepository.processMockPayment(currentOrder.orderId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isPaymentProcessing = false,
                    paymentSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isPaymentProcessing = false,
                    error = result.exceptionOrNull()?.message ?: "Payment failed. Please try again."
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
