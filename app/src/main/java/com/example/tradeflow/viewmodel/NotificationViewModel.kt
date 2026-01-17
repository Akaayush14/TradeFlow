package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.NotificationModel
import com.example.tradeflow.repository.NotificationRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val repo: NotificationRepo) : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationModel>?>(null)
    val notifications: StateFlow<List<NotificationModel>?> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        // Set up continuous listener for unread count
        startListeningToUnreadCount()
    }

    private fun startListeningToUnreadCount() {
        repo.startListeningToUnreadCount { count ->
            viewModelScope.launch {
                _unreadCount.value = count
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove listener when ViewModel is cleared
        repo.stopListeningToUnreadCount()
    }

    fun addNotification(notification: NotificationModel, callback: (Boolean, String) -> Unit) {
        repo.addNotification(notification) { success, message ->
            if (success) {
                // Refresh notifications after adding
                getAllNotifications()
                // Unread count will be updated automatically by the listener
            }
            callback(success, message)
        }
    }

    fun getAllNotifications() {
        repo.getAllNotifications { notificationsList ->
            viewModelScope.launch {
                _notifications.value = notificationsList
            }
        }
    }

    fun getUnreadCount() {
        repo.getUnreadCount { count ->
            viewModelScope.launch {
                _unreadCount.value = count
            }
        }
    }

    fun markAsRead(notificationId: String) {
        repo.markAsRead(notificationId) { success ->
            if (success) {
                getAllNotifications()
                // Unread count will be updated automatically by the listener
            }
        }
    }

    fun markAllAsRead() {
        repo.markAllAsRead { success ->
            if (success) {
                getAllNotifications()
                // Unread count will be updated automatically by the listener
            }
        }
    }

    fun deleteNotification(notificationId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteNotification(notificationId) { success, message ->
            if (success) {
                getAllNotifications()
                // Unread count will be updated automatically by the listener
            }
            callback(success, message)
        }
    }

    fun deleteAllNotifications(callback: (Boolean, String) -> Unit) {
        repo.deleteAllNotifications { success, message ->
            if (success) {
                getAllNotifications()
                // Unread count will be updated automatically by the listener
            }
            callback(success, message)
        }
    }
}