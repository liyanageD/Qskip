package com.example.qskip.admin.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.repository.AdminRepository
import com.example.qskip.domain.repository.RewardSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminRewardsUiState(
    val settings: RewardSettings = RewardSettings(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminRewardsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminRewardsUiState())
    val uiState: StateFlow<AdminRewardsUiState> = _uiState.asStateFlow()

    init {
        loadRewardSettings()
    }

    fun loadRewardSettings() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            adminRepository.getRewardSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
            }
        }
    }

    fun saveRewardSettings(pointsPerHundred: Int, discountPerPoint: Double, minPoints: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val newSettings = RewardSettings(pointsPerHundred, discountPerPoint, minPoints)
        viewModelScope.launch {
            val result = adminRepository.saveRewardSettings(newSettings)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    settings = newSettings,
                    isLoading = false,
                    isSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to save settings"
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(isSuccess = false, error = null)
    }
}
