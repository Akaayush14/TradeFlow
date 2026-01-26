package com.example.tradeflow.model

data class ThemeModel(
    val userId: String = "",
    val themeMode: String = "system",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "themeMode" to themeMode,
            "lastUpdated" to lastUpdated
        )
    }
}