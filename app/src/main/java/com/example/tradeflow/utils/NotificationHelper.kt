package com.example.tradeflow.utils

import com.example.tradeflow.model.NotificationModel
import com.example.tradeflow.repository.NotificationRepo

class NotificationHelper(private val notificationRepo: NotificationRepo) {

    companion object {
        const val TYPE_ITEM_ADDED = "item_added"
        const val TYPE_ITEM_APPROVED = "item_approved"
        const val TYPE_ITEM_REJECTED = "item_rejected"
        const val TYPE_TRADE_REQUEST = "trade_request"
        const val TYPE_TRADE_ACCEPTED = "trade_accepted"
        const val TYPE_TRADE_REJECTED = "trade_rejected"
        const val TYPE_USER_BLOCKED = "user_blocked"
        const val TYPE_USER_UNBLOCKED = "user_unblocked"
        const val TYPE_USER_RESTRICTED = "user_restricted"
        const val TYPE_USER_UNRESTRICTED = "user_unrestricted"
        const val TYPE_POINTS_AWARDED = "points_awarded"
        const val TYPE_SYSTEM = "system"
    }

    fun notifyItemAdded(userId: String, itemId: String, itemTitle: String) {
        val notification = NotificationModel(
            message = "Your item '$itemTitle' has been submitted for approval",
            type = TYPE_ITEM_ADDED,
            itemId = itemId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyItemApproved(userId: String, itemId: String, itemTitle: String) {
        val notification = NotificationModel(
            message = "Great news! Your item '$itemTitle' has been approved and is now live",
            type = TYPE_ITEM_APPROVED,
            itemId = itemId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyItemRejected(userId: String, itemId: String, itemTitle: String, reason: String = "") {
        val message = if (reason.isNotEmpty()) {
            "Your item '$itemTitle' was rejected. Reason: $reason"
        } else {
            "Your item '$itemTitle' was rejected"
        }
        val notification = NotificationModel(
            message = message,
            type = TYPE_ITEM_REJECTED,
            itemId = itemId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyTradeRequest(userId: String, itemId: String, itemTitle: String, requesterName: String) {
        val notification = NotificationModel(
            message = "$requesterName wants to trade for your item '$itemTitle'",
            type = TYPE_TRADE_REQUEST,
            itemId = itemId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyTradeAccepted(userId: String, itemId: String, itemTitle: String) {
        val notification = NotificationModel(
            message = "Your trade request for '$itemTitle' has been accepted!",
            type = TYPE_TRADE_ACCEPTED,
            itemId = itemId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyTradeRejected(userId: String, itemId: String, itemTitle: String) {
        val notification = NotificationModel(
            message = "Your trade request for '$itemTitle' was declined",
            type = TYPE_TRADE_REJECTED,
            itemId = itemId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyUserBlocked(userId: String) {
        val notification = NotificationModel(
            message = "Your account has been blocked by an administrator",
            type = TYPE_USER_BLOCKED,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyUserUnblocked(userId: String) {
        val notification = NotificationModel(
            message = "Your account has been unblocked. You can now use all features",
            type = TYPE_USER_UNBLOCKED,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyUserRestricted(userId: String) {
        val notification = NotificationModel(
            message = "Your account has been restricted. Some features may be limited",
            type = TYPE_USER_RESTRICTED,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyUserUnrestricted(userId: String) {
        val notification = NotificationModel(
            message = "Your account restrictions have been lifted",
            type = TYPE_USER_UNRESTRICTED,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun notifyPointsAwarded(userId: String, points: Long, reason: String = "trade completion") {
        val notification = NotificationModel(
            message = "You've earned $points points for $reason!",
            type = TYPE_POINTS_AWARDED,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }

    fun sendSystemNotification(userId: String, message: String) {
        val notification = NotificationModel(
            message = message,
            type = TYPE_SYSTEM,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationRepo.addNotification(notification) { _, _ -> }
    }
}