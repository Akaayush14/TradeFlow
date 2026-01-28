package com.example.tradeflow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.model.UserPointRedemModel
import com.example.tradeflow.repository.PointDealRepo
import com.example.tradeflow.repository.RedemptionRepo
import com.example.tradeflow.repository.RedemptionRepoImpl
import com.example.tradeflow.repository.PointTransactionRepo
import com.example.tradeflow.repository.PointTransactionRepoImpl
import com.example.tradeflow.model.PointTransaction
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
    private val txRepo: PointTransactionRepo = PointTransactionRepoImpl()

    // Client-side lock to prevent race conditions (double claiming)
    private val _processingDealIds = mutableSetOf<String>()

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

    fun getEligibleDealsForUser(userId: String, userTier: String) {
        repo.getActivePointDeals { success, message, deals ->
            if (!success) {
                _activeDeals.postValue(emptyList())
                return@getActivePointDeals
            }
            redemptionRepo.getRedemptionsByUserId(userId) { rSuccess, _, redemptions ->
                val claimedIds = redemptions?.map { it.dealId }?.toSet() ?: emptySet()
                val filtered = deals?.filter { deal ->
                    val isNotClaimed = deal.dealId !in claimedIds
                    
                    val isTierMatch = when (userTier.lowercase()) {
                        "gold" -> true // Gold sees all tiers
                        "silver" -> !deal.tier.equals("Gold", ignoreCase = true) // Silver sees everything except Gold
                        else -> deal.tier.equals("Bronze", ignoreCase = true) || deal.tier.equals("All", ignoreCase = true) // Bronze sees Bronze and All
                    }
                    
                    isNotClaimed && isTierMatch
                } ?: emptyList()
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

        // Prevent multiple simultaneous clicks for the same deal
        if (_processingDealIds.contains(dealId)) {
            return
        }
        _processingDealIds.add(dealId)

        redemptionRepo.hasUserClaimedDeal(userId, dealId) { claimedSuccess, _, alreadyClaimed ->
            if (!claimedSuccess) {
                _processingDealIds.remove(dealId)
                _redemptionStatus.postValue(Pair(false, "Failed to verify claim status"))
                return@hasUserClaimedDeal
            }
            if (alreadyClaimed) {
                _processingDealIds.remove(dealId)
                _redemptionStatus.postValue(Pair(false, "You have already claimed this deal"))
                return@hasUserClaimedDeal
            }

            // Fetch deal to verify
            repo.getPointDealById(dealId) { dealSuccess, _, deal ->
                if (!dealSuccess || deal == null) {
                    _processingDealIds.remove(dealId)
                    _redemptionStatus.postValue(Pair(false, "Deal not found"))
                    return@getPointDealById
                }

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
                                    redemptionRepo.saveRedemption(redemption) { rSuccess, _ ->
                                        if (rSuccess) {
                                            txRepo.saveTransaction(
                                                PointTransaction(
                                                    userId = userId,
                                                    type = "DEBIT",
                                                    source = "Deal Redemption: ${deal.title}",
                                                    points = -pointsRequired,
                                                    amount = 0.0,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            ) { _, _ -> }
                                            _processingDealIds.remove(dealId)
                                            _redemptionStatus.postValue(Pair(true, "Deal redeemed successfully!"))
                                        } else {
                                            _processingDealIds.remove(dealId)
                                            _redemptionStatus.postValue(Pair(false, "Failed to save redemption record"))
                                        }
                                    }
                                } else {
                                    _processingDealIds.remove(dealId)
                                    _redemptionStatus.postValue(Pair(false, pointsMessage))
                                }
                            }
                        } else {
                            _processingDealIds.remove(dealId)
                            _redemptionStatus.postValue(Pair(false, "Insufficient points!"))
                        }
                    } else {
                        _processingDealIds.remove(dealId)
                        _redemptionStatus.postValue(Pair(false, "User not found"))
                    }
                }
            }
        }
    }

    fun claimPointDeal(dealId: String, dealTitle: String, dealOffer: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        // Prevent multiple simultaneous clicks for the same deal
        if (_processingDealIds.contains(dealId)) {
            return
        }
        _processingDealIds.add(dealId)

        redemptionRepo.hasUserClaimedDeal(userId, dealId) { claimedSuccess, _, alreadyClaimed ->
            if (!claimedSuccess) {
                _processingDealIds.remove(dealId)
                _redemptionStatus.postValue(Pair(false, "Failed to verify claim status"))
                return@hasUserClaimedDeal
            }
            if (alreadyClaimed) {
                _processingDealIds.remove(dealId)
                _redemptionStatus.postValue(Pair(false, "You have already claimed this deal"))
                return@hasUserClaimedDeal
            }

            // Fetch deal to determine reward
            repo.getPointDealById(dealId) { dealSuccess, _, deal ->
                if (!dealSuccess || deal == null) {
                    _processingDealIds.remove(dealId)
                    _redemptionStatus.postValue(Pair(false, "Deal not found"))
                    return@getPointDealById
                }

                userRepo.getUserByIdSingle(userId) { success, message, user ->
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
                                    redemptionRepo.saveRedemption(redemption) { rSuccess, _ ->
                                        if (rSuccess) {
                                            txRepo.saveTransaction(
                                                PointTransaction(
                                                    userId = userId,
                                                    type = "CREDIT",
                                                    source = "Free Points Claim: ${deal.title}",
                                                    points = reward,
                                                    amount = 0.0,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            ) { _, _ -> }
                                            _processingDealIds.remove(dealId)
                                            _redemptionStatus.postValue(Pair(true, "Free points claimed successfully!"))
                                        } else {
                                            _processingDealIds.remove(dealId)
                                            _redemptionStatus.postValue(Pair(false, "Failed to save claim record"))
                                        }
                                    }
                                } else {
                                    _processingDealIds.remove(dealId)
                                    _redemptionStatus.postValue(Pair(false, pointsMessage))
                                }
                            }
                        } else {
                            _processingDealIds.remove(dealId)
                            _redemptionStatus.postValue(Pair(false, "This deal has no free points to claim"))
                        }
                    } else {
                        _processingDealIds.remove(dealId)
                        _redemptionStatus.postValue(Pair(false, "User not found"))
                    }
                }
            }
        }
    }

    fun buyPoints(userId: String, points: Long, amount: Double) {
        userRepo.getUserByIdSingle(userId) { success, message, user ->
            if (success && user != null) {
                val updatedPoints = user.points + points
                userRepo.updateUserPoints(userId, updatedPoints) { pointsSuccess, pointsMessage ->
                    if (pointsSuccess) {
                        txRepo.saveTransaction(
                            PointTransaction(
                                userId = userId,
                                type = "CREDIT",
                                source = "Point Purchase",
                                points = points,
                                amount = amount,
                                timestamp = System.currentTimeMillis()
                            )
                        ) { _, _ -> }
                        _redemptionStatus.postValue(Pair(true, "Points purchased successfully!"))
                    } else {
                        _redemptionStatus.postValue(Pair(false, pointsMessage))
                    }
                }
            } else {
                _redemptionStatus.postValue(Pair(false, "User not found"))
            }
        }
    }

    fun clearRedemptionStatus() {
        _redemptionStatus.value = null
    }
}
