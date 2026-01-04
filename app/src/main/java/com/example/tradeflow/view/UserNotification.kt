package com.example.tradeflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.tradeflow.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit = {}) {


    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    Text(
                        "Notification",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                },
                onBackClick = onBackClick
            )
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = White)
        ) {
            Text("Notification Screen")
        }
    }
}