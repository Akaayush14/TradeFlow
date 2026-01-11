package com.example.tradeflow.model

data class UserNotificationModel(
    val notificationId: String = "",
    val type: String = "", // "REQUEST", "ACCEPTED", "REJECTED", "MESSAGE", "COMPLETED"
    val title: String = "",
    val message: String = "",
    val senderId: String = "", // User who triggered the notification
    val senderName: String = "",
    val senderImage: String = "",
    val receiverId: String = "", // User who receives the notification
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val requestId: String = "", // Link to the request if applicable
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "notificationId" to notificationId,
            "type" to type,
            "title" to title,
            "message" to message,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderImage" to senderImage,
            "receiverId" to receiverId,
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
            "requestId" to requestId,
            "isRead" to isRead,
            "createdAt" to createdAt
        )
    }
}
