package com.example.qskip.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Cart
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val budget: Double = 0.0,
    val cartTotal: Double = 0.0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            authRepository.getUserProfile().collect { user ->
                val currentBudget = user?.budget ?: 0.0
                _uiState.value = _uiState.value.copy(budget = currentBudget)
            }
        }
        viewModelScope.launch {
            cartRepository.getCart().collect { cart ->
                _uiState.value = _uiState.value.copy(
                    cartTotal = cart.total,
                    isLoading = false
                )
            }
        }
    }

    fun saveBudget(newBudget: Double) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.updateUserBudget(newBudget)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    budget = newBudget,
                    isLoading = false,
                    isSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to save budget"
                )
            }
        }
    }

    fun clearBudget() {
        saveBudget(0.0)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
