package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.SearchHistoryModel
import com.example.tradeflow.repository.SearchHistoryRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchHistoryViewModel(private val repo: SearchHistoryRepo) : ViewModel() {

    private val _searchHistory = MutableStateFlow<List<SearchHistoryModel>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistoryModel>> = _searchHistory.asStateFlow()

    fun saveSearch(userId: String, query: String) {
        if (query.isBlank()) return
        repo.saveSearch(userId, query) { success, _ ->
            if (success) {
                // Optionally refresh history immediately
                getSearchHistory(userId)
            }
        }
    }

    fun getSearchHistory(userId: String) {
        repo.getSearchHistory(userId) { success, _, data ->
            if (success) {
                _searchHistory.value = data ?: emptyList()
            }
        }
    }
}
