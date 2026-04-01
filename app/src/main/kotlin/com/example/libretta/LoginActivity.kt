package com.example.libretta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.libretta.auth.AuthApiClient
import com.example.libretta.auth.AuthRepository
import com.example.libretta.auth.AuthSkipper
import com.example.libretta.auth.LoginUiState
import com.example.libretta.auth.LoginUseCase
import com.example.libretta.auth.LoginViewModel
import com.example.libretta.auth.LoginViewModelFactory
import com.example.libretta.auth.StubAuthRepository
import com.example.libretta.session.SessionManager
import com.example.libretta.session.SharedPreferencesSessionRepository

class LoginActivity : AppCompatActivity() {

    private val sessionManager by lazy {
        SessionManager(SharedPreferencesSessionRepository(this))
    }

    private val viewModel: LoginViewModel by viewModels {
        val app = application as LibrettaApplication
        val repository: AuthRepository = if (BuildConfig.DEBUG) {
            StubAuthRepository()
        } else {
            AuthApiClient(app.okHttpClient, app.baseUrl)
        }
        LoginViewModelFactory(LoginUseCase(repository), sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LibrettaApplication
        if (AuthSkipper.applyIfNeeded(app.skipAuthApi, sessionManager) ||
            sessionManager.isLoggedIn()
        ) {
            startActivity(Intent(this, TopActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val textError = findViewById<TextView>(R.id.textError)

        if (BuildConfig.DEBUG) {
            editEmail.setText(StubAuthRepository.EMAIL)
            editPassword.setText(StubAuthRepository.PASSWORD)
            findViewById<TextView>(R.id.textDebugMode).visibility = View.VISIBLE
        }

        buttonLogin.setOnClickListener {
            viewModel.login(editEmail.text.toString(), editPassword.text.toString())
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is LoginUiState.Loading -> {
                    buttonLogin.isEnabled = false
                    textError.visibility = View.GONE
                }

                is LoginUiState.Success -> {
                    startActivity(Intent(this, TopActivity::class.java))
                    finish()
                }

                is LoginUiState.Error -> {
                    buttonLogin.isEnabled = true
                    textError.text = state.message
                    textError.visibility = View.VISIBLE
                }

                is LoginUiState.Idle -> {
                    buttonLogin.isEnabled = true
                    textError.visibility = View.GONE
                }
            }
        }
    }
}
