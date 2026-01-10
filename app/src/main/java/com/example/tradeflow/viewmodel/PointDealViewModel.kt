package com.example.tradeflow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.repository.PointDealRepo

class PointDealViewModel(val repo: PointDealRepo) : ViewModel() {
    
    private val _allDeals = MutableLiveData<List<PointDealModel>?>()
    val allDeals: MutableLiveData<List<PointDealModel>?> get() = _allDeals

    private val _activeDeals = MutableLiveData<List<PointDealModel>?>()
    val activeDeals: MutableLiveData<List<PointDealModel>?> get() = _activeDeals

    fun addPointDeal(model: PointDealModel, callback: (Boolean, String) -> Unit) {
        repo.addPointDeal(model, callback)
    }

    fun updatePointDeal(model: PointDealModel, callback: (Boolean, String) -> Unit) {
        repo.updatePointDeal(model, callback)
    }

    fun deletePointDeal(dealId: String, callback: (Boolean, String) -> Unit) {
        repo.deletePointDeal(dealId, callback)
    }

    fun getAllPointDeals() {
        repo.getAllPointDeals { success, message, data ->
            if (success) {
                _allDeals.postValue(data)
            } else {
                _allDeals.postValue(emptyList())
            }
        }
    }

    fun getActivePointDeals() {
        repo.getActivePointDeals { success, message, data ->
            if (success) {
                _activeDeals.postValue(data)
            } else {
                _activeDeals.postValue(emptyList())
            }
        }
    }

    fun getPointDealsByTier(tier: String) {
        repo.getPointDealsByTier(tier) { success, message, data ->
            if (success) {
                _allDeals.postValue(data)
            } else {
                _allDeals.postValue(emptyList())
            }
        }
    }
}



