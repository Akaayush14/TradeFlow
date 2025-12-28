package com.example.classwork.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.UserRepo

class UserViewModel(val repo: UserRepo): ViewModel(){
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

    fun addUserToDatabase(
        userId: String,
        model: UserModel, callback: (Boolean, String) -> Unit
    ){
        repo.addUserToDatabase(userId, model, callback)
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

    private val _users = MutableLiveData<UserModel?>()
    val users: MutableLiveData<UserModel?> get() = _users

    private val _allUsers = MutableLiveData<List<UserModel>?>()


    fun getUserById(
        userId: String
    ){
        repo.getUserById(userId){
                success,msg,data->
            if(success){
                _users.postValue(data)
            }else{
                _users.postValue(null)
            }
        }

    }

    fun getAllUser()
    {
        repo.getAllUser {
                success, message, data ->
            if(success){
                _allUsers.postValue(data)
            }else{
                _allUsers.postValue(emptyList())
            }
        }

    }
}