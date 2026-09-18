package com.example.qskip.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.WishlistItem
import com.example.qskip.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistUiState(
    val items: List<WishlistItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            wishlistRepository.getWishlist().collect { items ->
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false
                )
            }
        }
    }

    fun removeFromWishlist(productId: String) {
        viewModelScope.launch {
            val result = wishlistRepository.removeFromWishlist(productId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to remove item"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
