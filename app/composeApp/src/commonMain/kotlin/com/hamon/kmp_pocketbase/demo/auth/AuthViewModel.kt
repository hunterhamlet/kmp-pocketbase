package com.hamon.kmp_pocketbase.demo.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

internal sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

internal class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var name by mutableStateOf("")

    fun login() {
        viewModelScope.launch {
            uiState = AuthUiState.Loading
            repo.login(email, password)
                .onSuccess { uiState = AuthUiState.Success }
                .onFailure { uiState = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    fun register() {
        viewModelScope.launch {
            uiState = AuthUiState.Loading
            repo.register(email, password, name)
                .onSuccess { uiState = AuthUiState.Success }
                .onFailure { uiState = AuthUiState.Error(it.message ?: "Register failed") }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = AuthUiState.Loading
            repo.refresh()
                .onSuccess { uiState = AuthUiState.Success }
                .onFailure { uiState = AuthUiState.Error(it.message ?: "Refresh failed") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            uiState = AuthUiState.Idle
        }
    }

    fun resetState() {
        uiState = AuthUiState.Idle
    }
}
