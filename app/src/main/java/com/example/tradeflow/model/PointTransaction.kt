package com.example.tradeflow.model

data class PointTransaction(
    var id: String = "",
    var userId: String = "",
    var type: String = "", // "CREDIT" or "DEBIT"
    var source: String = "", // "Khalti Purchase", "Admin Gift", "Deal Claim", "Bonus"
    var points: Long = 0L, // positive for credit, negative for debit
    var amount: Double = 0.0, // optional Rs amount
    var timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "type" to type,
            "source" to source,
            "points" to points,
            "amount" to amount,
            "timestamp" to timestamp
        )
    }
}