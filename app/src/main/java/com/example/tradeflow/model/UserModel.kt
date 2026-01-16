package com.example.tradeflow.model

data class UserModel(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var location: String = "",
    var gender: String = "",
    var dob: String = "",
    var isBlocked: Boolean = false,
    var isRestricted: Boolean = false,
    var points: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "location" to location,
            "isBlocked" to isBlocked,
            "isRestricted" to isRestricted,
            "points" to points
        )
    }
}