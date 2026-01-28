package com.example.tradeflow.model

data class SearchHistoryModel(
    var id: String = "",
    var userId: String = "",
    var query: String = "",
    var timestamp: Long = System.currentTimeMillis()
)
