package com.example.tradeflow.repository

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

}