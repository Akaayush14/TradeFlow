package com.example.tradeflow.repository

import com.example.tradeflow.model.AdminModel
import com.google.firebase.auth.FirebaseUser

interface AdminRepo {
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

    fun addAdminToDatabase(
        userId: String,
        model: AdminModel,
        callback: (Boolean, String) -> Unit
    )

    fun forgetPassword(
        email:String,
        callback:(Boolean, String) -> Unit
    )

    fun getCurrentUser() : FirebaseUser?

    fun getAdminById(
        userId: String,
        callback:(Boolean, String, AdminModel?) -> Unit
    )

    fun getAllAdmins(callback: (Boolean, String, List<AdminModel>?) -> Unit)

    fun deleteAdmin(
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun updateAdminStatus(
        userId: String,
        isBlocked: Boolean,
        isRestricted: Boolean,
        callback: (Boolean, String) -> Unit
    )
}
