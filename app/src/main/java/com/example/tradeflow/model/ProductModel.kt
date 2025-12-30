package com.example.tradeflow.model

data class ProductModel(
    var productId: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var category: String = "",
    var location: String = "",
    var description: String = "",
    var type: String = "", // "Barter", "Rent", or "Both"
    var status: String = "Available", // "Available", "Pending", "Completed"
    var ownerId: String = "", // User ID who created this product
    var createdAt: Long = System.currentTimeMillis(),
    var isDeleted: Boolean = false
){
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "productId" to productId,
            "name" to name,
            "price" to price,
            "imageUrl" to imageUrl,
            "category" to category,
            "location" to location,
            "description" to description,
            "type" to type,
            "status" to status,
            "ownerId" to ownerId,
            "createdAt" to createdAt,
            "isDeleted" to isDeleted
        )
    }
}
