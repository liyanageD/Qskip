package com.example.qskip.admin.products

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.model.ProductVariant
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
    val isLoading: Boolean = false,
    val isSaveSuccess: Boolean = false,
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
        loadProducts()
    }

    fun loadProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = productRepository.getProducts()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    products = result.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load products"
                )
            }
        }
    }

    fun saveProductWithImage(
        name: String,
        productCode: String,
        description: String,
        basePrice: Double,
        imageBytes: ByteArray?,
        variants: List<ProductVariant>
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            var imageUrls = emptyList<String>()

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
                productCode = productCode,
                name = name,
                description = description,
                basePrice = basePrice,
                imageUrls = imageUrls,
                active = true
            )

            val saveResult = productRepository.saveProduct(product, variants)
            if (saveResult.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSaveSuccess = true)
                loadProducts()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = saveResult.exceptionOrNull()?.message ?: "Failed to save product"
                )
            }
        }
    }

    fun generateProductQr(productCode: String) {
        val bitmap = QrGeneratorUtil.generateQrBitmap(productCode)
        _uiState.value = _uiState.value.copy(generatedQrBitmap = bitmap)
    }

    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaveSuccess = false, error = null)
    }
}
