package com.example.tradeflow.model

data class UserNotificationModel(
    val notificationId: String = "",
    val type: String = "", // "REQUEST", "ACCEPTED", "REJECTED", "MESSAGE", "COMPLETED"
    val requestType: String = "", // "BARTER", "RENT" - NEW FIELD
    val title: String = "",
    val message: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImage: String = "",
    val senderRating: Double = 0.0, // NEW FIELD
    val senderReviewCount: Int = 0, // NEW FIELD
    val receiverId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",

    // NEW FIELDS for exchange/rental details
    val offerProductId: String = "", // Product being offered in exchange
    val offerProductName: String = "",
    val offerProductImage: String = "",
    val rentalPeriod: String = "", // e.g., "Jan 15-20, 2026"
    val rentalPrice: String = "", // e.g., "$45/day"

    val requestId: String = "",
    val isRead: Boolean = false,
    val status: String = "", // "PENDING", "ACCEPTED", "REJECTED" - NEW FIELD
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "notificationId" to notificationId,
            "type" to type,
            "requestType" to requestType,
            "title" to title,
            "message" to message,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderImage" to senderImage,
            "senderRating" to senderRating,
            "senderReviewCount" to senderReviewCount,
            "receiverId" to receiverId,
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
            "offerProductId" to offerProductId,
            "offerProductName" to offerProductName,
            "offerProductImage" to offerProductImage,
            "rentalPeriod" to rentalPeriod,
            "rentalPrice" to rentalPrice,
            "requestId" to requestId,
            "isRead" to isRead,
            "status" to status,
            "createdAt" to createdAt
        )
    }
}