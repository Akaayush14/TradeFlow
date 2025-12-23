package com.example.tradeflow

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.tradeflow.ui.theme.TealBlue
import com.example.tradeflow.ui.theme.Transparent
import com.example.tradeflow.ui.theme.White

class DashboardPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardPageBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardPageBody() {
    val context = LocalContext.current
    val activity = context as Activity

    data class NavItem(val label: String, val iconOutlined: Int, val iconFilled: Int)

    var selectedIndex by remember { mutableStateOf(0) }

    val listItem = listOf(
        NavItem(label = "Explore", R.drawable.explore, R.drawable.explore_filled),
        NavItem(label = "Inbox", R.drawable.inbox, R.drawable.inbox_filled),
        NavItem(label = "AddItem", R.drawable.additem, R.drawable.additem_filled),
        NavItem(label = "Notice", R.drawable.notification, R.drawable.notification_filled),
        NavItem(label = "profile", R.drawable.profile, R.drawable.profile_filled),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = TealBlue
            ) {
                listItem.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                painter = painterResource(if (isSelected) item.iconFilled else item.iconOutlined),
                                contentDescription = null,
                                tint = White
                            )
                        },
                        label = { Text(item.label, color = White) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = White,
                            unselectedIconColor = White,
                            selectedTextColor = White,
                            unselectedTextColor = White,
                            indicatorColor = Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedIndex) {
                0 -> ExploreScreen()
                1 -> InboxScreen(onBackClick = { selectedIndex = 0 })
                2 -> AddItemScreen(onBackClick = { selectedIndex = 0 })
                3 -> NotificationScreen(onBackClick = { selectedIndex = 0 })
                4 -> ProfileScreen(onBackClick = { selectedIndex = 0 })
                else -> ExploreScreen()
            }
        }
    }
}