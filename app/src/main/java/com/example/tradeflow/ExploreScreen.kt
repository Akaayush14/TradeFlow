package com.example.tradeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tradeflow.ui.theme.White

@Composable
fun ExploreScreen() {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = White)
    ) {
        Text("Explore Screen")
    }
}