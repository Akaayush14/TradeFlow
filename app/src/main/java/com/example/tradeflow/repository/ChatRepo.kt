package com.example.tradeflow.repository

import com.example.tradeflow.model.MessageModel
import com.google.firebase.auth.FirebaseUser

interface ChatRepo {
    fun sendMessage(
        receiverId: String,
        message: MessageModel,
        callback: (Boolean, String) -> Unit
    )

    fun getMessages(
        receiverId: String,
        onNewMessage: (MessageModel) -> Unit,
        callback: (Boolean, String) -> Unit
    )

    fun getChatPartners(
        callback: (Boolean, String, List<String>?) -> Unit
    )

    fun getChatSummaries(
        callback: (Boolean, String, List<com.example.tradeflow.model.UserModel.ChatModel>?) -> Unit
    )

    fun getCurrentUser(): FirebaseUser?

    fun removeListeners()
}
