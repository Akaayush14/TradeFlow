package com.example.tradeflow.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.example.tradeflow.model.AdminModel
import com.example.tradeflow.repository.AdminRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(val repo: AdminRepo): ViewModel(){
    fun login(
        email:String, password:String,
        callback:(Boolean, String) -> Unit
    ){
        repo.login(email, password, callback)
    }

    fun register(
        email:String, password: String, phone:String,
        callback: (Boolean, String, String) -> Unit
    ){
        repo.register(email, password, phone, callback)
    }

    fun addAdminToDatabase(
        userId: String,
        model: AdminModel, callback: (Boolean, String) -> Unit
    ){
        repo.addAdminToDatabase(userId, model, callback)
    }

    fun forgetPassword(
        email:String,
        callback:(Boolean, String) -> Unit
    ){
        repo.forgetPassword(email, callback)
    }

    fun getCurrentUser() : FirebaseUser?{
        return repo.getCurrentUser()
    }

    private val _admin = MutableStateFlow<AdminModel?>(null)
    val admin: StateFlow<AdminModel?> = _admin.asStateFlow()

    private val _allAdmins = MutableStateFlow<List<AdminModel>?>(null)
    val allAdmins: StateFlow<List<AdminModel>?> = _allAdmins.asStateFlow()


    fun getAdminById(
        userId: String
    ){
        viewModelScope.launch {
            repo.getAdminById(userId) { success, msg, data ->
                if(success){
                    _admin.value = data
                }else{
                    _admin.value = null
                }
            }
        }
    }

    fun getAllAdmins()
    {
        viewModelScope.launch {
            repo.getAllAdmins { success, message, data ->
                if(success){
                    _allAdmins.value = data
                }else{
                    _allAdmins.value = emptyList()
                }
            }
        }
    }

    fun deleteAdmin(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteAdmin(userId, callback)
    }

    fun updateAdminStatus(
        userId: String,
        isBlocked: Boolean,
        isRestricted: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateAdminStatus(userId, isBlocked, isRestricted, callback)
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repo.uploadImage(context, imageUri, callback)
    }
}
