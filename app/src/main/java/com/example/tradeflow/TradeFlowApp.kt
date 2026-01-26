package com.example.tradeflow

import android.app.Application
import com.example.tradeflow.repository.NotificationRepoImpl
import com.example.tradeflow.utils.NotificationHelper

class TradeFlowApp : Application() {

    companion object {
        lateinit var notificationHelper: NotificationHelper
            private set
    }

    override fun onCreate() {
        super.onCreate()
        val notificationRepo = NotificationRepoImpl()
        notificationHelper = NotificationHelper(notificationRepo)
    }
}