package com.example.tradeflow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.model.UserPointRedemModel
import com.example.tradeflow.repository.PointDealRepo
import com.example.tradeflow.repository.RedemptionRepoImpl
import com.example.tradeflow.repository.UserRepo
import com.google.firebase.auth.FirebaseAuth

class PointDealViewModel(
    val repo: PointDealRepo,
    val userRepo: UserRepo  // ADD THIS
) : ViewModel() {

    private val _allDeals = MutableLiveData<List<PointDealModel>?>()
    val allDeals: MutableLiveData<List<PointDealModel>?> get() = _allDeals

    private val _activeDeals = MutableLiveData<List<PointDealModel>?>()
    val activeDeals: MutableLiveData<List<PointDealModel>?> get() = _activeDeals

    private val _redemptionStatus = MutableLiveData<Pair<Boolean, String>?>()
    val redemptionStatus: MutableLiveData<Pair<Boolean, String>?> get() = _redemptionStatus

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

    fun redeemPointDeal(dealId: String, pointsRequired: Long, dealTitle: String, dealOffer: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        userRepo.getUserById(userId) { success, message, user ->
            if (success && user != null) {
                if (user.points >= pointsRequired) {
                    val updatedPoints = user.points - pointsRequired
                    userRepo.updateUserPoints(userId, updatedPoints) { pointsSuccess, pointsMessage ->
                        if (pointsSuccess) {
                            val redemption = UserPointRedemModel(
                                redemptionId = "",
                                userId = userId,
                                dealId = dealId,
                                pointsSpent = pointsRequired,
                                dealTitle = dealTitle,
                                dealOffer = dealOffer
                            )
                            saveRedemptionRecord(redemption)
                            _redemptionStatus.postValue(Pair(true, "Deal redeemed successfully!"))
                        } else {
                            _redemptionStatus.postValue(Pair(false, pointsMessage))
                        }
                    }
                } else {
                    _redemptionStatus.postValue(Pair(false, "Insufficient points!"))
                }
            } else {
                _redemptionStatus.postValue(Pair(false, message))
            }
        }
    }

    private fun saveRedemptionRecord(redemption: UserPointRedemModel) {
        val redemptionRepo = RedemptionRepoImpl()
        redemptionRepo.saveRedemption(redemption) { _, _ -> }
    }

    fun clearRedemptionStatus() {
        _redemptionStatus.value = null
    }
}
