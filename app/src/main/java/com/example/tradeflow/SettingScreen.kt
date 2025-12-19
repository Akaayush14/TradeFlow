package com.example.tradeflow


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00897B))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Settings",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3))
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Lucas Scott", style = MaterialTheme.typography.titleMedium)
            Text(text = "@lucasscott3", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsItem("Notifications")
        SettingsItem("Edit Profile")
        SettingsItem("About us")
        SettingsItem("Privacy & Security")
        SettingsItem("Logout")
    }
}

@Composable
fun SettingsItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                println("$title clicked")
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
    Divider()
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SettingsScreen()
}
