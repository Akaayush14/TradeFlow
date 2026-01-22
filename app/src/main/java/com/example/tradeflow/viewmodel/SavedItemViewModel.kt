package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.SavedItemModel
import com.example.tradeflow.repository.SavedItemRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SavedItemViewModel(val repo: SavedItemRepo) : ViewModel() {
    
    private val _savedItems = MutableStateFlow<List<SavedItemModel>>(emptyList())
    val savedItems: StateFlow<List<SavedItemModel>> = _savedItems

    private val _savedProductIds = MutableStateFlow<Set<String>>(emptySet())
    val savedProductIds: StateFlow<Set<String>> = _savedProductIds

    fun saveItem(userId: String, productId: String) {
        repo.saveItem(userId, productId) { _, _ -> }
    }

    fun unsaveItem(userId: String, productId: String) {
        repo.unsaveItem(userId, productId) { _, _ -> }
    }

    fun getSavedItems(userId: String) {
        repo.getSavedItems(userId) { success, _, data ->
            if (success && data != null) {
                _savedItems.value = data
                _savedProductIds.value = data.map { it.productId }.toSet()
            }
        }
    }
    
    fun isSaved(productId: String): Boolean {
        return _savedProductIds.value.contains(productId)
    }
}
