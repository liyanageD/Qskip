package com.example.qskip.admin.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qskip.domain.model.User
import com.example.qskip.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminCustomersUiState(
    val customers: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminCustomersViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCustomersUiState())
    val uiState: StateFlow<AdminCustomersUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            adminRepository.getAllCustomers().collect { users ->
                _uiState.value = _uiState.value.copy(
                    customers = users,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleCustomerAccountStatus(user: User) {
        val newStatus = if (user.accountStatus == "ACTIVE") "SUSPENDED" else "ACTIVE"
        viewModelScope.launch {
            adminRepository.updateCustomerStatus(user.uid, newStatus)
        }
    }
}
