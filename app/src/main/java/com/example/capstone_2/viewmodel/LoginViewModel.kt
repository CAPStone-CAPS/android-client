package com.example.capstone_2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone_2.data.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle: LoginUiState
    object Loading: LoginUiState
    object Success: LoginUiState
    data class Error(val reason: String): LoginUiState
}

class LoginViewModel(app: Application): AndroidViewModel(app) {
    private val repo = LoginRepository(app.applicationContext)

    private val _ui = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val ui: StateFlow<LoginUiState> = _ui

    fun login(email: String, password: String) {
        _ui.value = LoginUiState.Loading
        viewModelScope.launch {
            val r = repo.login(email, password)
            _ui.value = r.fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { LoginUiState.Error(it.message ?: "로그인 실패") }
            )
        }
    }
}
