package com.example.libretta.auth

class StubAuthRepository : AuthRepository {

    override fun login(request: LoginRequest): LoginResult {
        return if (request.email == EMAIL && request.password == PASSWORD) {
            LoginResult.Success(token = "stub-token", displayName = "司書 太郎")
        } else {
            LoginResult.Failure("メールアドレスまたはパスワードが正しくありません")
        }
    }

    companion object {
        const val EMAIL = "librarian@example.com"
        const val PASSWORD = "password"
    }
}
