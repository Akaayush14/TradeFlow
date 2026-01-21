package com.example.tradeflow.repository

import com.example.tradeflow.model.NotificationModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationRepoImpl : NotificationRepo {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("notifications")
    private var unreadCountListener: ValueEventListener? = null

    override fun addNotification(
        notification: NotificationModel,
        callback: (Boolean, String) -> Unit
    ) {
        val notificationId = ref.push().key ?: ""
        notification.notificationId = notificationId

        ref.child(notificationId).setValue(notification.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Notification added successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to add notification")
            }
        }
    }

    override fun getAllNotifications(callback: (List<NotificationModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = mutableListOf<NotificationModel>()
                for (child in snapshot.children) {
                    val notification = child.getValue(NotificationModel::class.java)
                    notification?.let {
                        // Ensure all fields are read correctly
                        it.notificationId = child.key ?: ""
                        it.message = child.child("message").getValue(String::class.java) ?: ""
                        it.timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                        it.type = child.child("type").getValue(String::class.java) ?: ""
                        it.isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                        it.itemId = child.child("itemId").getValue(String::class.java) ?: ""
                        it.userId = child.child("userId").getValue(String::class.java) ?: ""
                        notifications.add(it)
                    }
                }
                // Sort by timestamp descending (newest first)
                callback(notifications.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun markAsRead(notificationId: String, callback: (Boolean) -> Unit) {
        ref.child(notificationId).child("isRead").setValue(true).addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }

    override fun markAllAsRead(callback: (Boolean) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = mutableMapOf<String, Any>()
                for (child in snapshot.children) {
                    updates["${child.key}/isRead"] = true
                }
                if (updates.isNotEmpty()) {
                    ref.updateChildren(updates).addOnCompleteListener {
                        callback(it.isSuccessful)
                    }
                } else {
                    callback(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    override fun getUnreadCount(callback: (Int) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
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
                callback(0)
            }
        })
    }

    override fun startListeningToUnreadCount(callback: (Int) -> Unit) {
        // Remove existing listener if any
        unreadCountListener?.let {
            ref.removeEventListener(it)
        }

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
                callback(0)
            }
        }
        ref.addValueEventListener(unreadCountListener!!)
    }

    override fun stopListeningToUnreadCount() {
        unreadCountListener?.let {
            ref.removeEventListener(it)
            unreadCountListener = null
        }
    }

    override fun deleteNotification(notificationId: String, callback: (Boolean, String) -> Unit) {
        if (notificationId.isEmpty()) {
            callback(false, "Invalid notification ID")
            return
        }
        ref.child(notificationId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Notification deleted successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to delete notification")
            }
        }
    }

    override fun deleteAllNotifications(callback: (Boolean, String) -> Unit) {
        ref.removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "All notifications deleted successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to delete all notifications")
            }
        }
    }
}