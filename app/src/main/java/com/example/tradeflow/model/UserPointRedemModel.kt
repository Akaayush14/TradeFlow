package com.example.tradeflow.model

data class UserPointRedemModel(

    var redemptionId: String = "",
    var userId: String = "",
    var dealId: String = "",
    var pointsSpent: Long = 0L,
    var redeemedAt: Long = System.currentTimeMillis(),
    var dealTitle: String = "",
    var dealOffer: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "redemptionId" to redemptionId,
            "userId" to userId,
            "dealId" to dealId,
            "pointsSpent" to pointsSpent,
            "redeemedAt" to redeemedAt,
            "dealTitle" to dealTitle,
            "dealOffer" to dealOffer
        )
    }
}