package com.example.tradeflow.repository

import com.example.tradeflow.model.PointTransaction
import com.google.firebase.database.ValueEventListener

interface PointTransactionRepo {
    fun saveTransaction(
        tx: PointTransaction,
        callback: (Boolean, String) -> Unit
    )

    fun observeRecentTransactions(
        userId: String,
        onChange: (List<PointTransaction>) -> Unit
    ): ValueEventListener

    fun deleteTransaction(transactionId: String, callback: (Boolean, String) -> Unit)
    fun deleteAllTransactions(userId: String, callback: (Boolean, String) -> Unit)

    fun removeListener(listener: ValueEventListener)
}
