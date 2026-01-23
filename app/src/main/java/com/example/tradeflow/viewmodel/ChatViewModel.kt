package com.example.tradeflow.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.ChatMessage
import com.example.tradeflow.repository.ChatRepository
import com.example.tradeflow.repository.ChatRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(private val context: Context) : ViewModel() {

    private val repository: ChatRepository = ChatRepositoryImpl(context)

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun loadMessages(senderId: String, receiverId: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            repository.getMessages(senderId, receiverId)
                .catch { e -> _uiState.value = ChatUiState.Error(e.message ?: "Unknown error") }
                .collect { messages ->
                    _uiState.value = ChatUiState.Success(messages)
                }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, text: String) {
        if (text.isBlank()) return
        repository.sendMessage(senderId, receiverId, text,
            onSuccess = {
                // Message sent successfully
                if (receiverId == "chat_bot") {
                    viewModelScope.launch {
                        _isTyping.value = true
                        try {
                            repository.generateBotResponse(senderId, text)
                        } finally {
                            _isTyping.value = false
                        }
                    }
                }
            },
            onError = { e ->
                // Handle error - could add error state here if needed
            }
        )
    }

    fun deleteMessage(messageId: String, roomId: String) {
        repository.deleteMessage(roomId, messageId)
    }
}