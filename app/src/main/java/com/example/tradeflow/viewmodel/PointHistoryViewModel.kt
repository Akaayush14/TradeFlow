package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.PointTransaction
import com.example.tradeflow.repository.PointTransactionRepo
import com.example.tradeflow.repository.PointTransactionRepoImpl
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PointHistoryViewModel(
    private val txRepo: PointTransactionRepo = PointTransactionRepoImpl()
): ViewModel() {
    private val _transactions = MutableStateFlow<List<PointTransaction>>(emptyList())
    val transactions: StateFlow<List<PointTransaction>> = _transactions.asStateFlow()

    private var listener: ValueEventListener? = null

    fun observeRecent(userId: String) {
        listener?.let { txRepo.removeListener(it) }
        viewModelScope.launch {
            listener = txRepo.observeRecentTransactions(userId) {
                _transactions.value = it
            }
        }
    }

    fun deleteTransaction(transactionId: String, callback: (Boolean, String) -> Unit) {
        txRepo.deleteTransaction(transactionId, callback)
    }

    fun deleteAllTransactions(userId: String, callback: (Boolean, String) -> Unit) {
        txRepo.deleteAllTransactions(userId, callback)
    }

    override fun onCleared() {
        super.onCleared()
        listener?.let { txRepo.removeListener(it) }
    }
}


