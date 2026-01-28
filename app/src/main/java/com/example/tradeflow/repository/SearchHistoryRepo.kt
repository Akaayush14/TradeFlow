package com.example.tradeflow.repository

import com.example.tradeflow.model.SearchHistoryModel

interface SearchHistoryRepo {
    fun saveSearch(userId: String, query: String, callback: (Boolean, String) -> Unit)
    fun getSearchHistory(userId: String, callback: (Boolean, String, List<SearchHistoryModel>?) -> Unit)
}
