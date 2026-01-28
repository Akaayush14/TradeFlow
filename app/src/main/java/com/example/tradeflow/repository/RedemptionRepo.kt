package com.example.tradeflow.repository

import com.example.tradeflow.model.UserPointRedemModel

interface RedemptionRepo {
    fun saveRedemption(
        redemption: UserPointRedemModel,
        callback: (Boolean, String) -> Unit
    )

    fun getRedemptionsByUserId(
        userId: String,
        callback: (Boolean, String, List<UserPointRedemModel>?) -> Unit
    )

    fun hasUserClaimedDeal(
        userId: String,
        dealId: String,
        callback: (Boolean, String, Boolean) -> Unit
    )

    fun createRedemptionPlaceholder(
        userId: String,
        dealId: String,
        callback: (Boolean, String) -> Unit
    )

    fun removeRedemption(
        userId: String,
        dealId: String,
        callback: (Boolean, String) -> Unit
    )

    fun deleteAllRedemptionsForUser(
        userId: String,
        callback: (Boolean, String) -> Unit
    )
}
