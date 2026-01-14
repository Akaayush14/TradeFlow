package com.example.tradeflow.repository

import com.example.tradeflow.model.PointDealModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PointDealRepoImpl : PointDealRepo {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("PointDeals")




    override fun addPointDeal(
        model: PointDealModel,
        callback: (Boolean, String) -> Unit
    ) {
        val dealId = ref.push().key.toString()
        model.dealId = dealId
        model.createdAt = System.currentTimeMillis()
        model.updatedAt = System.currentTimeMillis()

        ref.child(dealId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Point deal added successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }


    override fun updatePointDeal(
        model: PointDealModel,
        callback: (Boolean, String) -> Unit
    ) {
        model.updatedAt = System.currentTimeMillis()
        ref.child(model.dealId).updateChildren(model.toUpdateMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Point deal updated successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deletePointDeal(
        dealId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(dealId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Point deal deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getAllPointDeals(
        callback: (Boolean, String, List<PointDealModel>?) -> Unit
    ) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allDeals = mutableListOf<PointDealModel>()
                    for (data in snapshot.children) {
                        val deal = data.getValue(PointDealModel::class.java)
                        if (deal != null) {
                            allDeals.add(deal)
                        }
                    }
                    callback(true, "Deals fetched", allDeals)
                } else {
                    callback(true, "No deals found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getPointDealById(
        dealId: String,
        callback: (Boolean, String, PointDealModel?) -> Unit
    ) {
        ref.child(dealId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val deal = snapshot.getValue(PointDealModel::class.java)
                    if (deal != null) {
                        callback(true, "Deal fetched", deal)
                    } else {
                        callback(false, "Deal data is null", null)
                    }
                } else {
                    callback(false, "Deal not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getActivePointDeals(
        callback: (Boolean, String, List<PointDealModel>?) -> Unit
    ) {
        val currentTime = System.currentTimeMillis()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val activeDeals = mutableListOf<PointDealModel>()
                    for (data in snapshot.children) {
                        val deal = data.getValue(PointDealModel::class.java)
                        if (deal != null) {
                            val isActive = deal.isActive
                            val isValid = deal.validTill > currentTime
                            if (isActive && isValid) {
                                activeDeals.add(deal)
                            }
                        }
                    }
                    callback(true, "Active deals fetched", activeDeals)
                } else {
                    callback(true, "No active deals found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getPointDealsByTier(
        tier: String,
        callback: (Boolean, String, List<PointDealModel>?) -> Unit
    ) {
        ref.orderByChild("tier").equalTo(tier)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val tierDeals = mutableListOf<PointDealModel>()
                        val currentTime = System.currentTimeMillis()
                        for (data in snapshot.children) {
                            val deal = data.getValue(PointDealModel::class.java)
                            if (deal != null && deal.isActive && deal.validTill > currentTime) {
                                tierDeals.add(deal)
                            }
                        }
                        callback(true, "Tier deals fetched", tierDeals)
                    } else {
                        callback(true, "No deals found for tier", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}


