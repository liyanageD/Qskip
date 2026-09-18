package com.example.qskip.presentation.exit

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Order
import com.example.qskip.domain.repository.OrderRepository
import com.example.qskip.utils.QrGeneratorUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExitQrUiState(
    val order: Order? = null,
    val qrBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExitQrViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val orderId: String? = savedStateHandle["orderId"]

    private val _uiState = MutableStateFlow(ExitQrUiState())
    val uiState: StateFlow<ExitQrUiState> = _uiState.asStateFlow()

    init {
        val targetId = orderId?.trim()
        if (!targetId.isNullOrBlank()) {
            loadOrderAndGenerateQr(targetId)
        } else {
            loadLatestOrder()
        }
    }

    private fun loadOrderAndGenerateQr(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            orderRepository.getOrderById(id).collect { order ->
                if (order != null) {
                    processOrderExitPass(order)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Order not found"
                    )
                }
            }
        }
    }

    private fun loadLatestOrder() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            orderRepository.getUserOrders().collect { orders ->
                val latest = orders.firstOrNull()
                if (latest != null) {
                    processOrderExitPass(latest)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No active orders found"
                    )
                }
            }
        }
    }

    private fun processOrderExitPass(order: Order) {
        // Verify order payment eligibility
        val isPaid = order.paymentStatus == "MOCK_SUCCESS" || 
                     order.paymentStatus == "SUCCESS" || 
                     order.orderStatus == "PAID" || 
                     order.orderStatus == "COMPLETED"

        if (!isPaid) {
            _uiState.value = _uiState.value.copy(
                order = order,
                qrBitmap = null,
                isLoading = false,
                error = "Exit QR is not available for this order (Payment Pending)."
            )
            return
        }

        val tokenId = if (!order.exitTokenId.isNullOrBlank()) order.exitTokenId else order.orderId
        val bitmap = QrGeneratorUtil.generateQrBitmap(tokenId)

        if (bitmap != null) {
            _uiState.value = _uiState.value.copy(
                order = order,
                qrBitmap = bitmap,
                isLoading = false,
                error = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                order = order,
                qrBitmap = null,
                isLoading = false,
                error = "Exit QR code is not available for this order."
            )
        }
    }
}
