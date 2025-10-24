package com.example.chatter.ui.screens

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatter.data.model.Message
import com.example.chatter.data.repositories.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _chatUiState = MutableStateFlow(ChatUIState())
    val chatUiState = _chatUiState.asStateFlow()

    val channelName = savedStateHandle.get<String>("channelName") ?: "Channel"

    fun onMessageChange(message: String) {
        _chatUiState.value = _chatUiState.value.copy(
            message = message
        )
    }


    suspend fun getMessages(channelId: String) {
        chatRepository.getMessages(channelId).collect {
            _chatUiState.value = _chatUiState.value.copy(
                messages = it
            )
        }
    }

    fun sendMessage(channelId: String, messageText: String) {
        chatRepository.sendMessage(
            messageText,
            channelId,
            imageUrl = null,
            channelName = channelName
        )
        onMessageChange("")
    }

    fun sendImage(imageUri: Uri, channelId: String) {
        viewModelScope.launch {
            chatRepository.sendImageMessage(imageUri, channelId, channelName = channelName)
        }
    }

}

data class ChatUIState(
    val messages: List<Message> = emptyList(),
    val message: String = "",
)