package com.example.tradeflow.repository

import com.example.tradeflow.model.PointTransaction
import com.google.firebase.database.*

class PointTransactionRepoImpl : PointTransactionRepo {
    private val db = FirebaseDatabase.getInstance()
    private val ref = db.getReference("PointTransactions")
    private val activeQueries = java.util.HashMap<ValueEventListener, Query>()

    override fun saveTransaction(
        tx: PointTransaction,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key ?: run {
            callback(false, "Failed to create transaction id")
            return
        }
        tx.id = id
        ref.child(id).setValue(tx)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Saved")
                else callback(false, task.exception?.message ?: "Error")
            }
    }

    override fun observeRecentTransactions(
        userId: String,
        onChange: (List<PointTransaction>) -> Unit
    ): ValueEventListener {
        val currentTime = System.currentTimeMillis()
        val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
        val cutoff = currentTime - thirtyDaysMillis

        val query = ref.orderByChild("userId").equalTo(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<PointTransaction>()
                for (child in snapshot.children) {
                    val tx = child.getValue(PointTransaction::class.java)
                    if (tx != null && tx.timestamp >= cutoff) {
                        list.add(tx)
                    }
                }
                list.sortByDescending { it.timestamp }
                onChange(list)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("PointTransactionRepo", "observeRecentTransactions cancelled: ${error.message}")
            }
        }
        query.addValueEventListener(listener)
        activeQueries[listener] = query
        return listener
    }

    override fun deleteTransaction(transactionId: String, callback: (Boolean, String) -> Unit) {
        ref.child(transactionId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Transaction deleted")
                else callback(false, task.exception?.message ?: "Error deleting transaction")
            }
    }

    override fun deleteAllTransactions(userId: String, callback: (Boolean, String) -> Unit) {
        ref.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(true, "No transactions to delete")
                    return
                }

                // We'll delete them one by one or we could use updateChildren with nulls
                // Since we want to delete all transactions for this user
                val updates = hashMapOf<String, Any?>()
                for (child in snapshot.children) {
                    updates[child.key!!] = null
                }

                ref.updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) callback(true, "All transactions deleted")
                        else callback(false, task.exception?.message ?: "Error deleting transactions")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun removeListener(listener: ValueEventListener) {
        val query = activeQueries.remove(listener)
        if (query != null) {
            query.removeEventListener(listener)
        } else {
            ref.removeEventListener(listener)
        }
    }
}