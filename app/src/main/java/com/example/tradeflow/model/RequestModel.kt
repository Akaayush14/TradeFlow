package com.example.tradeflow.model

data class RequestModel(
    val requestId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val productPrice: Double = 0.0,
    val productType: String = "", // "Barter", "Rent", "Both"
    val ownerId: String = "", // Person who owns the item
    val ownerName: String = "",
    val requesterId: String = "", // Person requesting the item
    val requesterName: String = "",
    val requesterImage: String = "",
    val requesterMessage: String = "", // Optional message from requester
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED", "COMPLETED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "requestId" to requestId,
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
            "productPrice" to productPrice,
            "productType" to productType,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "requesterId" to requesterId,
            "requesterName" to requesterName,
            "requesterImage" to requesterImage,
            "requesterMessage" to requesterMessage,
            "status" to status,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}