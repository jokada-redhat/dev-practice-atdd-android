package com.example.atdd.auth

interface AuthRepository {
    fun login(request: LoginRequest): LoginResult
}
