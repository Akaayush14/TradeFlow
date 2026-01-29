package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tradeflow.repository.ChatRepoImpl
import com.example.tradeflow.repository.UserRepoImpl

class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(UserRepoImpl()) as T
        }
        if (modelClass.isAssignableFrom(ChatSystemViewModel::class.java)) {
            return ChatSystemViewModel(ChatRepoImpl()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}