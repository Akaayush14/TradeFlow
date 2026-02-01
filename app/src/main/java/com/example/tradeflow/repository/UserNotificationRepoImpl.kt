package com.example.tradeflow.repository

import android.util.Log
import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.model.RequestModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserNotificationRepoImpl : UserNotificationRepo {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("notifications")
    private val requestsRef: DatabaseReference = database.getReference("requests")
    private var unreadCountListener: ValueEventListener? = null
    private var unreadCountQuery: com.google.firebase.database.Query? = null

    override fun createRequest(
        request: RequestModel,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            val requestId = requestsRef.push().key ?: ""
            val requestWithId = request.copy(requestId = requestId)

            requestsRef.child(requestId).setValue(requestWithId.toMap())
                .addOnSuccessListener {
                    Log.d("TF_REQUEST", "Request created successfully: $requestId")
                    callback(true, requestId)
                }
                .addOnFailureListener { e ->
                    Log.e("TF_REQUEST", "Error creating request: ${e.message}")
                    callback(false, "Failed to create request: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("TF_REQUEST", "Exception creating request: ${e.message}")
            callback(false, "Exception: ${e.message}")
        }
    }

    override fun createNotification(
        notification: UserNotificationModel,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            val notificationId = ref.push().key ?: ""
            val notificationWithId = notification.copy(notificationId = notificationId)

            ref.child(notificationId).setValue(notificationWithId.toMap())
                .addOnSuccessListener {
                    Log.d("TF_NOTIFICATION", "Notification created successfully: $notificationId")
                    callback(true, notificationId)
                }
                .addOnFailureListener { e ->
                    Log.e("TF_NOTIFICATION", "Error creating notification: ${e.message}")
                    callback(false, "Failed to create notification: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception creating notification: ${e.message}")
            callback(false, "Exception: ${e.message}")
        }
    }

    override fun getNotifications(
        userId: String,
        callback: (Boolean, String, List<UserNotificationModel>?) -> Unit
    ) {
        try {
            ref.orderByChild("receiverId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val notifications = mutableListOf<UserNotificationModel>()
                        for (child in snapshot.children) {
                            val notification = child.getValue(UserNotificationModel::class.java)
                            notification?.let {
                                notifications.add(it.copy(notificationId = child.key ?: ""))
                            }
                        }
                        callback(true, "Success", notifications.sortedByDescending { it.createdAt })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TF_NOTIFICATION", "Error getting notifications: ${error.message}")
                        callback(false, "Error: ${error.message}", null)
                    }
                })
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception getting notifications: ${e.message}")
            callback(false, "Error: ${e.message}", null)
        }
    }

    override fun markAsRead(
        notificationId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            ref.child(notificationId).child("isRead").setValue(true)
                .addOnSuccessListener {
                    Log.d("TF_NOTIFICATION", "Notification marked as read: $notificationId")
                    callback(true, "Marked as read")
                }
                .addOnFailureListener { e ->
                    Log.e("TF_NOTIFICATION", "Error marking notification as read: ${e.message}")
                    callback(false, "Error: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception marking as read: ${e.message}")
            callback(false, "Error: ${e.message}")
        }
    }

    override fun markAllAsRead(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            ref.orderByChild("receiverId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val updates = mutableMapOf<String, Any>()
                        for (child in snapshot.children) {
                            if (child.child("isRead").getValue(Boolean::class.java) == false) {
                                updates["${child.key}/isRead"] = true
                            }
                        }
                        if (updates.isNotEmpty()) {
                            ref.updateChildren(updates)
                                .addOnSuccessListener {
                                    Log.d("TF_NOTIFICATION", "All notifications marked as read for user: $userId")
                                    callback(true, "All marked as read")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TF_NOTIFICATION", "Error marking all as read: ${e.message}")
                                    callback(false, "Error: ${e.message}")
                                }
                        } else {
                            callback(true, "All marked as read")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TF_NOTIFICATION", "Error marking all as read: ${error.message}")
                        callback(false, "Error: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception marking all as read: ${e.message}")
            callback(false, "Error: ${e.message}")
        }
    }

    override fun getUnreadCount(
        userId: String,
        callback: (Boolean, String, Int) -> Unit
    ) {
        try {
            ref.orderByChild("receiverId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var count = 0
                        for (child in snapshot.children) {
                            val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                            if (!isRead) {
                                count++
                            }
                        }
                        callback(true, "Success", count)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TF_NOTIFICATION", "Error getting unread count: ${error.message}")
                        callback(false, "Error: ${error.message}", 0)
                    }
                })
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception getting unread count: ${e.message}")
            callback(false, "Error: ${e.message}", 0)
        }
    }

    override fun startListeningToUnreadCount(
        userId: String,
        callback: (Int) -> Unit
    ) {
        stopListeningToUnreadCount()

        try {
            val query = ref.orderByChild("receiverId").equalTo(userId)
            unreadCountQuery = query

            unreadCountListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    for (child in snapshot.children) {
                        val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                        if (!isRead) {
                            count++
                        }
                    }
                    callback(count)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TF_NOTIFICATION", "Error listening to unread count: ${error.message}")
                    callback(0)
                }
            }
            query.addValueEventListener(unreadCountListener!!)
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception starting unread count listener: ${e.message}")
            callback(0)
        }
    }

    override fun stopListeningToUnreadCount() {
        try {
            if (unreadCountListener != null && unreadCountQuery != null) {
                unreadCountQuery?.removeEventListener(unreadCountListener!!)
                unreadCountListener = null
                unreadCountQuery = null
            }
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception stopping unread count listener: ${e.message}")
        }
    }

    override fun getRequestById(
        requestId: String,
        callback: (Boolean, String, RequestModel?) -> Unit
    ) {
        try {
            requestsRef.child(requestId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val request = snapshot.getValue(RequestModel::class.java)
                        callback(true, "Success", request)
                    } else {
                        callback(false, "Request not found", null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TF_REQUEST", "Error getting request: ${e.message}")
                    callback(false, "Error: ${e.message}", null)
                }
        } catch (e: Exception) {
            Log.e("TF_REQUEST", "Exception getting request: ${e.message}")
            callback(false, "Error: ${e.message}", null)
        }
    }

    override fun updateRequestStatus(
        requestId: String,
        status: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            requestsRef.child(requestId).child("status").setValue(status)
                .addOnSuccessListener {
                    Log.d("TF_REQUEST", "Request status updated: $requestId -> $status")
                    callback(true, "Status updated")
                }
                .addOnFailureListener { e ->
                    Log.e("TF_REQUEST", "Error updating request status: ${e.message}")
                    callback(false, "Error: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("TF_REQUEST", "Exception updating request status: ${e.message}")
            callback(false, "Error: ${e.message}")
        }
    }

    fun updateRequestDetails(
        requestId: String,
        updates: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            requestsRef.child(requestId).updateChildren(updates)
                .addOnSuccessListener {
                    callback(true, "Request updated")
                }
                .addOnFailureListener { e ->
                    callback(false, "Error: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}")
        }
    }

    override fun getRequestsByOwner(
        ownerId: String,
        callback: (Boolean, String, List<RequestModel>?) -> Unit
    ) {
        try {
            requestsRef.orderByChild("ownerId").equalTo(ownerId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val requests = mutableListOf<RequestModel>()
                        for (child in snapshot.children) {
                            val request = child.getValue(RequestModel::class.java)
                            request?.let {
                                requests.add(it.copy(requestId = child.key ?: ""))
                            }
                        }
                        callback(true, "Success", requests.sortedByDescending { it.createdAt })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TF_REQUEST", "Error getting requests: ${error.message}")
                        callback(false, "Error: ${error.message}", null)
                    }
                })
        } catch (e: Exception) {
            Log.e("TF_REQUEST", "Exception getting requests: ${e.message}")
            callback(false, "Error: ${e.message}", null)
        }
    }

    override fun getRequestsByRequester(
        requesterId: String,
        callback: (Boolean, String, List<RequestModel>?) -> Unit
    ) {
        try {
            requestsRef.orderByChild("requesterId").equalTo(requesterId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val requests = mutableListOf<RequestModel>()
                        for (child in snapshot.children) {
                            val request = child.getValue(RequestModel::class.java)
                            request?.let {
                                requests.add(it.copy(requestId = child.key ?: ""))
                            }
                        }
                        callback(true, "Success", requests.sortedByDescending { it.createdAt })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TF_REQUEST", "Error getting requests: ${error.message}")
                        callback(false, "Error: ${error.message}", null)
                    }
                })
        } catch (e: Exception) {
            Log.e("TF_REQUEST", "Exception getting requests: ${e.message}")
            callback(false, "Error: ${e.message}", null)
        }
    }

    override fun updateNotificationStatus(
        notificationId: String,
        status: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            ref.child(notificationId).child("status").setValue(status)
                .addOnSuccessListener {
                    Log.d("TF_NOTIFICATION", "Notification status updated: $notificationId -> $status")
                    callback(true, "Status updated")
                }
                .addOnFailureListener { e ->
                    Log.e("TF_NOTIFICATION", "Error updating notification status: ${e.message}")
                    callback(false, "Error: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception updating notification status: ${e.message}")
            callback(false, "Error: ${e.message}")
        }
    }

    override fun deleteNotification(
        notificationId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            if (notificationId.isEmpty()) {
                callback(false, "Invalid notification ID")
                return
            }
            ref.child(notificationId).removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        callback(true, "Notification deleted successfully")
                    } else {
                        callback(false, it.exception?.message ?: "Failed to delete notification")
                    }
                }
        } catch (e: Exception) {
            Log.e("TF_NOTIFICATION", "Exception deleting notification: ${e.message}")
            callback(false, "Exception: ${e.message}")
        }
    }

    override fun deleteRequest(
        requestId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            requestsRef.child(requestId).removeValue()
                .addOnSuccessListener {
                    callback(true, "Request deleted")
                }
                .addOnFailureListener { e ->
                    callback(false, "Error: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}")
        }
    }

    override fun deleteRequestForRequester(
        requestId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            requestsRef.child(requestId).child("deletedByRequester").setValue(true)
                .addOnSuccessListener {
                    callback(true, "Request deleted from list")
                }
                .addOnFailureListener { e ->
                    callback(false, "Failed to delete: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}")
        }
    }

    override fun getLastAcceptedRequest(
        callback: (Boolean, String, RequestModel?) -> Unit
    ) {
        try {
            // Fetch recent requests to find the last ACCEPTED or CONFIRMED trade
            requestsRef.orderByKey().limitToLast(10)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.childrenCount > 0) {
                            // Children are returned in ascending order (oldest first)
                            // We reverse to check newest first
                            val children = snapshot.children.toList().reversed()
                            
                            val found = children.find { child ->
                                val status = child.child("status").getValue(String::class.java)
                                status == "ACCEPTED" || status == "CONFIRMED"
                            }

                            if (found != null) {
                                val request = found.getValue(RequestModel::class.java)
                                if (request != null) {
                                    callback(true, "Success", request.copy(requestId = found.key ?: ""))
                                } else {
                                    callback(false, "Failed to parse request", null)
                                }
                            } else {
                                callback(false, "No revertible trade found", null)
                            }
                        } else {
                            callback(false, "No requests found", null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(false, "Error: ${error.message}", null)
                    }
                })
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}", null)
        }
    }
}
