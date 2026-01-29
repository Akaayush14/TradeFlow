package com.example.tradeflow.model

import java.util.*

data class MessageModel(
    var messageId: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var text: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false,
    var messageType: MessageType = MessageType.TEXT
) {
    enum class MessageType {
        TEXT
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "messageId" to messageId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "text" to text,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "messageType" to messageType.name
        )
    }
}