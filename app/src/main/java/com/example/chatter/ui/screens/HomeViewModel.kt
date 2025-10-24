package com.example.chatter.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatter.data.model.Channel
import com.example.chatter.data.repositories.UserRepository
import com.example.chatter.utils.OneTimeScreenUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel(
) {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<OneTimeScreenUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun onNewChannelNameChange(newChannelName: String) {
        _homeUiState.value = _homeUiState.value.copy(
            newChannelName = newChannelName,
            isNewChannelAdding = false
        )
    }

    fun addNewChannel() {
        viewModelScope.launch {
            val result =
                userRepository.addChannel(Channel(name = _homeUiState.value.newChannelName))
            if (result.isSuccess) {
                _homeUiState.value = _homeUiState.value.copy(
                    newChannelName = "",
                    isNewChannelAdding = false
                )
                _eventFlow.emit(OneTimeScreenUiEvent.ShowToast("Channel added successfully"))
            } else {
                _homeUiState.value = _homeUiState.value.copy(
                    newChannelName = "",
                    isNewChannelAdding = false
                )
                _eventFlow.emit(OneTimeScreenUiEvent.ShowToast("Error: ${result.exceptionOrNull()?.message?: "Unknown Error"}"))
            }
        }
    }


    init {
        viewModelScope.launch {
            userRepository.getChannels()
                .onStart {
                    _homeUiState.value = _homeUiState.value.copy(isLoading = true)
                }
                .collect { channels ->
                    _homeUiState.value = _homeUiState.value.copy(
                        channels = channels,
                        isLoading = false
                    )
                }

        }
    }


}

data class HomeUiState(
    val channels: List<Channel> = emptyList(),
    val isLoading: Boolean = false,
    val newChannelName: String = "",
    val isNewChannelAdding: Boolean = false
    )