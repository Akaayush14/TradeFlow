package com.example.tradeflow.repository

import com.example.tradeflow.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(senderId: String, receiverId: String, message: String, onSuccess: () -> Unit, onError: (Exception) -> Unit)
    suspend fun generateBotResponse(senderId: String, prompt: String)
    fun getMessages(senderId: String, receiverId: String): Flow<List<ChatMessage>>
    fun deleteMessage(roomId: String, messageId: String)
}