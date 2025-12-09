package com.example.tradeflow

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tradeflow.ui.theme.Blue
import com.example.tradeflow.ui.theme.PurpleGrey80
import com.example.tradeflow.ui.theme.TealBlue
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
    var searchText by remember { mutableStateOf("") }



    data class NavItem(val label: String, val icon: Int)

    var selectedIndex by remember { mutableStateOf(0) }

    val listItem = listOf(
        NavItem(label = "Explore", icon = R.drawable.explore),
        NavItem(label = "Inbox", icon = R.drawable.inbox),
        NavItem(label = "AddItem", icon = R.drawable.additem),
        NavItem(label = "Notice", icon = R.drawable.notification),
        NavItem(label = "Profile", icon = R.drawable.profile),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = White,
                    actionIconContentColor = White,
                    containerColor =TealBlue,
                    navigationIconContentColor = White
                ),
//

                title = {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = {
                            Row (verticalAlignment = Alignment.CenterVertically) {
                                Icon(

                                    painter = painterResource(R.drawable.search),
                                    contentDescription = null,

                                    modifier = Modifier.size(20.dp),
                                    tint = Color. Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Search Anythings", color = Color.Gray)
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp)) // Rounded corners
                    )
                },

                    navigationIcon = {

                    },


                        actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_menu_24),
                            contentDescription = null
                        )
                    }

                }
            )
        },
        bottomBar = {
            NavigationBar {
                listItem.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(painter = painterResource(item.icon), contentDescription = null)
                        },
                        label = { Text(item.label) },
                        onClick = {
                            selectedIndex = index
                        },
                        selected = selectedIndex == index

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
                1 -> InboxScreen()
                2 -> AddItemScreen()
                3-> NotificationScreen()
                4 -> ProfileScreen()
                else -> ExploreScreen()

            }

        }
    }
}
