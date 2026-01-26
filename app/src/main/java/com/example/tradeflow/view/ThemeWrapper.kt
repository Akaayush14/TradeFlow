package com.example.tradeflow.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.tradeflow.theme.ThemeManager
import com.example.tradeflow.ui.theme.TradeFlowTheme
import com.example.tradeflow.ui.theme.SetStatusBarColors
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun ThemeWrapper(
    content: @Composable () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    val currentThemeMode by remember { ThemeManager.themeMode }

    var previousUserId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            if (previousUserId != userId) {
                ThemeManager.loadTheme(userId)
                previousUserId = userId
            }
        } else {
            ThemeManager.clear()
            previousUserId = null
        }
    }

    val useDarkTheme = when (currentThemeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    TradeFlowTheme(
        themeMode = currentThemeMode,
        darkTheme = useDarkTheme,
        dynamicColor = false
    ) {
        SetStatusBarColors(useDarkTheme)
        content()
    }
}