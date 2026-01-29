package com.example.tradeflow.repository

import com.example.tradeflow.model.ReviewModel

interface ReviewRepo {
    fun addReview(review: ReviewModel, callback: (Boolean, String) -> Unit)
    fun getReviewsByProductId(productId: String, callback: (Boolean, String, List<ReviewModel>?) -> Unit)
    fun getReviewsByUserId(userId: String, callback: (Boolean, String, List<ReviewModel>?) -> Unit)
}
