package com.example.tradeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tradeflow.ui.theme.TealBlue
import com.example.tradeflow.ui.theme.White
@Composable
fun ExploreScreen() {

    var selectedTab by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = TealBlue,
                    shape = RoundedCornerShape(12.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("All","Rent","Trade")
            tabs.forEach { tabName ->
                val isSelected = selectedTab == tabName
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(if (isSelected) White else Color(0xFFF0F0F0))
                        .clickable { selectedTab = tabName },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        color = if (isSelected) Color.Black else Color.Gray
                    )
                }
            }
        }

        Text(
            text = "Content for the $selectedTab tab goes here.",
            modifier = Modifier.padding(16.dp)
        )
    }
}
