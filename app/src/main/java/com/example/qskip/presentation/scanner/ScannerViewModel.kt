package com.example.qskip.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val scannedProductCode: String? = null,
    val manualInputCode: String = "",
    val showManualInputDialog: Boolean = false
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onQrCodeScanned(code: String) {
        // Prevent double scanning
        if (_uiState.value.isLoading || _uiState.value.scannedProductCode == code) return
        
        verifyProductCode(code)
    }

    fun onManualInputChanged(code: String) {
        _uiState.value = _uiState.value.copy(manualInputCode = code, error = null)
    }

    fun setShowManualInputDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showManualInputDialog = show, error = null)
    }

    fun submitManualCode() {
        val code = _uiState.value.manualInputCode.trim()
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Product code cannot be empty")
            return
        }
        setShowManualInputDialog(false)
        verifyProductCode(code)
    }

    private fun verifyProductCode(code: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = productRepository.getProductByCode(code)
            if (result.isSuccess) {
                // If it exists, pass the actual ID back to the UI to navigate
                val product = result.getOrNull()
                if (product != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scannedProductCode = product.productId
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Product details missing."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Invalid QR code or product not found"
                )
            }
        }
    }
    
    fun resetScanState() {
        _uiState.value = _uiState.value.copy(scannedProductCode = null, error = null)
    }
}
