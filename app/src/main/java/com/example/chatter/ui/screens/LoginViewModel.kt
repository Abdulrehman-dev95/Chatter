package com.example.chatter.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatter.data.repositories.AuthenticationRepository
import com.example.chatter.utils.OneTimeScreenUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState = _loginState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<OneTimeScreenUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChange(email: String) {
        _loginState.value = _loginState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _loginState.value = _loginState.value.copy(password = password)
    }


    fun onLoginClick() {
        _loginState.value = _loginState.value.copy(isLoading = true)
        val email = _loginState.value.email
        val password = _loginState.value.password

        viewModelScope.launch {
            val result = authenticationRepository.login(email, password)
            if (result.isSuccess) {
                _loginState.value = _loginState.value.copy(isLoading = false)
                _eventFlow.emit(OneTimeScreenUiEvent.ShowToast("Login successful"))
                _eventFlow.emit(OneTimeScreenUiEvent.NavigateToOtherScreen)
            } else {
                _loginState.value = _loginState.value.copy(isLoading = false)
                _eventFlow.emit(OneTimeScreenUiEvent.ShowToast("Login failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"))
            }
        }
    }


}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)