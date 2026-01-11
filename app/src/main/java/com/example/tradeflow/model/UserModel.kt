package com.example.tradeflow.model

data class UserModel(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var gender: String = "",           // NEW
    var dob: String = "",              // NEW (Date of Birth)
    var location: String = "",         // NEW (Country)
    var profilePhotoUrl: String = "",  // NEW (Optional - for future use)
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
            "gender" to gender,           // NEW
            "dob" to dob,                 // NEW
            "location" to location,       // NEW
            "profilePhotoUrl" to profilePhotoUrl, // NEW
            "isBlocked" to isBlocked,
            "isRestricted" to isRestricted,
            "points" to points
        )
    }
}