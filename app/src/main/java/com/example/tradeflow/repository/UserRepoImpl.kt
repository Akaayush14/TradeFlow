package com.example.tradeflow.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.tradeflow.model.UserModel
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.toMap
import android.content.Context
import android.net.Uri
import android.util.Log

class UserRepoImpl: UserRepo{
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    //table haru ma kaam garnu paro vane ref ma garnu paryo
    val ref: DatabaseReference = database.getReference("Users")

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, "login successful")
                }else{
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
                if (it.isSuccessful){
                    callback(true, "Registration success",
                        "${auth.currentUser?.uid}")
                }else{
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
            if (it.isSuccessful){
                callback(true, "Registration successful")
            }else{
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
    ) {
        ref.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
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

    override fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if(snapshot.exists()){
                        var allUsers = mutableListOf<UserModel>()
                        for (data in snapshot.children){
                            try {
                                var user = data.getValue(UserModel::class.java)
                                if(user != null){
                                    // Ensure userId is set from the snapshot key
                                    val userId = data.key ?: ""
                                    if (userId.isEmpty()) continue

                                    user.userId = userId

                                    // Ensure name and email are not null
                                    user.name = data.child("name").getValue(String::class.java) ?: ""
                                    user.email = data.child("email").getValue(String::class.java) ?: ""
                                    user.phone = data.child("phone").getValue(String::class.java) ?: ""

                                    // Read isBlocked value from Firebase, default to false if field doesn't exist
                                    if (data.hasChild("isBlocked")) {
                                        val isBlockedValue = data.child("isBlocked").getValue(Boolean::class.java)
                                        user.isBlocked = isBlockedValue ?: false
                                    } else {
                                        // Field doesn't exist in database, default to false
                                        user.isBlocked = false
                                    }
                                    // Read isRestricted value from Firebase, default to false if field doesn't exist
                                    if (data.hasChild("isRestricted")) {
                                        val isRestrictedValue = data.child("isRestricted").getValue(Boolean::class.java)
                                        user.isRestricted = isRestrictedValue ?: false
                                    } else {
                                        // Field doesn't exist in database, default to false
                                        user.isRestricted = false
                                    }

                                    // Only add user if userId is not empty
                                    if (user.userId.isNotEmpty()) {
                                        allUsers.add(user)
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip this user if there's an error reading it
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
        })
    }

    override fun deleteUser(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                callback(true, "User deleted successfully")
            }else{
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
            if(it.isSuccessful){
                val message = if(isBlocked) "User blocked successfully" else "User unblocked successfully"
                callback(true, message)
            }else{
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
            if(it.isSuccessful){
                val message = if(isRestricted) "User restricted successfully" else "User unrestricted successfully"
                callback(true, message)
            }else{
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

    //Adding/updating profile picture
    private val storageRef = FirebaseStorage.getInstance().reference
    override fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        userId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
        val profileImageRef = storageRef.child("profile_images/$fileName")

        profileImageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // Update user document with new image URL
                    updateProfileImageUrl(userId, imageUrl) { success, message ->
                        if (success) {
                            callback(true, imageUrl)
                        } else {
                            callback(false, null)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TF_PROFILE_UPLOAD", "Failed to upload profile image: ${e.message}")
                callback(false, null)
            }
    }

    override fun updateProfileImageUrl(
        userId: String,
        imageUrl: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).child("profileImageUrl").setValue(imageUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Profile image updated")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update profile image")
                }
            }
    }

}