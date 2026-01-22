package com.example.tradeflow.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// DATA MODEL
data class NotificationItem(val id: Int, val sender: String, val message: String, val time: String, val type: NotificationType)
enum class NotificationType { ALL, UNREAD, MENTION }

// SAMPLE DATA
val sampleNotifications = listOf(
    NotificationItem(1, "Wade Warren", "added new lead Brooklyn Simmons", "12 min ago", NotificationType.ALL),
    NotificationItem(2, "Esther Howard", "added new lead Leslie Alexander", "12 min ago", NotificationType.UNREAD),
    NotificationItem(3, "Jenny Wilson", "We have scheduled a meeting for next week.", "10 min ago", NotificationType.MENTION),
    NotificationItem(4, "Emily", "Copies of Government.pdf", "5 min ago", NotificationType.ALL),
    NotificationItem(5, "Robert Fox", "Please ensure feedback is constructive.", "2 min ago", NotificationType.UNREAD)
)

class UserSettingNotification : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            UserSettingNotificationScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingNotificationScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Unread", "Mentions")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },


                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF007D70)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            val filteredNotifications = sampleNotifications.filter {
                when (selectedTab) {
                    0 -> true
                    1 -> it.type == NotificationType.UNREAD
                    2 -> it.type == NotificationType.MENTION
                    else -> true
                }
            }
            LazyColumn {
                items(filteredNotifications) { item ->
                    when (item.type) {
                        NotificationType.ALL, NotificationType.UNREAD -> SimpleNotification(item.sender, item.message, item.time)
                        NotificationType.MENTION -> ReplyNotification(item.sender, item.message, Color(0xFFA8F0C6))
                    }
                }
            }
        }
    }
}

// UI COMPONENTS
@Composable
fun SimpleNotification(name: String, message: String, time: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Avatar()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("$name $message", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReplyNotification(name: String, message: String, bgColor: Color) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            Avatar()
            Spacer(modifier = Modifier.width(12.dp))
            Text("$name sent you reply", fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = {}) { Text("Reply") }
        }
    }
}

@Composable
fun Avatar() {
    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsPreview() {
    val navController = rememberNavController()
    UserSettingNotificationScreen(navController)
}