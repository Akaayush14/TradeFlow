package com.example.tradeflow.theme

import androidx.compose.runtime.mutableStateOf
import com.example.tradeflow.repository.ThemeRepoImpl
import com.google.firebase.auth.FirebaseAuth

object ThemeManager {
    var themeMode = mutableStateOf("system")
        private set

    private var themeRepo = ThemeRepoImpl()
    private var lastUserId: String? = null
    private val auth = FirebaseAuth.getInstance()

    fun setThemeMode(mode: String) {
        themeMode.value = mode
    }

    fun loadTheme(userId: String) {
        if (userId.isEmpty()) {
            themeMode.value = "system"
            lastUserId = null
            return
        }

        themeRepo.getTheme(userId) { success, message, themeMode ->
            if (success && themeMode != null) {
                this.themeMode.value = themeMode

                lastUserId = userId

            } else {
                // If no theme found, use system default
                this.themeMode.value = "system"
                lastUserId = userId
            }
        }
    }

    fun saveTheme(userId: String, themeMode: String, callback: (Boolean, String) -> Unit) {
        this.themeMode.value = themeMode
        lastUserId = userId

        themeRepo.saveTheme(userId, themeMode) { success, message ->
            callback(success, message)
        }
    }

    fun clear() {
        lastUserId = null
        themeMode.value = "system"
    }

    fun shouldReloadTheme(userId: String): Boolean {
        return lastUserId != userId
    }
}