package com.example.tradeflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.model.RequestModel
import com.example.tradeflow.repository.UserNotificationRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserNotificationViewModel(private val repository: UserNotificationRepoImpl) : ViewModel() {

    private val _notifications = MutableStateFlow<List<UserNotificationModel>>(emptyList())
    val notifications: StateFlow<List<UserNotificationModel>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun createItemRequest(
        product: com.example.tradeflow.model.ProductModel,
        requester: com.example.tradeflow.model.UserModel,
        message: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Create request
                val request = RequestModel(
                    productId = product.productId,
                    productName = product.name,
                    productImage = product.imageUrl,
                    productPrice = product.price,
                    productType = product.type,
                    ownerId = product.ownerId,
                    ownerName = "", // You can fetch owner name if needed
                    requesterId = requester.userId,
                    requesterName = requester.name,
                    requesterImage = requester.profileImageUrl,
                    requesterMessage = message,
                    status = "PENDING"
                )

                repository.createRequest(request) { success, requestId ->
                    if (success) {
                        // Create notification for owner
                        val notification = UserNotificationModel(
                            type = "REQUEST",
                            title = "New ${product.type} Request",
                            message = "${requester.name} wants to ${product.type.lowercase()} your ${product.name}",
                            senderId = requester.userId,
                            senderName = requester.name,
                            senderImage = requester.profileImageUrl,
                            receiverId = product.ownerId,
                            productId = product.productId,
                            productName = product.name,
                            productImage = product.imageUrl,
                            requestId = requestId,
                            isRead = false
                        )

                        repository.createNotification(notification) { notifSuccess, _ ->
                            if (notifSuccess) {
                                Log.d("TF_REQUEST_FLOW", "Request and notification created successfully")
                                onResult(true, "Request sent successfully!")
                            } else {
                                Log.e("TF_REQUEST_FLOW", "Failed to create notification")
                                onResult(false, "Request sent but notification failed")
                            }
                        }
                    } else {
                        Log.e("TF_REQUEST_FLOW", "Failed to create request")
                        onResult(false, "Failed to send request")
                    }
                }
            } catch (e: Exception) {
                Log.e("TF_REQUEST_FLOW", "Error: ${e.message}")
                onResult(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            repository.getNotifications(userId) { success, _, notificationList ->
                if (success) {
                    _notifications.value = notificationList ?: emptyList()
                    _unreadCount.value = notificationList?.count { !it.isRead } ?: 0
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId) { success, _ ->
                if (success) {
                    // Update local state
                    _notifications.value = _notifications.value.map { notif ->
                        if (notif.notificationId == notificationId) {
                            notif.copy(isRead = true)
                        } else {
                            notif
                        }
                    }
                    _unreadCount.value = _notifications.value.count { !it.isRead }
                }
            }
        }
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            repository.markAllAsRead(userId) { success, _ ->
                if (success) {
                    // Update local state
                    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                    _unreadCount.value = 0
                }
            }
        }
    }

    fun getUnreadCount(userId: String) {
        viewModelScope.launch {
            repository.getUnreadCount(userId) { success, _, count ->
                if (success) {
                    _unreadCount.value = count
                }
            }
        }
    }

    fun acceptRequest(
        requestId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.getRequestById(requestId) { success, _, request ->
                    if (success && request != null) {
                        // Update request status
                        repository.updateRequestStatus(requestId, "ACCEPTED") { updateSuccess, _ ->
                            if (updateSuccess) {
                                // Create notification for requester
                                val notification = UserNotificationModel(
                                    type = "ACCEPTED",
                                    title = "Request Accepted",
                                    message = "Your request for ${request.productName} has been accepted!",
                                    senderId = request.ownerId,
                                    senderName = request.ownerName,
                                    senderImage = "",
                                    receiverId = request.requesterId,
                                    productId = request.productId,
                                    productName = request.productName,
                                    productImage = request.productImage,
                                    requestId = requestId,
                                    isRead = false
                                )

                                repository.createNotification(notification) { notifSuccess, _ ->
                                    if (notifSuccess) {
                                        onResult(true, "Request accepted successfully!")
                                    } else {
                                        onResult(false, "Request accepted but notification failed")
                                    }
                                }
                            } else {
                                onResult(false, "Failed to update request status")
                            }
                        }
                    } else {
                        onResult(false, "Request not found")
                    }
                }
            } catch (e: Exception) {
                Log.e("TF_REQUEST_FLOW", "Error accepting request: ${e.message}")
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun rejectRequest(
        requestId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.getRequestById(requestId) { success, _, request ->
                    if (success && request != null) {
                        // Update request status
                        repository.updateRequestStatus(requestId, "REJECTED") { updateSuccess, _ ->
                            if (updateSuccess) {
                                // Create notification for requester
                                val notification = UserNotificationModel(
                                    type = "REJECTED",
                                    title = "Request Declined",
                                    message = "Your request for ${request.productName} has been declined.",
                                    senderId = request.ownerId,
                                    senderName = request.ownerName,
                                    senderImage = "",
                                    receiverId = request.requesterId,
                                    productId = request.productId,
                                    productName = request.productName,
                                    productImage = request.productImage,
                                    requestId = requestId,
                                    isRead = false
                                )

                                repository.createNotification(notification) { notifSuccess, _ ->
                                    if (notifSuccess) {
                                        onResult(true, "Request rejected")
                                    } else {
                                        onResult(false, "Request rejected but notification failed")
                                    }
                                }
                            } else {
                                onResult(false, "Failed to update request status")
                            }
                        }
                    } else {
                        onResult(false, "Request not found")
                    }
                }
            } catch (e: Exception) {
                Log.e("TF_REQUEST_FLOW", "Error rejecting request: ${e.message}")
                onResult(false, "Error: ${e.message}")
            }
        }
    }
}
