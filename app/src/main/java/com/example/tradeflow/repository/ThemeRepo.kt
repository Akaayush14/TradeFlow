package com.example.tradeflow.repository

interface ThemeRepo {
    fun saveTheme(userId: String, themeMode: String, callback: (Boolean, String) -> Unit)
    fun getTheme(userId: String, callback: (Boolean, String, String?) -> Unit)
}