package com.pietropuluche.veciapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pietropuluche.veciapp.data.local.SessionManager
import com.pietropuluche.veciapp.data.model.LoginRequest
import com.pietropuluche.veciapp.data.model.RegisterRequest
import com.pietropuluche.veciapp.data.repository.VeciAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val fullName: String = "",
    val errorMessage: String = "",
    val infoMessage: String = ""
)

class AuthViewModel(
    private val repository: VeciAppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = sessionManager.isLoggedIn(),
            fullName = sessionManager.fullName()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "", infoMessage = "")
            repository.login(LoginRequest(email.trim(), password))
                .onSuccess { response ->
                    sessionManager.saveSession(response.userId, response.fullName, response.token)
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isLoggedIn = true,
                        fullName = response.fullName,
                        infoMessage = "Sesion iniciada"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message.orEmpty()
                    )
                }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        documentNumber: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "", infoMessage = "")
            repository.register(
                RegisterRequest(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    password = password,
                    phone = phone.trim(),
                    documentNumber = documentNumber.trim().ifBlank { null }
                )
            ).onSuccess { response ->
                sessionManager.saveSession(response.userId, response.fullName, response.token)
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isLoggedIn = true,
                    fullName = response.fullName,
                    infoMessage = "Cuenta creada correctamente"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message.orEmpty()
                )
            }
        }
    }

    fun logout() {
        sessionManager.clear()
        _uiState.value = AuthUiState()
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "", infoMessage = "")
    }
}
