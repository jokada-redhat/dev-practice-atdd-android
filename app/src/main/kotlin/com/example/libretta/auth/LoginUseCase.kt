package com.example.libretta.auth

class LoginUseCase(private val authRepository: AuthRepository) {

    fun execute(request: LoginRequest): LoginResult {
        if (request.email.isBlank()) {
            return LoginResult.ValidationError("メールアドレスを入力してください")
        }
        if (request.password.isBlank()) {
            return LoginResult.ValidationError("パスワードを入力してください")
        }
        return authRepository.login(request)
    }
}
