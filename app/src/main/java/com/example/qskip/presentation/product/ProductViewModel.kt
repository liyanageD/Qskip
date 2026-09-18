package com.example.qskip.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.ProductVariant
import com.example.qskip.domain.model.ProductWithVariants
import com.example.qskip.domain.repository.CartRepository
import com.example.qskip.domain.repository.ProductRepository
import com.example.qskip.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val productData: ProductWithVariants? = null,
    val selectedVariant: ProductVariant? = null,
    val selectedQuantity: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val addToCartSuccess: Boolean = false,
    val isInWishlist: Boolean = false
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProductDetails()
    }

    private fun loadProductDetails() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = productRepository.getProductById(productId)
            val wishlistResult = wishlistRepository.isInWishlist(productId)
            
            if (result.isSuccess) {
                val data = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    productData = data,
                    selectedVariant = data?.variants?.firstOrNull(), // Auto-select first variant
                    isInWishlist = wishlistResult.getOrDefault(false),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load product"
                )
            }
        }
    }

    fun selectVariant(variant: ProductVariant) {
        _uiState.value = _uiState.value.copy(
            selectedVariant = variant,
            selectedQuantity = 1 // Reset quantity when variant changes
        )
    }

    fun updateQuantity(quantity: Int) {
        val variant = _uiState.value.selectedVariant
        if (variant != null && quantity in 1..variant.stock) {
            _uiState.value = _uiState.value.copy(selectedQuantity = quantity)
        }
    }

    fun addToCart() {
        val variant = _uiState.value.selectedVariant ?: return
        val quantity = _uiState.value.selectedQuantity
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = cartRepository.addToCart(productId, variant.variantId, quantity)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, addToCartSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to add to cart"
                )
            }
        }
    }

    fun resetAddToCartSuccess() {
        _uiState.value = _uiState.value.copy(addToCartSuccess = false)
    }

    fun toggleWishlist() {
        viewModelScope.launch {
            val currentStatus = _uiState.value.isInWishlist
            val newStatus = !currentStatus
            _uiState.value = _uiState.value.copy(isInWishlist = newStatus)

            val result = if (newStatus) {
                wishlistRepository.addToWishlist(productId)
            } else {
                wishlistRepository.removeFromWishlist(productId)
            }

            if (result.isFailure) {
                // Revert on failure
                _uiState.value = _uiState.value.copy(
                    isInWishlist = currentStatus,
                    error = result.exceptionOrNull()?.message ?: "Failed to update wishlist"
                )
            }
        }
    }
}
