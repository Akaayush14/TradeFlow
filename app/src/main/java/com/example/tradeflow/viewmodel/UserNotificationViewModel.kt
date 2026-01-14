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

import java.text.SimpleDateFormat
import java.util.*

class UserNotificationViewModel(private val repository: UserNotificationRepoImpl) : ViewModel() {

    private val _notifications = MutableStateFlow<List<UserNotificationModel>>(emptyList())
    val notifications: StateFlow<List<UserNotificationModel>> = _notifications

    private val _myRequests = MutableStateFlow<List<RequestModel>>(emptyList())
    val myRequests: StateFlow<List<RequestModel>> = _myRequests

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Enhanced method to create item request with complete notification details
     * Supports both Barter and Rent requests
     */
    fun createItemRequest(
        product: com.example.tradeflow.model.ProductModel,
        owner: com.example.tradeflow.model.UserModel, // Owner details
        requester: com.example.tradeflow.model.UserModel,
        requestType: String, // "BARTER" or "RENT"
        message: String = "",
        offerProduct: com.example.tradeflow.model.ProductModel? = null, // For barter
        rentalStartDate: Long = 0L, // For rent
        rentalEndDate: Long = 0L, // For rent
        rentalPricePerDay: Double = 0.0, // For rent
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Calculate rental details if it's a rental request
                val rentalDays = if (rentalStartDate > 0 && rentalEndDate > 0) {
                    ((rentalEndDate - rentalStartDate) / (1000 * 60 * 60 * 24)).toInt() + 1
                } else 0

                val rentalTotalPrice = rentalPricePerDay * rentalDays
                val rentalPeriod = formatRentalPeriod(rentalStartDate, rentalEndDate)
                val rentalPriceFormatted = if (rentalPricePerDay > 0) "$rentalPricePerDay/day" else ""

                // Create request with all details
                val request = RequestModel(
                    productId = product.productId,
                    productName = product.name,
                    productImage = product.imageUrl,
                    productPrice = product.price,
                    productType = requestType,
                    ownerId = product.ownerId,
                    ownerName = owner.name,
                    ownerImage = owner.profileImageUrl,
                    requesterId = requester.userId,
                    requesterName = requester.name,
                    requesterImage = requester.profileImageUrl,
                    requesterRating = 0.0, // Default rating since UserModel doesn't have rating field
                    requesterReviewCount = 0, // Default review count since UserModel doesn't have reviewCount field
                    requesterMessage = message,
                    offerProductId = offerProduct?.productId ?: "",
                    offerProductName = offerProduct?.name ?: "",
                    offerProductImage = offerProduct?.imageUrl ?: "",
                    offerProductPrice = offerProduct?.price ?: 0.0,
                    rentalStartDate = rentalStartDate,
                    rentalEndDate = rentalEndDate,
                    rentalPeriod = rentalPeriod,
                    rentalPricePerDay = rentalPricePerDay,
                    rentalTotalPrice = rentalTotalPrice,
                    rentalPriceFormatted = rentalPriceFormatted,
                    status = "PENDING"
                )

                repository.createRequest(request) { success, requestId ->
                    if (success) {
                        // Create notification for owner
                        val notificationTitle = when (requestType) {
                            "BARTER" -> "New barter offer"
                            "RENT" -> "Rental request received"
                            else -> "New ${requestType.lowercase()} request"
                        }

                        val notificationMessage = when (requestType) {
                            "BARTER" -> "Someone wants to barter!"
                            "RENT" -> "@${requester.name} wants to rent your ${product.name}"
                            else -> "${requester.name} is interested in your ${product.name}"
                        }

                        val notification = UserNotificationModel(
                            type = "REQUEST",
                            requestType = requestType,
                            title = notificationTitle,
                            message = notificationMessage,
                            senderId = requester.userId,
                            senderName = requester.name,
                            senderImage = requester.profileImageUrl,
                            senderRating =  0.0,
                            senderReviewCount = 0,
                            receiverId = product.ownerId,
                            productId = product.productId,
                            productName = product.name,
                            productImage = product.imageUrl,
                            offerProductId = offerProduct?.productId ?: "",
                            offerProductName = offerProduct?.name ?: "",
                            offerProductImage = offerProduct?.imageUrl ?: "",
                            rentalPeriod = rentalPeriod,
                            rentalPrice = rentalPriceFormatted,
                            requestId = requestId,
                            isRead = false,
                            status = "" // Empty status means pending action
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

    /**
     * Helper function to format rental period for display
     */
    private fun formatRentalPeriod(startDate: Long, endDate: Long): String {
        if (startDate == 0L || endDate == 0L) return ""

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val startStr = dateFormat.format(Date(startDate))
        val endStr = dateFormat.format(Date(endDate))
        val year = yearFormat.format(Date(endDate))

        return "$startStr-${endStr}, $year"
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

    fun loadMyRequests(userId: String) {
        viewModelScope.launch {
            repository.getRequestsByRequester(userId) { success, _, requestList ->
                if (success) {
                    _myRequests.value = requestList ?: emptyList()
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId) { success, _ ->
                if (success) {
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
                                // Update local notification status
                                _notifications.value = _notifications.value.map { notif ->
                                    if (notif.requestId == requestId) {
                                        notif.copy(status = "ACCEPTED")
                                    } else {
                                        notif
                                    }
                                }

                                // Create notification for requester
                                val acceptMessage = when (request.productType) {
                                    "BARTER" -> "Barter request accepted!"
                                    "RENT" -> "Your rental request for ${request.productName} has been approved!"
                                    else -> "Your request for ${request.productName} has been accepted!"
                                }

                                val notification = UserNotificationModel(
                                    type = "ACCEPTED",
                                    requestType = request.productType,
                                    title = "Request Accepted",
                                    message = acceptMessage,
                                    senderId = request.ownerId,
                                    senderName = request.ownerName,
                                    senderImage = "",
                                    receiverId = request.requesterId,
                                    productId = request.productId,
                                    productName = request.productName,
                                    productImage = request.productImage,
                                    requestId = requestId,
                                    isRead = false,
                                    status = "ACCEPTED"
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
                                // Update local notification status
                                _notifications.value = _notifications.value.map { notif ->
                                    if (notif.requestId == requestId) {
                                        notif.copy(status = "REJECTED")
                                    } else {
                                        notif
                                    }
                                }

                                // Create notification for requester
                                val rejectMessage = when (request.productType) {
                                    "BARTER" -> "Barter request declined"
                                    "RENT" -> "Rental request for ${request.productName} has been declined."
                                    else -> "Your request for ${request.productName} has been declined."
                                }

                                val notification = UserNotificationModel(
                                    type = "REJECTED",
                                    requestType = request.productType,
                                    title = "Request Declined",
                                    message = rejectMessage,
                                    senderId = request.ownerId,
                                    senderName = request.ownerName,
                                    senderImage = "",
                                    receiverId = request.requesterId,
                                    productId = request.productId,
                                    productName = request.productName,
                                    productImage = request.productImage,
                                    requestId = requestId,
                                    isRead = false,
                                    status = "REJECTED"
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

    fun deleteNotification(
        notificationId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId) { success, message ->
                if (success) {
                    _notifications.value = _notifications.value.filter {
                        it.notificationId != notificationId
                    }
                    _unreadCount.value = _notifications.value.count { !it.isRead }
                }
                onResult(success, message)
            }
        }
    }

    fun cancelRequest(
        requestId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateRequestStatus(requestId, "CANCELED") { updateSuccess, message ->
                    if (updateSuccess) {
                        _myRequests.value = _myRequests.value.map { request ->
                            if (request.requestId == requestId) {
                                request.copy(status = "CANCELED")
                            } else {
                                request
                            }
                        }
                        onResult(true, "Request canceled")
                    } else {
                        onResult(false, message)
                    }
                }
            } catch (e: Exception) {
                Log.e("TF_REQUEST_FLOW", "Error canceling request: ${e.message}")
                onResult(false, "Error: ${e.message}")
            }
        }
    }
}
