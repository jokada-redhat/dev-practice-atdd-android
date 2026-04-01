package com.example.libretta.auth

interface AuthRepository {
    fun login(request: LoginRequest): LoginResult
}
