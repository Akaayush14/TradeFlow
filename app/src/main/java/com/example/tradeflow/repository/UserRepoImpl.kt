package com.example.tradeflow.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.tradeflow.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ServerValue
import java.io.InputStream
import java.util.concurrent.Executors

class UserRepoImpl: UserRepo {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Users")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dpi7b9iam",
            "api_key" to "561879326562495",
            "api_secret" to "iteXJaLRqFgpuMwmVcw0gw9fjgE"
        )
    )

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "login successful")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        phone: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(
                        true, "Registration success",
                        "${auth.currentUser?.uid}"
                    )
                } else {
                    callback(false, "${it.exception?.message}", "")
                }
            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Registration successful")
            } else {
                callback(true, "${it.exception?.message}")
            }
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Link sent to $email")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        user.userId = userId
                        user.name = snapshot.child("name").getValue(String::class.java) ?: ""
                        user.email = snapshot.child("email").getValue(String::class.java) ?: ""
                        user.phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                        user.location = snapshot.child("location").getValue(String::class.java) ?: ""
                        user.gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                        user.dob = snapshot.child("dob").getValue(String::class.java) ?: ""

                        if (snapshot.hasChild("isBlocked")) {
                            user.isBlocked = snapshot.child("isBlocked").getValue(Boolean::class.java) ?: false
                        } else {
                            user.isBlocked = false
                        }

                        if (snapshot.hasChild("isRestricted")) {
                            user.isRestricted = snapshot.child("isRestricted").getValue(Boolean::class.java) ?: false
                        } else {
                            user.isRestricted = false
                        }

                        user.points = snapshot.child("points").getValue(Long::class.java) ?: 0L
                        user.profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                        user.isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                        user.lastActive = snapshot.child("lastActive").getValue(Long::class.java) ?: 0L

                        callback(true, "Profile Fetched", user)
                    } else {
                        callback(false, "User data is null", null)
                    }
                } else {
                    callback(false, "User not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        }
        ref.child(userId).addValueEventListener(listener)
        return listener
    }

    override fun getUserByIdSingle(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        callback(true, "Profile Fetched", user)
                    } else {
                        callback(false, "User data is null", null)
                    }
                } else {
                    callback(false, "User not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        var allUsers = mutableListOf<UserModel>()
                        for (data in snapshot.children) {
                            try {
                                var user = data.getValue(UserModel::class.java)
                                if (user != null) {
                                    val userId = data.key ?: ""
                                    if (userId.isEmpty()) continue

                                    user.userId = userId
                                    user.name = data.child("name").getValue(String::class.java) ?: ""
                                    user.email = data.child("email").getValue(String::class.java) ?: ""
                                    user.phone = data.child("phone").getValue(String::class.java) ?: ""
                                    user.location = data.child("location").getValue(String::class.java) ?: ""
                                    user.gender = data.child("gender").getValue(String::class.java) ?: ""
                                    user.dob = data.child("dob").getValue(String::class.java) ?: ""
                                    user.points = data.child("points").getValue(Long::class.java) ?: 0L
                                    user.profileImageUrl = data.child("profileImageUrl").getValue(String::class.java) ?: ""
                                    user.isOnline = data.child("isOnline").getValue(Boolean::class.java) ?: false
                                    user.lastActive = data.child("lastActive").getValue(Long::class.java) ?: 0L

                                    if (data.hasChild("isBlocked")) {
                                        val isBlockedValue = data.child("isBlocked").getValue(Boolean::class.java)
                                        user.isBlocked = isBlockedValue ?: false
                                    } else {
                                        user.isBlocked = false
                                    }

                                    if (data.hasChild("isRestricted")) {
                                        val isRestrictedValue = data.child("isRestricted").getValue(Boolean::class.java)
                                        user.isRestricted = isRestrictedValue ?: false
                                    } else {
                                        user.isRestricted = false
                                    }
                                    if (user.userId.isNotEmpty()) {
                                        allUsers.add(user)
                                    }
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }
                        callback(true, "User fetched", allUsers)
                    } else {
                        callback(true, "No users found", emptyList())
                    }
                } catch (e: Exception) {
                    callback(false, "Error: ${e.message}", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message ?: "Unknown error", null)
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    override fun removeUserListener(listener: ValueEventListener) {
        ref.removeEventListener(listener)
    }

    override fun deleteUser(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "User deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun blockUser(
        userId: String,
        isBlocked: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).child("isBlocked").setValue(isBlocked).addOnCompleteListener {
            if (it.isSuccessful) {
                val message =
                    if (isBlocked) "User blocked successfully" else "User unblocked successfully"
                callback(true, message)
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun restrictUser(
        userId: String,
        isRestricted: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).child("isRestricted").setValue(isRestricted).addOnCompleteListener {
            if (it.isSuccessful) {
                val message =
                    if (isRestricted) "User restricted successfully" else "User unrestricted successfully"
                callback(true, message)
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateUserPoints(
        userId: String,
        pointsToAdd: Long,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).child("points").get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.getValue(Long::class.java) ?: 0L
            val newPoints = currentPoints + pointsToAdd

            ref.child(userId).child("points").setValue(newPoints).addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Points updated successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
        }.addOnFailureListener {
            // If points field doesn't exist, create it
            ref.child(userId).child("points").setValue(pointsToAdd).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Points updated successfully")
                } else {
                    callback(false, "${task.exception?.message}")
                }
            }
        }
    }

    override fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Profile updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Update failed")
                }
            }
    }

    override fun resetAllUserPoints(callback: (Boolean, String) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val updates = mutableMapOf<String, Any>()
                    for (child in snapshot.children) {
                        val userId = child.key
                        if (userId != null) {
                            updates["$userId/points"] = 0L
                        }
                    }
                    if (updates.isNotEmpty()) {
                        ref.updateChildren(updates).addOnCompleteListener {
                            if (it.isSuccessful) {
                                callback(true, "All user points reset to 0")
                            } else {
                                callback(false, it.exception?.message ?: "Failed to reset points")
                            }
                        }
                    } else {
                        callback(true, "No users to update")
                    }
                } else {
                    callback(true, "No users found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                // Create unique name for profile image
                fileName = fileName?.substringBeforeLast(".") ?: "profile"
                fileName = "${fileName}_${System.currentTimeMillis()}"

                Log.d("TF_CLOUDINARY_UPLOAD", "Uploading to Cloudinary: $fileName")

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?
                imageUrl = imageUrl?.replace("http://", "https://")

                Log.d("TF_CLOUDINARY_UPLOAD", "Upload successful: $imageUrl")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                Log.e("TF_CLOUDINARY_UPLOAD", "Upload failed: ${e.message}")
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    override fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "No user logged in")
            return
        }

        val email = user.email
        if (email.isNullOrEmpty()) {
            callback(false, "User email not found")
            return
        }

        // Step 1: Re-authenticate the user with current password
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Step 2: Update to new password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                callback(true, "Password changed successfully")
                            } else {
                                callback(
                                    false,
                                    "Failed to update password: ${updateTask.exception?.message}"
                                )
                            }
                        }
                } else {
                    callback(false, "Current password is incorrect")
                }
            }
    }

    override fun setupUserPresence() {
        val userId = auth.currentUser?.uid ?: return
        val connectedRef = database.getReference(".info/connected")
        val userStatusRef = ref.child(userId)

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    val onlineState = mapOf(
                        "isOnline" to true,
                        "lastActive" to ServerValue.TIMESTAMP
                    )
                    userStatusRef.updateChildren(onlineState)

                    val offlineState = mapOf(
                        "isOnline" to false,
                        "lastActive" to ServerValue.TIMESTAMP
                    )
                    userStatusRef.onDisconnect().updateChildren(offlineState)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}