package com.example.tradeflow.model

data class UserModel(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email
        )
    }
}