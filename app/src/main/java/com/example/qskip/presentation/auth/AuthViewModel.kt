package com.example.qskip.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun login() {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter email and password")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.login(_uiState.value.email, _uiState.value.password)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                )
            }
        }
    }

    fun register() {
        if (_uiState.value.name.isBlank() || _uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.register(
                name = _uiState.value.name,
                email = _uiState.value.email,
                password = _uiState.value.password
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Registration failed"
                )
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
