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
        if (orderId != null) {
            loadOrderAndGenerateQr(orderId)
        } else {
            loadLatestOrder()
        }
    }

    private fun loadOrderAndGenerateQr(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            orderRepository.getOrderById(id).collect { order ->
                if (order != null) {
                    val tokenId = order.exitTokenId ?: ""
                    val bitmap = QrGeneratorUtil.generateQrBitmap(tokenId)
                    _uiState.value = _uiState.value.copy(
                        order = order,
                        qrBitmap = bitmap,
                        isLoading = false
                    )
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
                if (latest != null && latest.exitTokenId != null) {
                    val bitmap = QrGeneratorUtil.generateQrBitmap(latest.exitTokenId)
                    _uiState.value = _uiState.value.copy(
                        order = latest,
                        qrBitmap = bitmap,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No active exit QR code found"
                    )
                }
            }
        }
    }
}
