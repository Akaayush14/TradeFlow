package com.example.tradeflow.repository

import com.example.tradeflow.model.UserPointRedemModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RedemptionRepoImpl : RedemptionRepo {
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("PointRedemptions")

    override fun saveRedemption(
        redemption: UserPointRedemModel,
        callback: (Boolean, String) -> Unit
    ) {
        val redemptionId = ref.push().key ?: ""
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
