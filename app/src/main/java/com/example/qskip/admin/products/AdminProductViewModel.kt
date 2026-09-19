package com.example.qskip.admin.products

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.repository.ImageStorageRepository
import com.example.qskip.domain.repository.ProductRepository
import com.example.qskip.utils.QrGeneratorUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminProductUiState(
    val products: List<Product> = emptyList(),
    val editingProduct: Product? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val generatedQrBitmap: Bitmap? = null
)

@HiltViewModel
class AdminProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val imageStorageRepository: ImageStorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductUiState())
    val uiState: StateFlow<AdminProductUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
    }

    private fun observeProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            productRepository.getProductsFlow().collect { products ->
                _uiState.value = _uiState.value.copy(
                    products = products,
                    isLoading = false
                )
            }
        }
    }

    fun loadProductForEdit(productId: String) {
        if (productId.isBlank()) {
            _uiState.value = _uiState.value.copy(editingProduct = null)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = productRepository.getProductById(productId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    editingProduct = result.getOrNull(),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load product details"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun saveProductWithImage(
        productId: String = "",
        name: String,
        productCode: String,
        description: String,
        category: String,
        size: String,
        color: String,
        price: Double,
        stockQuantity: Int,
        imageBytes: ByteArray?,
        existingImageUrls: List<String>,
        active: Boolean
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            var imageUrls = existingImageUrls

            if (imageBytes != null) {
                val fileName = "prod_${System.currentTimeMillis()}.jpg"
                val uploadResult = imageStorageRepository.uploadImage(imageBytes, fileName)
                if (uploadResult.isSuccess) {
                    imageUrls = listOf(uploadResult.getOrNull()!!)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Image upload failed: " + uploadResult.exceptionOrNull()?.message
                    )
                    return@launch
                }
            }

            val product = Product(
                productId = productId,
                productCode = productCode,
                name = name,
                description = description,
                categoryId = category,
                size = size,
                color = color,
                price = price,
                stockQuantity = stockQuantity,
                imageUrls = imageUrls,
                active = active
            )

            val saveResult = productRepository.saveProduct(product)
            if (saveResult.isSuccess) {
                val msg = if (productId.isBlank()) "Product created successfully" else "Product updated successfully"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaveSuccess = true,
                    successMessage = msg
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = saveResult.exceptionOrNull()?.message ?: "Failed to save product"
                )
            }
        }
    }

    fun deleteProduct(productId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = productRepository.deleteProduct(productId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Product deactivated successfully"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to delete product"
                )
            }
        }
    }

    fun generateProductQr(productCode: String) {
        val bitmap = QrGeneratorUtil.generateQrBitmap(productCode)
        _uiState.value = _uiState.value.copy(generatedQrBitmap = bitmap)
    }

    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaveSuccess = false, successMessage = null, error = null)
    }
}
