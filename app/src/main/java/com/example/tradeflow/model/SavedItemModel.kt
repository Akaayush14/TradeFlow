package com.example.tradeflow.model

data class SavedItemModel(
    var savedId: String = "",
    var userId: String = "",
    var productId: String = "",
    var savedAt: Long = System.currentTimeMillis()
)
