
package com.example.tradeflow.model

data class UserModel(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var isBlocked: Boolean = false,
    var isRestricted: Boolean = false,
    var points: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "isBlocked" to isBlocked,
            "isRestricted" to isRestricted,
            "points" to points
        )
    }

    // for chat screen

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
}
