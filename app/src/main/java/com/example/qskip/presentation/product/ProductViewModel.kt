package com.example.qskip.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Product
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
    val product: Product? = null,
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
            val wishlistResult = wishlistRepository.isInWishlist(productId)
            val inWishlist = wishlistResult.getOrDefault(false)

            productRepository.getProductByIdFlow(productId).collect { product ->
                if (product != null) {
                    _uiState.value = _uiState.value.copy(
                        product = product,
                        isInWishlist = inWishlist,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Product not available"
                    )
                }
            }
        }
    }

    fun updateQuantity(quantity: Int) {
        val product = _uiState.value.product
        if (product != null && quantity in 1..product.stockQuantity) {
            _uiState.value = _uiState.value.copy(selectedQuantity = quantity)
        }
    }

    fun addToCart() {
        val product = _uiState.value.product ?: return
        val quantity = _uiState.value.selectedQuantity
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = cartRepository.addToCart(product.productId, quantity)
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
                _uiState.value = _uiState.value.copy(
                    isInWishlist = currentStatus,
                    error = result.exceptionOrNull()?.message ?: "Failed to update wishlist"
                )
            }
        }
    }
}
