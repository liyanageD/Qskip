package com.example.qskip.admin.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminInventoryUiState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val filterLowStockOnly: Boolean = false,
    val filterOutOfStockOnly: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AdminInventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminInventoryUiState())
    val uiState: StateFlow<AdminInventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
    }

    fun loadInventory() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val productsResult = productRepository.getProducts()
            if (productsResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    products = productsResult.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = productsResult.exceptionOrNull()?.message ?: "Failed to load inventory"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleLowStockFilter() {
        val current = _uiState.value.filterLowStockOnly
        _uiState.value = _uiState.value.copy(
            filterLowStockOnly = !current,
            filterOutOfStockOnly = false
        )
    }

    fun toggleOutOfStockFilter() {
        val current = _uiState.value.filterOutOfStockOnly
        _uiState.value = _uiState.value.copy(
            filterOutOfStockOnly = !current,
            filterLowStockOnly = false
        )
    }

    fun updateStock(productId: String, newStock: Int) {
        if (newStock < 0) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = productRepository.updateStock(productId, newStock)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Stock updated successfully"
                )
                loadInventory()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update stock"
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }
}
