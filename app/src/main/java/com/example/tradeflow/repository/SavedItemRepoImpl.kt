package com.example.tradeflow.repository

import com.example.tradeflow.model.SavedItemModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedItemRepoImpl : SavedItemRepo {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("saved_items")

    override fun saveItem(userId: String, productId: String, callback: (Boolean, String) -> Unit) {
        val savedId = ref.push().key ?: return callback(false, "Failed to generate key")
        val model = SavedItemModel(
            savedId = savedId,
            userId = userId,
            productId = productId
        )
        
        // Structure: saved_items/{userId}/{productId} -> SavedItemModel
        ref.child(userId).child(productId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Item saved")
            } else {
                callback(false, it.exception?.message ?: "Error saving item")
            }
        }
    }

    override fun unsaveItem(userId: String, productId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child(productId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Item unsaved")
            } else {
                callback(false, it.exception?.message ?: "Error unsaving item")
            }
        }
    }

    override fun getSavedItems(userId: String, callback: (Boolean, String, List<SavedItemModel>?) -> Unit) {
        ref.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<SavedItemModel>()
                for (child in snapshot.children) {
                    val item = child.getValue(SavedItemModel::class.java)
                    if (item != null) {
                        items.add(item)
                    }
                }
                callback(true, "Success", items)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
    
    override fun checkIsSaved(userId: String, productId: String, callback: (Boolean) -> Unit) {
        ref.child(userId).child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }
}
