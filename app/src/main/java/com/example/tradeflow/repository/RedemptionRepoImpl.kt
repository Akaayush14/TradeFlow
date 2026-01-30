package com.example.tradeflow.repository

import com.example.tradeflow.model.UserPointRedemModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class RedemptionRepoImpl : RedemptionRepo {
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("PointRedemptions")

    override fun saveRedemption(
        redemption: UserPointRedemModel,
        callback: (Boolean, String) -> Unit
    ) {
        val redemptionId = "${redemption.userId}_${redemption.dealId}"
        redemption.redemptionId = redemptionId
        ref.child(redemptionId).setValue(redemption)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Redemption saved successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to save redemption")
                }
            }
    }

    override fun getRedemptionsByUserId(
        userId: String,
        callback: (Boolean, String, List<UserPointRedemModel>?) -> Unit
    ) {
        ref.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val redemptions = mutableListOf<UserPointRedemModel>()
                        for (data in snapshot.children) {
                            val redemption = data.getValue(UserPointRedemModel::class.java)
                            redemption?.let { redemptions.add(it) }
                        }
                        callback(true, "Redemptions fetched", redemptions)
                    } else {
                        callback(true, "No redemptions found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun hasUserClaimedDeal(
        userId: String,
        dealId: String,
        callback: (Boolean, String, Boolean) -> Unit
    ) {
        // Check new deterministic ID first
        val redemptionId = "${userId}_${dealId}"
        ref.child(redemptionId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    callback(true, "Already claimed", true)
                } else {
                    // Fallback to query check for legacy records
                    ref.orderByChild("userId").equalTo(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    callback(true, "No claims for user", false)
                                    return
                                }
                                for (data in snapshot.children) {
                                    val redemption = data.getValue(UserPointRedemModel::class.java)
                                    if (redemption?.dealId == dealId) {
                                        callback(true, "Already claimed", true)
                                        return
                                    }
                                }
                                callback(true, "Not claimed", false)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, error.message, false)
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, false)
            }
        })
    }

    override fun createRedemptionPlaceholder(
        userId: String,
        dealId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val redemptionId = "${userId}_${dealId}"
        ref.child(redemptionId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                if (mutableData.value != null) {
                    // Record already exists
                    return Transaction.abort()
                }
                // Create placeholder
                val placeholder = HashMap<String, Any>()
                placeholder["userId"] = userId
                placeholder["dealId"] = dealId
                placeholder["status"] = "PENDING"
                placeholder["createdAt"] = System.currentTimeMillis()

                mutableData.value = placeholder
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                dataSnapshot: DataSnapshot?
            ) {
                if (committed) {
                    callback(true, "Placeholder created")
                } else {
                    callback(false, "Deal already claimed or processing")
                }
            }
        })
    }

    override fun removeRedemption(
        userId: String,
        dealId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val redemptionId = "${userId}_${dealId}"
        ref.child(redemptionId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Redemption removed")
                } else {
                    callback(false, task.exception?.message ?: "Failed to remove redemption")
                }
            }
    }

    override fun deleteAllRedemptionsForUser(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(true, "No redemptions to delete")
                    return
                }

                val updates = hashMapOf<String, Any?>()
                for (child in snapshot.children) {
                    updates[child.key!!] = null
                }

                ref.updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) callback(true, "All redemptions deleted")
                        else callback(false, task.exception?.message ?: "Error deleting redemptions")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }
}