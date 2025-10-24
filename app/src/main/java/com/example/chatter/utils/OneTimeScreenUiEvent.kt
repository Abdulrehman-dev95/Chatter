package com.example.chatter.utils

sealed class OneTimeScreenUiEvent {
    object NavigateToOtherScreen : OneTimeScreenUiEvent()
    data class ShowToast(val message: String) : OneTimeScreenUiEvent()
}