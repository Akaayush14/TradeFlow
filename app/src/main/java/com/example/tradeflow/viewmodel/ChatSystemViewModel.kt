package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.ChatModel
import com.example.tradeflow.model.MessageModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.ChatRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatSystemViewModel(val repo: ChatRepo) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages.asStateFlow()
    private val _chatPartners = MutableStateFlow<List<String>>(emptyList())
    val chatPartners: StateFlow<List<String>> = _chatPartners.asStateFlow()
    private val _chatSummaries = MutableStateFlow<List<ChatModel>>(emptyList())
    val chatSummaries: StateFlow<List<ChatModel>> = _chatSummaries.asStateFlow()
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

    fun deleteMessage(
        message: MessageModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteMessage(message) { success, msg ->
            callback(success, msg)
        }
    }

    fun getMessages(receiverId: String) {
        viewModelScope.launch {
            repo.getMessages(receiverId,
                onMessagesUpdate = { messages ->
                    _messages.value = messages
                },
                callback = { success, message ->
                    // Handle callback if needed
                }
            )
        }
    }

    fun loadChatPartners() {
        viewModelScope.launch {
            repo.getChatPartners { success, message, data ->
                _chatPartners.value = if (success) (data ?: emptyList()) else emptyList()
            }
        }
    }

    fun loadChatSummaries() {
        viewModelScope.launch {
            repo.getChatSummaries { success, message, data ->
                _chatSummaries.value = if (success) (data ?: emptyList()) else emptyList()
            }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun removeListeners() {
        repo.removeListeners()
    }
}