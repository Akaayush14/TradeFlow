package com.example.tradeflow.model

data class ChatModel(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val participantsData: Map<String, UserModel> = emptyMap()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "chatId" to chatId,
            "participants" to participants,
            "lastMessage" to lastMessage,
            "lastMessageTime" to lastMessageTime,
            "unreadCount" to unreadCount
        )
    }
}
