package com.example.qskip.admin.flyers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.Flyer
import com.example.qskip.domain.repository.AdminRepository
import com.example.qskip.domain.repository.ImageStorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminFlyersUiState(
    val flyers: List<Flyer> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminFlyersViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val imageStorageRepository: ImageStorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFlyersUiState())
    val uiState: StateFlow<AdminFlyersUiState> = _uiState.asStateFlow()

    init {
        loadFlyers()
    }

    fun loadFlyers() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            adminRepository.getFlyers().collect { flyers ->
                _uiState.value = _uiState.value.copy(
                    flyers = flyers,
                    isLoading = false
                )
            }
        }
    }

    fun saveFlyerWithImage(
        flyerId: String = "",
        title: String,
        subtitle: String,
        imageBytes: ByteArray?,
        existingImageUrl: String,
        active: Boolean
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            var imageUrl = existingImageUrl

            if (imageBytes != null) {
                val fileName = "flyer_${System.currentTimeMillis()}.jpg"
                val uploadResult = imageStorageRepository.uploadImage(imageBytes, fileName)
                if (uploadResult.isSuccess) {
                    imageUrl = uploadResult.getOrNull()!!
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Flyer image upload failed: " + uploadResult.exceptionOrNull()?.message
                    )
                    return@launch
                }
            }

            val flyer = Flyer(
                flyerId = flyerId,
                title = title,
                subtitle = subtitle,
                imageUrl = imageUrl,
                active = active
            )

            val result = adminRepository.saveFlyer(flyer)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAddDialog = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to save flyer"
                )
            }
        }
    }

    fun toggleFlyer(flyerId: String, active: Boolean) {
        viewModelScope.launch {
            adminRepository.toggleFlyerStatus(flyerId, active)
        }
    }

    fun deleteFlyer(flyerId: String) {
        viewModelScope.launch {
            adminRepository.deleteFlyer(flyerId)
        }
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }
}
