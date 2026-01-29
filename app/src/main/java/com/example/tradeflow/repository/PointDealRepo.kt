package com.example.tradeflow.repository

import com.example.tradeflow.model.PointDealModel

interface PointDealRepo {
    fun addPointDeal(
        model: PointDealModel,
        callback: (Boolean, String) -> Unit
    )

    fun updatePointDeal(
        model: PointDealModel,
        callback: (Boolean, String) -> Unit
    )

    fun deletePointDeal(
        dealId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getAllPointDeals(
        callback: (Boolean, String, List<PointDealModel>?) -> Unit
    )

    fun getPointDealById(
        dealId: String,
        callback: (Boolean, String, PointDealModel?) -> Unit
    )

    fun getActivePointDeals(
        callback: (Boolean, String, List<PointDealModel>?) -> Unit
    )

    fun getPointDealsByTier(
        tier: String,
        callback: (Boolean, String, List<PointDealModel>?) -> Unit
    )
}