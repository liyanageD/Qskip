package com.example.qskip.admin.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Review
import com.example.qskip.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminReviewsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReviewsUiState())
    val uiState: StateFlow<AdminReviewsUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            adminRepository.getReviews().collect { reviews ->
                _uiState.value = _uiState.value.copy(
                    reviews = reviews,
                    isLoading = false
                )
            }
        }
    }

    fun toggleReviewVisibility(reviewId: String, active: Boolean) {
        viewModelScope.launch {
            adminRepository.toggleReviewVisibility(reviewId, active)
        }
    }
}
