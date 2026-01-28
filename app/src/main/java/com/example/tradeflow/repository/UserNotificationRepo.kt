package com.example.tradeflow.repository

import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.model.RequestModel

interface UserNotificationRepo {
    fun createRequest(
        request: RequestModel,
        callback: (Boolean, String) -> Unit
    )

    fun createNotification(
        notification: UserNotificationModel,
        callback: (Boolean, String) -> Unit
    )

    fun getNotifications(
        userId: String,
        callback: (Boolean, String, List<UserNotificationModel>?) -> Unit
    )

    fun markAsRead(
        notificationId: String,
        callback: (Boolean, String) -> Unit
    )

    fun markAllAsRead(
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getUnreadCount(
        userId: String,
        callback: (Boolean, String, Int) -> Unit
    )

    fun getRequestById(
        requestId: String,
        callback: (Boolean, String, RequestModel?) -> Unit
    )

    fun updateRequestStatus(
        requestId: String,
        status: String,
        callback: (Boolean, String) -> Unit
    )

    fun getRequestsByOwner(
        ownerId: String,
        callback: (Boolean, String, List<RequestModel>?) -> Unit
    )

    fun getRequestsByRequester(
        requesterId: String,
        callback: (Boolean, String, List<RequestModel>?) -> Unit
    )

    fun updateNotificationStatus(
        notificationId: String,
        status: String,
        callback: (Boolean, String) -> Unit
    )

    fun deleteNotification(
        notificationId: String,
        callback: (Boolean, String) -> Unit
    )
}
