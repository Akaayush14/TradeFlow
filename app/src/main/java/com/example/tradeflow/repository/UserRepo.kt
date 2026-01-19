package com.example.tradeflow.repository

import android.content.Context
import android.net.Uri
import com.example.tradeflow.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepo {
    fun login(
        email:String,
        password:String,
        callback:(Boolean, String) -> Unit
    )

    fun register(
        email:String,
        password:String,
        phone:String,
        callback:(Boolean, String, String) -> Unit
    )

    fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    )

    fun forgetPassword(
        email:String,
        callback:(Boolean, String) -> Unit
    )

    fun getCurrentUser() : FirebaseUser?

    fun getUserById(
        userId: String,
        callback:(Boolean, String, UserModel?) -> Unit
    )

    fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit)

    fun deleteUser(
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun blockUser(
        userId: String,
        isBlocked: Boolean,
        callback: (Boolean, String) -> Unit
    )

    fun restrictUser(
        userId: String,
        isRestricted: Boolean,
        callback: (Boolean, String) -> Unit
    )

    fun updateUserPoints(
        userId: String,
        pointsToAdd: Long,
        callback: (Boolean, String) -> Unit
    )

    fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    )

    fun uploadImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )
}