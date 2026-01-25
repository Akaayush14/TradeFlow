package com.example.tradeflow.repository

import com.example.tradeflow.model.SearchHistoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchHistoryRepoImpl : SearchHistoryRepo {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("search_history")

    override fun saveSearch(userId: String, query: String, callback: (Boolean, String) -> Unit) {
        val id = ref.push().key ?: return
        val searchHistory = SearchHistoryModel(
            id = id,
            userId = userId,
            query = query,
            timestamp = System.currentTimeMillis()
        )
        ref.child(userId).child(id).setValue(searchHistory)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Search saved")
                } else {
                    callback(false, task.exception?.message ?: "Error saving search")
                }
            }
    }

    override fun getSearchHistory(userId: String, callback: (Boolean, String, List<SearchHistoryModel>?) -> Unit) {
        ref.child(userId).orderByChild("timestamp").limitToLast(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val historyList = mutableListOf<SearchHistoryModel>()
                    for (child in snapshot.children) {
                        val history = child.getValue(SearchHistoryModel::class.java)
                        if (history != null) {
                            historyList.add(history)
                        }
                    }
                    // Reverse to show newest first
                    callback(true, "Success", historyList.reversed())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}
