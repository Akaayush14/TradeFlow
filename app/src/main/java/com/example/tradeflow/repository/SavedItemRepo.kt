package com.example.tradeflow.repository

import com.example.tradeflow.model.SavedItemModel

interface SavedItemRepo {
    fun saveItem(userId: String, productId: String, callback: (Boolean, String) -> Unit)
    fun unsaveItem(userId: String, productId: String, callback: (Boolean, String) -> Unit)
    fun getSavedItems(userId: String, callback: (Boolean, String, List<SavedItemModel>?) -> Unit)
    fun checkIsSaved(userId: String, productId: String, callback: (Boolean) -> Unit)
}
