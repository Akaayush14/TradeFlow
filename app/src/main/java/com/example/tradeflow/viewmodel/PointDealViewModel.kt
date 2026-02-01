package com.example.tradeflow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.model.UserPointRedemModel
import com.example.tradeflow.repository.PointDealRepo
import com.example.tradeflow.repository.RedemptionRepo
import com.example.tradeflow.repository.RedemptionRepoImpl
import com.example.tradeflow.repository.PointTransactionRepo
import com.example.tradeflow.repository.PointTransactionRepoImpl
import com.example.tradeflow.model.PointTransaction
import com.example.tradeflow.repository.UserRepo
import com.google.firebase.auth.FirebaseAuth

import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.repository.UserNotificationRepo
import com.example.tradeflow.repository.UserNotificationRepoImpl

class PointDealViewModel(
    val repo: PointDealRepo,
    val userRepo: UserRepo
) : ViewModel() {

    private val _allDeals = MutableLiveData<List<PointDealModel>?>()
    val allDeals: MutableLiveData<List<PointDealModel>?> get() = _allDeals

    private val _activeDeals = MutableLiveData<List<PointDealModel>?>()
    val activeDeals: MutableLiveData<List<PointDealModel>?> get() = _activeDeals

    private val _userGiftDeals = MutableLiveData<List<PointDealModel>?>()
    val userGiftDeals: MutableLiveData<List<PointDealModel>?> get() = _userGiftDeals

    private val _users = MutableLiveData<List<UserModel>?>()
    val users: MutableLiveData<List<UserModel>?> get() = _users

    private val _redemptionStatus = MutableLiveData<Pair<Boolean, String>?>()
    val redemptionStatus: MutableLiveData<Pair<Boolean, String>?> get() = _redemptionStatus

    private val _userRedemptions = MutableLiveData<Set<String>>()
    val userRedemptions: MutableLiveData<Set<String>> get() = _userRedemptions

    private val redemptionRepo: RedemptionRepo = RedemptionRepoImpl()
    private val txRepo: PointTransactionRepo = PointTransactionRepoImpl()
    private val notificationRepo: UserNotificationRepo = UserNotificationRepoImpl()

    // Client-side lock to prevent race conditions (double claiming)
    private val _processingDealIds = mutableSetOf<String>()
    private val _processedPurchaseIds = mutableSetOf<String>()

    fun addPointDeal(model: PointDealModel, callback: (Boolean, String) -> Unit) {
        repo.addPointDeal(model, callback)
    }

    fun updatePointDeal(model: PointDealModel, callback: (Boolean, String) -> Unit) {
        repo.updatePointDeal(model, callback)
    }

    fun deletePointDeal(dealId: String, callback: (Boolean, String) -> Unit) {
        repo.getPointDealById(dealId) { success, _, deal ->
            if (success && deal != null && deal.notificationId.isNotEmpty()) {
                notificationRepo.deleteNotification(deal.notificationId) { _, _ ->
                    // Proceed to delete deal regardless of notification deletion success
                    repo.deletePointDeal(dealId, callback)
                }
            } else {
                repo.deletePointDeal(dealId, callback)
            }
        }
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

    fun getAllUsers() {
        userRepo.getAllUser { success, message, data ->
            if (success) {
                _users.postValue(data)
            } else {
                _users.postValue(emptyList())
            }
        }
    }

    fun getEligibleDealsForUser(userId: String, userTier: String) {
        repo.getActivePointDeals { success, message, deals ->
            if (!success) {
                _activeDeals.postValue(emptyList())
                _userGiftDeals.postValue(emptyList())
                return@getActivePointDeals
            }
            redemptionRepo.getRedemptionsByUserId(userId) { rSuccess, _, redemptions ->
                val claimedIds = redemptions?.map { it.dealId }?.toSet() ?: emptySet()
                _userRedemptions.postValue(claimedIds)

                val filtered = deals?.filter { deal ->
                    // We now show claimed deals too, so we don't filter by claimedIds
                    // val isNotClaimed = deal.dealId !in claimedIds

                    val isTargetUserMatch = if (deal.targetUserId.isNotEmpty()) {
                        deal.targetUserId == userId
                    } else {
                        true // If no target user, proceed to tier check
                    }

                    val isTierMatch = if (deal.targetUserId.isNotEmpty()) {
                        true // If target user is set, ignore tier
                    } else {
                        when (userTier.lowercase()) {
                            "gold" -> true // Gold sees all tiers
                            "silver" -> !deal.tier.equals("Gold", ignoreCase = true) // Silver sees everything except Gold
                            else -> deal.tier.equals("Bronze", ignoreCase = true) || deal.tier.equals("All", ignoreCase = true) // Bronze sees Bronze and All
                        }
                    }

                    isTargetUserMatch && isTierMatch
                } ?: emptyList()
                
                // Split into public deals and personal gift deals
                val publicDeals = filtered.filter { it.targetUserId.isEmpty() }
                val giftDeals = filtered.filter { it.targetUserId == userId }

                _activeDeals.postValue(publicDeals)
                _userGiftDeals.postValue(giftDeals)
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

                            // Try to reserve the deal (Locking mechanism)
                            redemptionRepo.createRedemptionPlaceholder(userId, dealId) { lockSuccess, lockMessage ->
                                if (!lockSuccess) {
                                    _processingDealIds.remove(dealId)
                                    _redemptionStatus.postValue(Pair(false, lockMessage))
                                    return@createRedemptionPlaceholder
                                }

                                // Deal reserved, now deduct points
                                userRepo.updateUserPoints(userId, -pointsRequired) { pointsSuccess, pointsMessage ->
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
                                                        source = "Congratulation ! ${deal.offer} IN ${deal.serviceCategory.ifEmpty { deal.title }}",
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
                                        // Failed to deduct points, rollback reservation
                                        redemptionRepo.removeRedemption(userId, dealId) { _, _ -> }
                                        _processingDealIds.remove(dealId)
                                        _redemptionStatus.postValue(Pair(false, pointsMessage))
                                    }
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

                // Check expiration
                if (deal.validTill > 0 && deal.validTill < System.currentTimeMillis()) {
                    _processingDealIds.remove(dealId)
                    _redemptionStatus.postValue(Pair(false, "This deal has expired"))
                    return@getPointDealById
                }

                userRepo.getUserByIdSingle(userId) { success, message, user ->
                    if (success && user != null) {
                        val reward = deal.rewardPoints
                        if (reward > 0L) {
                            userRepo.updateUserPoints(userId, reward) { pointsSuccess, pointsMessage ->
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
                                            // Create transaction record for ALL claims (including gifts)
                                            // This ensures it appears in the user's Point History
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

    fun buyPoints(userId: String, points: Long, amount: Double, purchaseId: String) {
        if (_processedPurchaseIds.contains(purchaseId)) {
            return
        }
        _processedPurchaseIds.add(purchaseId)

        userRepo.getUserByIdSingle(userId) { success, message, user ->
            if (success && user != null) {
                userRepo.updateUserPoints(userId, points) { pointsSuccess, pointsMessage ->
                    if (pointsSuccess) {
                        txRepo.saveTransaction(
                            PointTransaction(
                                userId = userId,
                                type = "CREDIT",
                                source = "Congratulations! You got $points points",
                                points = points,
                                amount = amount,
                                timestamp = System.currentTimeMillis()
                            )
                        ) { _, _ -> }
                        _redemptionStatus.postValue(Pair(true, "Points purchased successfully!"))
                    } else {
                        // If update fails, we might want to allow retrying, so remove the ID?
                        // For now, keep it to prevent double charging risk, or remove it?
                        // If it fails, the user didn't get points. We should probably remove it.
                        _processedPurchaseIds.remove(purchaseId)
                        _redemptionStatus.postValue(Pair(false, pointsMessage))
                    }
                }
            } else {
                _processedPurchaseIds.remove(purchaseId)
                _redemptionStatus.postValue(Pair(false, "User not found"))
            }
        }
    }

    fun buyDealDirectly(userId: String, deal: PointDealModel, amount: Double) {
        // 1. Save Transaction History with custom message
        txRepo.saveTransaction(
            PointTransaction(
                userId = userId,
                type = "DEAL_PURCHASE",
                source = "Congratulation ! ${deal.offer} IN ${deal.serviceCategory.ifEmpty { deal.title }}",
                points = 0,
                amount = amount,
                timestamp = System.currentTimeMillis()
            )
        ) { txSuccess, _ ->
            if (txSuccess) {
                // 2. Save Redemption Record (so it shows as claimed)
                val redemption = UserPointRedemModel(
                    redemptionId = "",
                    userId = userId,
                    dealId = deal.dealId,
                    pointsSpent = 0, // Paid with cash
                    dealTitle = deal.title,
                    dealOffer = deal.offer
                )
                redemptionRepo.saveRedemption(redemption) { rSuccess, _ ->
                    if (rSuccess) {
                        _redemptionStatus.postValue(Pair(true, "Payment successful! You got ${deal.offer}"))
                    } else {
                        _redemptionStatus.postValue(Pair(false, "Payment successful but failed to save redemption"))
                    }
                }
            } else {
                _redemptionStatus.postValue(Pair(false, "Failed to save transaction"))
            }
        }
    }

    fun giftPointsToUser(targetUserId: String, points: Long, dealTitle: String, validTill: Long, callback: (Boolean, String) -> Unit) {
        userRepo.getUserByIdSingle(targetUserId) { success, message, user ->
            if (success && user != null) {
                 val dealRecord = PointDealModel(
                     title = "Gift to ${user.name}",
                     offer = "Claim $points Free Points",
                     tier = "All",
                     serviceCategory = "Admin Gift",
                     pointsRequired = 0,
                     rewardPoints = points,
                     targetUserId = targetUserId,
                     isActive = true,
                     validTill = validTill,
                     notificationId = ""
                 )
                 repo.addPointDeal(dealRecord) { dealSuccess, dealMsg -> 
                    if (dealSuccess) {
                        callback(true, "Gift sent to ${user.name}! They can now claim it.")
                    } else {
                        callback(false, "Failed to create gift deal: $dealMsg")
                    }
                 }
            } else {
                callback(false, message)
            }
        }
    }

    fun deleteRedemption(userId: String, dealId: String) {
        redemptionRepo.removeRedemption(userId, dealId) { success, message ->
            if (success) {
                _redemptionStatus.postValue(Pair(true, "Redemption deleted"))
                val current = _userRedemptions.value?.toMutableSet() ?: mutableSetOf()
                current.remove(dealId)
                _userRedemptions.postValue(current)
            } else {
                _redemptionStatus.postValue(Pair(false, message))
            }
        }
    }

    fun deleteAllRedemptions(userId: String) {
        redemptionRepo.deleteAllRedemptionsForUser(userId) { success, message ->
            if (success) {
                _redemptionStatus.postValue(Pair(true, "All redemptions deleted"))
                _userRedemptions.postValue(emptySet())
            } else {
                _redemptionStatus.postValue(Pair(false, message))
            }
        }
    }

    fun clearRedemptionStatus() {
        _redemptionStatus.value = null
    }
}