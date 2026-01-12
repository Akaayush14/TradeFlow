package com.example.tradeflow.model

import java.io.Serializable

data class UserModel(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var profileImageUrl: String = "",
    var isBlocked: Boolean = false,
    var isRestricted: Boolean = false,
    var points: Long = 0L
) : Serializable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "profileImageUrl" to profileImageUrl,
            "isBlocked" to isBlocked,
            "isRestricted" to isRestricted,
            "points" to points
        )
    }
}