package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.UserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel( val repo: UserRepo): ViewModel(){
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

    private val _users = MutableStateFlow<UserModel?>(null)
    val users: StateFlow<UserModel?> = _users.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserModel>?>(null)
    val allUsers: StateFlow<List<UserModel>?> = _allUsers.asStateFlow()


    fun getUserById(
        userId: String
    ){
        viewModelScope.launch {
            repo.getUserById(userId) { success, msg, data ->
                if(success){
                    _users.value = data
                }else{
                    _users.value = null
                }
            }
        }
    }

    fun getAllUser()
    {
        viewModelScope.launch {
            repo.getAllUser { success, message, data ->
                if(success){
                    _allUsers.value = data
                }else{
                    _allUsers.value = emptyList()
                }
            }
        }
    }

    fun deleteUser(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteUser(userId, callback)
    }

    fun blockUser(
        userId: String,
        isBlocked: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        repo.blockUser(userId, isBlocked) { success, message ->
            if (success) {
                // Manually update the StateFlow immediately for instant UI feedback
                viewModelScope.launch {
                    val currentList = _allUsers.value
                    if (currentList != null && currentList.isNotEmpty()) {
                        // Find the user index
                        val userIndex = currentList.indexOfFirst { it.userId == userId }
                        if (userIndex != -1) {
                            // Create a mutable copy of the list
                            val updatedList = currentList.toMutableList()
                            // Update only the specific user
                            val userToUpdate = currentList[userIndex]
                            updatedList[userIndex] = UserModel(
                                userId = userToUpdate.userId,
                                name = userToUpdate.name,
                                email = userToUpdate.email,
                                phone = userToUpdate.phone,
                                isBlocked = isBlocked,
                                isRestricted = userToUpdate.isRestricted
                            )
                            // Update StateFlow with new list
                            _allUsers.value = updatedList.toList()
                        }
                    } else {
                        // If list is null or empty, refresh from database
                        getAllUser()
                    }
                }
            }
            callback(success, message)
        }
    }

    fun restrictUser(
        userId: String,
        isRestricted: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        repo.restrictUser(userId, isRestricted) { success, message ->
            if (success) {
                // Manually update the StateFlow immediately for instant UI feedback
                viewModelScope.launch {
                    val currentList = _allUsers.value
                    if (currentList != null && currentList.isNotEmpty()) {
                        // Find the user index
                        val userIndex = currentList.indexOfFirst { it.userId == userId }
                        if (userIndex != -1) {
                            // Create a mutable copy of the list
                            val updatedList = currentList.toMutableList()
                            // Update only the specific user
                            val userToUpdate = currentList[userIndex]
                            updatedList[userIndex] = UserModel(
                                userId = userToUpdate.userId,
                                name = userToUpdate.name,
                                email = userToUpdate.email,
                                phone = userToUpdate.phone,
                                isBlocked = userToUpdate.isBlocked,
                                isRestricted = isRestricted
                            )
                            // Update StateFlow with new list
                            _allUsers.value = updatedList.toList()
                        }
                    } else {
                        // If list is null or empty, refresh from database
                        getAllUser()
                    }
                }
            }
            callback(success, message)
        }
    }

    fun updateUserPoints(
        userId: String,
        pointsToAdd: Long,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateUserPoints(userId, pointsToAdd, callback)
    }

    fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateUserProfile(userId, updates, callback)
    }
    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors.asStateFlow()

    fun clearValidationErrors() {
        _validationErrors.value = emptyMap()
    }

    fun setValidationError(field: String, message: String) {
        _validationErrors.value = _validationErrors.value + (field to message)
    }
}