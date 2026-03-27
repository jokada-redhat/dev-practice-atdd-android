package com.example.atdd.auth

sealed class LoginResult {
    data class Success(val token: String, val displayName: String) : LoginResult()
    data class Failure(val errorMessage: String) : LoginResult()
    data class ValidationError(val message: String) : LoginResult()
}
