package com.example.tradeflow.model

data class NotificationModel(
    var notificationId: String = "",
    var message: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var type: String = "", // "item_listed", "item_unlisted", "item_deleted", "user_blocked", "user_unblocked", "user_restricted", "user_unrestricted", "user_deleted"
    var isRead: Boolean = false,
    var itemId: String = "", // For item-related notifications
    var userId: String = "" // For user-related notifications
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "notificationId" to notificationId,
            "message" to message,
            "timestamp" to timestamp,
            "type" to type,
            "isRead" to isRead,
            "itemId" to itemId,
            "userId" to userId
        )
    }
}

