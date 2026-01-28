package com.example.tradeflow.model

data class PointDealModel(
    var dealId: String = "",
    var title: String = "",
    var offer: String = "", // e.g., "FLAT Rs.15 OFF!!"
    var tier: String = "", // "Bronze", "Silver", "Gold"
    var serviceCategory: String = "", // "BIKE", "FOOD", "CAR", etc.
    var pointsRequired: Long = 0L,
    var validTill: Long = System.currentTimeMillis(), // Timestamp
    var isActive: Boolean = true,
    var discountAmount: Double = 0.0,
    var discountType: String = "", // "FLAT" or "UPTO"
    var rewardPoints: Long = 0L,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "dealId" to dealId,
            "title" to title,
            "offer" to offer,
            "tier" to tier,
            "serviceCategory" to serviceCategory,
            "pointsRequired" to pointsRequired,
            "validTill" to validTill,
            "isActive" to isActive,
            "discountAmount" to discountAmount,
            "discountType" to discountType,
            "rewardPoints" to rewardPoints,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }


    fun toUpdateMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "offer" to offer,
            "tier" to tier,
            "serviceCategory" to serviceCategory,
            "pointsRequired" to pointsRequired,
            "validTill" to validTill,
            "isActive" to isActive,
            "discountAmount" to discountAmount,
            "discountType" to discountType,
            "rewardPoints" to rewardPoints,
            "updatedAt" to updatedAt
        )
    }



}

