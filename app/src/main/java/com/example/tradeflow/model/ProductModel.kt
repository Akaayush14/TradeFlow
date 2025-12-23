package com.example.tradeflow.model

data class ProductModel(
    var productId: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var categoryId: String = "",
){
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "name" to name,
            "price" to price,
            "imageUrl" to imageUrl,
            "categoryId" to categoryId,
        )
    }
}
