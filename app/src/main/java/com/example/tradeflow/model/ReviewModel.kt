package com.example.tradeflow.model

data class ReviewModel(
    var reviewId: String = "",
    var productId: String = "",
    var userId: String = "",
    var userName: String = "",
    var rating: Float = 0f,
    var comment: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "reviewId" to reviewId,
            "productId" to productId,
            "userId" to userId,
            "userName" to userName,
            "rating" to rating,
            "comment" to comment,
            "timestamp" to timestamp
        )
    }
}
