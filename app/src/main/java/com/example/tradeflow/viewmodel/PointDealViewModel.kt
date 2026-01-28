package com.example.tradeflow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.model.UserPointRedemModel
import com.example.tradeflow.repository.PointDealRepo
import com.example.tradeflow.repository.RedemptionRepo
import com.example.tradeflow.repository.RedemptionRepoImpl
import com.example.tradeflow.repository.UserRepo
import com.google.firebase.auth.FirebaseAuth

class PointDealViewModel(
    val repo: PointDealRepo,
    val userRepo: UserRepo
) : ViewModel() {

    private val _allDeals = MutableLiveData<List<PointDealModel>?>()
    val allDeals: MutableLiveData<List<PointDealModel>?> get() = _allDeals

    private val _activeDeals = MutableLiveData<List<PointDealModel>?>()
    val activeDeals: MutableLiveData<List<PointDealModel>?> get() = _activeDeals

    private val _redemptionStatus = MutableLiveData<Pair<Boolean, String>?>()
    val redemptionStatus: MutableLiveData<Pair<Boolean, String>?> get() = _redemptionStatus
    private val redemptionRepo: RedemptionRepo = RedemptionRepoImpl()

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

    fun getEligibleDealsForUser(userId: String) {
        repo.getActivePointDeals { success, message, deals ->
            if (!success) {
                _activeDeals.postValue(emptyList())
                return@getActivePointDeals
            }
            redemptionRepo.getRedemptionsByUserId(userId) { rSuccess, _, redemptions ->
                val claimedIds = redemptions?.map { it.dealId }?.toSet() ?: emptySet()
                val filtered = deals?.filter { it.dealId !in claimedIds } ?: emptyList()
                _activeDeals.postValue(filtered)
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

        redemptionRepo.hasUserClaimedDeal(userId, dealId) { claimedSuccess, _, alreadyClaimed ->
            if (!claimedSuccess) {
                _redemptionStatus.postValue(Pair(false, "Failed to verify claim status"))
                return@hasUserClaimedDeal
            }
            if (alreadyClaimed) {
                _redemptionStatus.postValue(Pair(false, "You have already claimed this deal"))
                return@hasUserClaimedDeal
            }

            // Fetch deal to determine reward or cost
            repo.getPointDealById(dealId) { dealSuccess, _, deal ->
                if (!dealSuccess || deal == null) {
                    _redemptionStatus.postValue(Pair(false, "Deal not found"))
                    return@getPointDealById
                }

                userRepo.getUserById(userId) { success, message, user ->
                    if (success && user != null) {
                        val reward = deal.rewardPoints
                        if (reward > 0L) {
                            val updatedPoints = user.points + reward
                            userRepo.updateUserPoints(userId, updatedPoints) { pointsSuccess, pointsMessage ->
                                if (pointsSuccess) {
                                    val redemption = UserPointRedemModel(
                                        redemptionId = "",
                                        userId = userId,
                                        dealId = dealId,
                                        pointsSpent = 0L,
                                        dealTitle = dealTitle,
                                        dealOffer = dealOffer
                                    )
                                    saveRedemptionRecord(redemption)
                                    _redemptionStatus.postValue(Pair(true, "Free points claimed successfully!"))
                                } else {
                                    _redemptionStatus.postValue(Pair(false, pointsMessage))
                                }
                            }
                        } else {
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
                        }
                    } else {
                        _redemptionStatus.postValue(Pair(false, message))
                    }
                }
            }
        }
    }

    private fun saveRedemptionRecord(redemption: UserPointRedemModel) {
        redemptionRepo.saveRedemption(redemption) { _, _ -> }
    }

    fun clearRedemptionStatus() {
        _redemptionStatus.value = null
    }
}
