package com.example.tradeflow.repository

import com.example.tradeflow.model.ReviewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReviewRepoImpl : ReviewRepo {
    private val database = FirebaseDatabase.getInstance().getReference("Reviews")

    override fun addReview(review: ReviewModel, callback: (Boolean, String) -> Unit) {
        val id = database.push().key ?: return callback(false, "Failed to generate ID")
        review.reviewId = id
        database.child(id).setValue(review.toMap())
            .addOnSuccessListener {
                callback(true, "Review added successfully")
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Failed to add review")
            }
    }

    override fun getReviewsByProductId(productId: String, callback: (Boolean, String, List<ReviewModel>?) -> Unit) {
        database.orderByChild("productId").equalTo(productId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reviews = mutableListOf<ReviewModel>()
                    if (snapshot.exists()) {
                        for (doc in snapshot.children) {
                            val review = doc.getValue(ReviewModel::class.java)
                            if (review != null) {
                                reviews.add(review)
                            }
                        }
                    }
                    callback(true, "Reviews fetched", reviews)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getReviewsByUserId(userId: String, callback: (Boolean, String, List<ReviewModel>?) -> Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reviews = mutableListOf<ReviewModel>()
                    if (snapshot.exists()) {
                        for (doc in snapshot.children) {
                            val review = doc.getValue(ReviewModel::class.java)
                            if (review != null) {
                                reviews.add(review)
                            }
                        }
                    }
                    callback(true, "Reviews fetched", reviews)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}
