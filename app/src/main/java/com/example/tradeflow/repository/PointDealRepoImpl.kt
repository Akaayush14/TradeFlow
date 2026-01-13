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
        println("DEBUG [REPO]: Fetching active deals from Firebase...")

        ref.orderByChild("isActive").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    println("DEBUG [REPO]: Firebase returned ${snapshot.childrenCount} items")

                    if (snapshot.exists()) {
                        val activeDeals = mutableListOf<PointDealModel>()
                        val currentTime = System.currentTimeMillis()

                        for (data in snapshot.children) {
                            val deal = data.getValue(PointDealModel::class.java)
                            println("DEBUG [REPO]: Checking deal: ${deal?.offer}")
                            println("DEBUG [REPO]: Deal validTill: ${deal?.validTill}, Current: $currentTime")
                            println("DEBUG [REPO]: Is active and valid: ${deal?.isActive} && ${deal?.validTill ?: 0 > currentTime}")

                            if (deal != null && deal.validTill > currentTime) {
                                println("DEBUG [REPO]: ✓ Adding deal: ${deal.offer}")
                                activeDeals.add(deal)
                            }
                        }

                        println("DEBUG [REPO]: Total active deals found: ${activeDeals.size}")
                        callback(true, "Active deals fetched", activeDeals)
                    } else {
                        println("DEBUG [REPO]: No deals found in Firebase")
                        callback(true, "No active deals found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("DEBUG [REPO]: Firebase error: ${error.message}")
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


