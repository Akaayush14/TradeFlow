package com.example.tradeflow.repository

import com.example.tradeflow.model.NotificationModel

interface NotificationRepo {
    fun addNotification(notification: NotificationModel, callback: (Boolean, String) -> Unit)
    fun getAllNotifications(callback: (List<NotificationModel>?) -> Unit)
    fun markAsRead(notificationId: String, callback: (Boolean) -> Unit)
    fun markAllAsRead(callback: (Boolean) -> Unit)
    fun getUnreadCount(callback: (Int) -> Unit)
    fun startListeningToUnreadCount(callback: (Int) -> Unit)
    fun stopListeningToUnreadCount()
    fun deleteNotification(notificationId: String, callback: (Boolean, String) -> Unit)
    fun deleteAllNotifications(callback: (Boolean, String) -> Unit)
}