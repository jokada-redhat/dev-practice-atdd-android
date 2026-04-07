package com.example.libretta.steps

import com.example.libretta.auth.AuthRepository
import com.example.libretta.auth.LoginRequest
import com.example.libretta.auth.LoginResult
import com.example.libretta.auth.LoginUseCase
import io.cucumber.datatable.DataTable
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
                    LoginResult.Success("test-token-${request.email}", "テストユーザー")
                } else {
                    LoginResult.Failure("メールアドレスまたはパスワードが正しくありません")
                }
            }
        }
        loginUseCase = LoginUseCase(repository)
    }

    @Given("以下の認証情報でユーザーが登録されている:")
    fun usersAreRegistered(dataTable: DataTable) {
        val row = dataTable.asMaps().first()
        registeredUsers[row["email"]!!] = row["password"]!!
    }

    @When("以下の認証情報でログインする")
    fun loginWith(dataTable: DataTable) {
        val row = dataTable.asMaps().first()
        val email = row["email"] ?: ""
        val password = row["password"] ?: ""
        if (!::loginUseCase.isInitialized) {
            // バリデーションテスト用: APIなしでもUseCaseを生成
            val dummyRepository = object : AuthRepository {
                override fun login(request: LoginRequest): LoginResult = LoginResult.Failure("未設定")
            }
            loginUseCase = LoginUseCase(dummyRepository)
        }
        loginResult = loginUseCase.execute(LoginRequest(email, password))

        // エラーメッセージを共通変数に設定
        when (loginResult) {
            is LoginResult.Failure -> {
                CommonSteps.lastErrorMessage = (loginResult as LoginResult.Failure).errorMessage
            }

            else -> {}
        }
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

    @And("表示名 {string} が返される")
    fun displayNameIsReturned(expectedDisplayName: String) {
        val success = loginResult as LoginResult.Success
        assertEquals(expectedDisplayName, success.displayName)
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
