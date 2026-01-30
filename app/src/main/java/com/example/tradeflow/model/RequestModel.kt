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

    val ownerImage: String = "",

    val requesterId: String = "",
    val requesterName: String = "",
    val requesterImage: String = "",

    val requesterRating: Double = 0.0,
    val requesterReviewCount: Int = 0,

    val requesterMessage: String = "",

    val offerProductId: String = "",
    val offerProductName: String = "",
    val offerProductImage: String = "",
    val offerProductPrice: Double = 0.0,
    val offeredItems: List<OfferedItem> = emptyList(),

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
    val completedAt: Long = 0L,
    val creditPoints: Double = 0.0, // Credit points offered/requested in trade
    val creditPointAction: String = "OFFER", // "OFFER" (Requester pays) or "REQUEST" (Requester asks)
    val securityDeposit: Double = 0.0 // Added field
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
            "offeredItems" to offeredItems.map { it.toMap() },
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
            "completedAt" to completedAt,
            "creditPoints" to creditPoints,
            "creditPointAction" to creditPointAction,
            "securityDeposit" to securityDeposit
        )
    }
}