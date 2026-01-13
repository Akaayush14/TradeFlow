package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.MessageModel
import com.example.tradeflow.repository.ChatRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(val repo: ChatRepo) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()
    fun sendTextMessage(
        receiverId: String,
        text: String,
        callback: (Boolean, String) -> Unit
    ) {
        val message = MessageModel(
            text = text,
            messageType = MessageModel.MessageType.TEXT
        )
        repo.sendMessage(receiverId, message) { success, message ->
            callback(success, message)
        }
    }

    fun getMessages(receiverId: String) {
        viewModelScope.launch {
            repo.getMessages(receiverId,
                onNewMessage = { message ->
                    val currentMessages = _messages.value.toMutableList()
                    // Check if message already exists to avoid duplicates
                    if (!currentMessages.any { it.messageId == message.messageId }) {
                        currentMessages.add(message)
                        _messages.value = currentMessages.sortedBy { it.timestamp }
                    }
                },
                callback = { success, message ->
                    // Handle callback if needed
                }
            )
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun removeListeners() {
        repo.removeListeners()
    }
}
