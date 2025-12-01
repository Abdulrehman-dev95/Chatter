package com.example.chatter.ui.screens

import android.util.Patterns
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
class SignupViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) :
    ViewModel() {
    private val _signupUiState = MutableStateFlow(SignupUiState())
    val signupState = _signupUiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<OneTimeScreenUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onNameChange(name: String) {
        _signupUiState.value = _signupUiState.value.copy(name = name)
    }

    fun onEmailChange(email: String) {
        _signupUiState.value = _signupUiState.value.copy(email = email.trim(), emailError = null)
    }

    fun onPasswordChange(password: String) {
        _signupUiState.value = _signupUiState.value.copy(password = password, passwordError = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _signupUiState.value = _signupUiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    fun validateEmail(): Boolean {
        val email = _signupUiState.value.email
        return when {
            email.isEmpty() -> {
                _signupUiState.value =
                    _signupUiState.value.copy(emailError = "Email cannot be empty")
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _signupUiState.value =
                    _signupUiState.value.copy(emailError = "Invalid email format")
                false
            }

            else -> true
        }
    }

    fun validatePassword(): Boolean {
        val password = _signupUiState.value.password
        return when {
            password.isEmpty() -> {
                _signupUiState.value =
                    _signupUiState.value.copy(passwordError = "Password cannot be empty")
                false
            }

            password.length < 6 -> {
                _signupUiState.value =
                    _signupUiState.value.copy(passwordError = "Password must be at least 6 characters")
                false
            }

            else -> true
        }
    }

    fun validateConfirmPassword(): Boolean {
        val password = _signupUiState.value.password
        val confirmPassword = _signupUiState.value.confirmPassword
        return when {
            confirmPassword.isEmpty() -> {
                _signupUiState.value =
                    _signupUiState.value.copy(confirmPasswordError = "Please confirm password")
                false
            }

            confirmPassword != password -> {
                _signupUiState.value =
                    _signupUiState.value.copy(confirmPasswordError = "Password do not match")
                false
            }

            else -> true
        }
    }

    fun onSignupClick() {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()

        if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            _signupUiState.value = _signupUiState.value.copy(isLoading = true)
            val name = _signupUiState.value.name
            val email = _signupUiState.value.email
            val password = _signupUiState.value.password

            viewModelScope.launch {
                val result = authenticationRepository.signup(name, email, password)
                if (result.isSuccess) {
                    _signupUiState.value =
                        _signupUiState.value.copy(isLoading = false)
//                    initializeZegoCloud(
//                        applicationContext = applicationContext
//                    )
                    _eventFlow.emit(OneTimeScreenUiEvent.ShowToast("Signup successful"))
                    _eventFlow.emit(OneTimeScreenUiEvent.NavigateToOtherScreen)
                } else {
                    _signupUiState.value = _signupUiState.value.copy(
                        isLoading = false,
                    )
                    _eventFlow.emit(OneTimeScreenUiEvent.ShowToast("Signup failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"))
                }
            }

        }
    }

//    private fun initializeZegoCloud(applicationContext: Application) {
//        val appId: Long = 566529291
//        val appSign = "13f48aa1d21d45f2969152627179851a18400ffa78209d666deb12ce247fc7b2"
//        val userId = Firebase.auth.currentUser!!.uid
//        val userName = Firebase.auth.currentUser!!.displayName!!
//
//        val invitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
//        val notificationConfig = ZegoNotificationConfig()
//        notificationConfig.channelID = "Chatter-9515"
//        notificationConfig.channelName = "Chatter Messages"
//        invitationConfig.notificationConfig = notificationConfig
//
//        ZegoUIKitPrebuiltCallService.init(
//            applicationContext,
//            appId,
//            appSign,
//            userId,
//            userName,
//            invitationConfig
//        )
//    }


}

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
)