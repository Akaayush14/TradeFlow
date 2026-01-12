package com.example.tradeflow.model

data class RequestModel(
    // Existing fields (unchanged)
    val requestId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val productPrice: Double = 0.0,
    val productType: String = "", // NOW: "BARTER", "RENT", "BOTH"
    val ownerId: String = "",
    val ownerName: String = "",

    // ✅ NEW: Owner details
    val ownerImage: String = "",

    val requesterId: String = "",
    val requesterName: String = "",
    val requesterImage: String = "",

    // ✅ NEW: Requester reputation
    val requesterRating: Double = 0.0,
    val requesterReviewCount: Int = 0,

    val requesterMessage: String = "",

    // ✅ NEW: Barter exchange details
    val offerProductId: String = "",
    val offerProductName: String = "",
    val offerProductImage: String = "",
    val offerProductPrice: Double = 0.0,

    // ✅ NEW: Rental details
    val rentalStartDate: Long = 0L,
    val rentalEndDate: Long = 0L,
    val rentalPeriod: String = "",
    val rentalPricePerDay: Double = 0.0,
    val rentalTotalPrice: Double = 0.0,
    val rentalPriceFormatted: String = "",

    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // NEW: Additional metadata
    val responseMessage: String = "",
    val completedAt: Long = 0L
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
            "ownerImage" to ownerImage,
            "requesterId" to requesterId,
            "requesterName" to requesterName,
            "requesterImage" to requesterImage,
            "requesterRating" to requesterRating,
            "requesterReviewCount" to requesterReviewCount,
            "requesterMessage" to requesterMessage,
            "offerProductId" to offerProductId,
            "offerProductName" to offerProductName,
            "offerProductImage" to offerProductImage,
            "offerProductPrice" to offerProductPrice,
            "rentalStartDate" to rentalStartDate,
            "rentalEndDate" to rentalEndDate,
            "rentalPeriod" to rentalPeriod,
            "rentalPricePerDay" to rentalPricePerDay,
            "rentalTotalPrice" to rentalTotalPrice,
            "rentalPriceFormatted" to rentalPriceFormatted,
            "status" to status,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "responseMessage" to responseMessage,
            "completedAt" to completedAt
        )
    }
}