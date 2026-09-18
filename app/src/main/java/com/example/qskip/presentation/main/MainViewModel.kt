package com.example.qskip.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    data class Success(val startDestination: String) : SplashState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        if (!authRepository.isUserLoggedIn()) {
            _splashState.value = SplashState.Success(Screen.Login.route)
            return
        }

        viewModelScope.launch {
            val userResult = authRepository.getUserProfileOnce()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                if (user != null && (user.role == "ADMIN" || user.role == "STAFF")) {
                    _splashState.value = SplashState.Success(Screen.AdminDashboard.route)
                } else {
                    _splashState.value = SplashState.Success(Screen.Home.route)
                }
            } else {
                _splashState.value = SplashState.Success(Screen.Home.route)
            }
        }
    }
}
