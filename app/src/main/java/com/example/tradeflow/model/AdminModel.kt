// AdminModel.kt
package com.example.tradeflow.model

data class AdminModel(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var dateOfBirth: String = "",
    var gender: String = "",
    var imageUrl: String = "",
    var location: String = "",
    var isBlocked: Boolean = false,
    var isRestricted: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "dateOfBirth" to dateOfBirth,
            "gender" to gender,
            "imageUrl" to imageUrl,
            "location" to location,
            "isBlocked" to isBlocked,
            "isRestricted" to isRestricted
        )
    }
}