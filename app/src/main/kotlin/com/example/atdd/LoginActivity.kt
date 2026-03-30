package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.atdd.auth.AuthApiClient
import com.example.atdd.auth.AuthSkipper
import com.example.atdd.auth.LoginUiState
import com.example.atdd.auth.LoginUseCase
import com.example.atdd.auth.LoginViewModel
import com.example.atdd.auth.LoginViewModelFactory
import com.example.atdd.session.SessionManager
import com.example.atdd.session.SharedPreferencesSessionRepository

class LoginActivity : AppCompatActivity() {

    private val sessionManager by lazy {
        SessionManager(SharedPreferencesSessionRepository(this))
    }

    private val viewModel: LoginViewModel by viewModels {
        val app = application as AtddApplication
        val client = AuthApiClient(app.okHttpClient, app.baseUrl)
        LoginViewModelFactory(LoginUseCase(client), sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AtddApplication
        if (AuthSkipper.applyIfNeeded(app.skipAuth, sessionManager)
            || sessionManager.isLoggedIn()) {
            startActivity(Intent(this, TopActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val textError = findViewById<TextView>(R.id.textError)

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
