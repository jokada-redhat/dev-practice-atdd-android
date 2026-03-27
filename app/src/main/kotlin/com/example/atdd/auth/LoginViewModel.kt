package com.example.atdd.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.atdd.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val loginUseCase: LoginUseCase, private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = loginUseCase.execute(LoginRequest(email, password))
            withContext(Dispatchers.Main) {
                _uiState.value = when (result) {
                    is LoginResult.Success -> {
                        sessionManager.saveSession(result.token, result.displayName)
                        LoginUiState.Success(result.displayName)
                    }

                    is LoginResult.Failure -> LoginUiState.Error(result.errorMessage)

                    is LoginResult.ValidationError -> LoginUiState.Error(result.message)
                }
            }
        }
    }
}

class LoginViewModelFactory(private val loginUseCase: LoginUseCase, private val sessionManager: SessionManager) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LoginViewModel(loginUseCase, sessionManager) as T
    }
}
