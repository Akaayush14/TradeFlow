package com.example.tradeflow.model

data class OfferedItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val productPrice: Double = 0.0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
            "productPrice" to productPrice
        )
    }
}
