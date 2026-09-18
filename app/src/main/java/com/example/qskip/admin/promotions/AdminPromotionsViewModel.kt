package com.example.qskip.admin.promotions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Promotion
import com.example.qskip.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminPromotionsUiState(
    val promotions: List<Promotion> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminPromotionsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPromotionsUiState())
    val uiState: StateFlow<AdminPromotionsUiState> = _uiState.asStateFlow()

    init {
        loadPromotions()
    }

    fun loadPromotions() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            adminRepository.getPromotions().collect { promos ->
                _uiState.value = _uiState.value.copy(
                    promotions = promos,
                    isLoading = false
                )
            }
        }
    }

    fun savePromotion(title: String, code: String, type: String, value: Double) {
        if (title.isBlank() || code.isBlank()) return
        val promo = Promotion(
            title = title,
            code = code,
            discountType = type,
            discountValue = value,
            active = true
        )
        _uiState.value = _uiState.value.copy(showAddDialog = false, isLoading = true)
        viewModelScope.launch {
            adminRepository.savePromotion(promo)
            loadPromotions()
        }
    }

    fun togglePromotion(promotionId: String, active: Boolean) {
        viewModelScope.launch {
            adminRepository.togglePromotionStatus(promotionId, active)
        }
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }
}
