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

    fun getCurrentUser(): FirebaseUser?

    fun removeListeners()
}