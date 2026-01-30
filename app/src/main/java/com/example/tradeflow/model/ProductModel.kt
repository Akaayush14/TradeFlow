package com.example.tradeflow.model

import java.io.Serializable

data class ProductModel(
    var productId: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var category: String = "",
    var location: String = "",
    var description: String = "",
    var type: String = "", // "Barter", "Rent", or "Both"
    var status: String = "Available", // "Available", "Pending", "Completed"
    var ownerId: String = "", // User ID who created this product
    var createdAt: Long = System.currentTimeMillis(),
    var isDeleted: Boolean = false,
    var completedAt: Long? = null,
    var imageUrl2: String = "",
    var imageUrl3: String = "",
    var imageUrl4: String = "",
    var imageUrls: List<String> = emptyList(),
    var isListed: Boolean = false,
    var securityDeposit: Double = 0.0, // Added field
    var rentalEndDate: Long = 0L, // Timestamp when rental ends
    var activeRequestId: String = "" // Links to the current active rental request
) : Serializable {
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "productId" to productId,
            "name" to name,
            "price" to price,
            "imageUrl" to imageUrl,
            "imageUrl2" to imageUrl2,
            "imageUrl3" to imageUrl3,
            "imageUrl4" to imageUrl4,
            "imageUrls" to imageUrls,
            "category" to category,
            "location" to location,
            "description" to description,
            "type" to type,
            "status" to status,
            "ownerId" to ownerId,
            "createdAt" to createdAt,
            "isDeleted" to isDeleted,
            "completedAt" to completedAt,
            "isListed" to isListed,
            "securityDeposit" to securityDeposit,
            "rentalEndDate" to rentalEndDate,
            "activeRequestId" to activeRequestId
        )
    }
}