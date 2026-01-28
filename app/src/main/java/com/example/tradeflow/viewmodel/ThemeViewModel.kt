package com.example.tradeflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeflow.repository.ThemeRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeRepo: ThemeRepo) : ViewModel() {
    private val _currentThemeMode = MutableStateFlow("system")
    val currentThemeMode: StateFlow<String> = _currentThemeMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var cachedUserId: String? = null
    private var cachedThemeMode: String? = null

    fun saveTheme(userId: String, themeMode: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _currentThemeMode.value = themeMode
            cachedUserId = userId
            cachedThemeMode = themeMode
            _errorMessage.value = null
            _isLoading.value = true

            themeRepo.saveTheme(userId, themeMode) { success, message ->
                _isLoading.value = false
                if (!success) {
                    _errorMessage.value = "Theme saved locally. Sync error: $message"
                }
                callback(success, message)
            }
        }
    }

    fun saveThemeInBackground(userId: String, themeMode: String) {
        viewModelScope.launch {
            _currentThemeMode.value = themeMode
            cachedUserId = userId
            cachedThemeMode = themeMode
            themeRepo.saveTheme(userId, themeMode) { _, _ -> }
        }
    }

    fun loadTheme(userId: String) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch

            // Check cache first
            if (cachedUserId == userId && cachedThemeMode != null) {
                _currentThemeMode.value = cachedThemeMode!!
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            themeRepo.getTheme(userId) { success, message, themeMode ->
                _isLoading.value = false
                if (success && themeMode != null) {
                    _currentThemeMode.value = themeMode
                    cachedUserId = userId
                    cachedThemeMode = themeMode
                } else {
                    _errorMessage.value = message
                }
            }
        }
    }

    fun setThemeMode(themeMode: String) {
        _currentThemeMode.value = themeMode
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearCache() {
        cachedUserId = null
        cachedThemeMode = null
        _currentThemeMode.value = "system"
    }
}