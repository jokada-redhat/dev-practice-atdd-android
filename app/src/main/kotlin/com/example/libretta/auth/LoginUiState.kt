package com.example.libretta.auth

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val displayName: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
