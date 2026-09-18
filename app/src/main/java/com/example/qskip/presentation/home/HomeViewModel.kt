package com.example.qskip.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recommendedProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUserLoggedIn: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
        loadRecommendedProducts()
    }

    private fun checkAuthStatus() {
        _uiState.value = _uiState.value.copy(
            isUserLoggedIn = authRepository.isUserLoggedIn()
        )
    }

    fun loadRecommendedProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = productRepository.getRecommendedProducts()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    recommendedProducts = result.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Failed to load products"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            checkAuthStatus()
        }
    }
}
