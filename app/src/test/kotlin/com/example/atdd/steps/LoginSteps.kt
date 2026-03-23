package com.example.atdd.steps

import com.example.atdd.auth.AuthRepository
import com.example.atdd.auth.LoginRequest
import com.example.atdd.auth.LoginResult
import com.example.atdd.auth.LoginUseCase
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

class LoginSteps {

    private val registeredUsers = mutableMapOf<String, String>()
    private lateinit var loginResult: LoginResult
    private lateinit var loginUseCase: LoginUseCase

    @Given("ログインAPIが利用可能である")
    fun loginApiIsAvailable() {
        val repository = object : AuthRepository {
            override fun login(request: LoginRequest): LoginResult {
                val registeredPassword = registeredUsers[request.email]
                    ?: return LoginResult.Failure("メールアドレスまたはパスワードが正しくありません")
                return if (registeredPassword == request.password) {
                    LoginResult.Success("test-token-${request.email}")
                } else {
                    LoginResult.Failure("メールアドレスまたはパスワードが正しくありません")
                }
            }
        }
        loginUseCase = LoginUseCase(repository)
    }

    @Given("ユーザー {string} がパスワード {string} で登録されている")
    fun userIsRegistered(email: String, password: String) {
        registeredUsers[email] = password
    }

    @When("メールアドレス {string} とパスワード {string} でログインする")
    fun loginWith(email: String, password: String) {
        if (!::loginUseCase.isInitialized) {
            // バリデーションテスト用: APIなしでもUseCaseを生成
            val dummyRepository = object : AuthRepository {
                override fun login(request: LoginRequest): LoginResult = LoginResult.Failure("未設定")
            }
            loginUseCase = LoginUseCase(dummyRepository)
        }
        loginResult = loginUseCase.execute(LoginRequest(email, password))
    }

    @Then("ログインが成功する")
    fun loginSucceeds() {
        assertTrue(
            "ログイン結果が Success であるべき: $loginResult",
            loginResult is LoginResult.Success
        )
    }

    @Then("ログインが失敗する")
    fun loginFails() {
        assertTrue(
            "ログイン結果が Failure であるべき: $loginResult",
            loginResult is LoginResult.Failure
        )
    }

    @And("アクセストークンが返される")
    fun accessTokenIsReturned() {
        val success = loginResult as LoginResult.Success
        assertNotNull("トークンが null であってはならない", success.token)
        assertTrue("トークンが空であってはならない", success.token.isNotBlank())
    }

    @And("エラーメッセージ {string} が返される")
    fun errorMessageIsReturned(expectedMessage: String) {
        val failure = loginResult as LoginResult.Failure
        assertEquals(expectedMessage, failure.errorMessage)
    }

    @Then("バリデーションエラー {string} が発生する")
    fun validationErrorOccurs(expectedMessage: String) {
        assertTrue(
            "ログイン結果が ValidationError であるべき: $loginResult",
            loginResult is LoginResult.ValidationError
        )
        val error = loginResult as LoginResult.ValidationError
        assertEquals(expectedMessage, error.message)
    }
}
