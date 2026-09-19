package com.example.qskip.admin.exitverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.ExitToken
import com.example.qskip.domain.model.Order
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminExitScannerUiState(
    val scannedToken: ExitToken? = null,
    val scannedOrder: Order? = null,
    val isAlreadyProcessed: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val flagReason: String = "",
    val showFlagDialog: Boolean = false
)

@HiltViewModel
class AdminExitScannerViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminExitScannerUiState())
    val uiState: StateFlow<AdminExitScannerUiState> = _uiState.asStateFlow()

    fun onExitQrScanned(rawInput: String) {
        val tokenIdOrOrderId = rawInput.trim()
        if (_uiState.value.isLoading || _uiState.value.scannedToken?.tokenId == tokenIdOrOrderId) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val tokenResult = orderRepository.getExitToken(tokenIdOrOrderId)
            if (tokenResult.isSuccess) {
                val token = tokenResult.getOrNull()!!
                _uiState.value = _uiState.value.copy(scannedToken = token)

                orderRepository.getOrderById(token.orderId).collect { order ->
                    val isProcessed = token.status == "VERIFIED" || 
                                     token.status == "FLAGGED" || 
                                     order?.exitStatus == "VERIFIED" || 
                                     order?.exitStatus == "FLAGGED" || 
                                     order?.orderStatus == "COMPLETED"

                    _uiState.value = _uiState.value.copy(
                        scannedOrder = order,
                        isAlreadyProcessed = isProcessed,
                        isLoading = false
                    )
                }
            } else {
                // Attempt direct order lookup by order ID
                orderRepository.getOrderById(tokenIdOrOrderId).collect { order ->
                    if (order != null) {
                        val token = ExitToken(
                            tokenId = order.exitTokenId ?: tokenIdOrOrderId,
                            orderId = order.orderId,
                            userId = order.userId,
                            status = order.exitStatus,
                            verifiedAt = order.verifiedAt,
                            verifiedBy = order.verifiedBy,
                            flaggedReason = order.flaggedReason
                        )
                        val isProcessed = order.exitStatus == "VERIFIED" || 
                                         order.exitStatus == "FLAGGED" || 
                                         order.orderStatus == "COMPLETED"

                        _uiState.value = _uiState.value.copy(
                            scannedToken = token,
                            scannedOrder = order,
                            isAlreadyProcessed = isProcessed,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = tokenResult.exceptionOrNull()?.message ?: "Invalid Exit QR Token or Order not found"
                        )
                    }
                }
            }
        }
    }

    fun verifyExit() {
        val token = _uiState.value.scannedToken ?: return
        if (_uiState.value.isAlreadyProcessed) return
        val staffId = authRepository.getCurrentUserId() ?: "STAFF_DEMO"

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = orderRepository.verifyExit(token.tokenId, staffId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Verification failed"
                )
            }
        }
    }

    fun flagExit() {
        val token = _uiState.value.scannedToken ?: return
        if (_uiState.value.isAlreadyProcessed) return
        val staffId = authRepository.getCurrentUserId() ?: "STAFF_DEMO"
        val reason = _uiState.value.flagReason.ifBlank { "Items mismatch" }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null, showFlagDialog = false)
        viewModelScope.launch {
            val result = orderRepository.flagExit(token.tokenId, staffId, reason)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Flag operation failed"
                )
            }
        }
    }

    fun setShowFlagDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFlagDialog = show)
    }

    fun onFlagReasonChanged(reason: String) {
        _uiState.value = _uiState.value.copy(flagReason = reason)
    }

    fun resetScannerState() {
        _uiState.value = AdminExitScannerUiState()
    }
}
