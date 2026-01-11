package com.example.tradeflow.view

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
import com.example.tradeflow.R
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.Transparent
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.model.ProductModel

class UserDashboard : ComponentActivity() {
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
fun TradeFlowTopBar(
    title: @Composable () -> Unit,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit) = {}
) {
    androidx.compose.material3.CenterAlignedTopAppBar(
        title = title,
        navigationIcon = {
            if (onBackClick != null) {
                androidx.compose.material3.IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_new_24),
                        contentDescription = "Back",
                        tint = com.example.tradeflow.ui.theme.White
                    )
                }
            }
        },
        actions = actions,
        colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = com.example.tradeflow.ui.theme.Greenish,
            titleContentColor = com.example.tradeflow.ui.theme.White,
            navigationIconContentColor = com.example.tradeflow.ui.theme.White,
            actionIconContentColor = com.example.tradeflow.ui.theme.White
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardPageBody() {
    val context = LocalContext.current
    val activity = context as Activity

    data class NavItem(val label: String, val iconOutlined: Int, val iconFilled: Int)

    var selectedIndex by remember { mutableStateOf(0) }
    var addItemMode by remember { mutableStateOf(AddItemMode.ADD) }
    var editingProduct by remember { mutableStateOf<ProductModel?>(null) }
    var showEditSuccess by remember { mutableStateOf(false) }

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
                containerColor = Greenish
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
                0 -> UserExploreScreen()
                1 -> UserInboxScreen(onBackClick = { selectedIndex = 0 })
                2 -> UserAddItemScreen(
                    mode = addItemMode,
                    initialProduct = editingProduct,
                    onBackClick = {
                        selectedIndex = if (addItemMode == AddItemMode.EDIT) 4 else 0
                        addItemMode = AddItemMode.ADD
                        editingProduct = null
                    },
                    onSaved = {
                        selectedIndex = 4
                        addItemMode = AddItemMode.ADD
                        editingProduct = null
                        showEditSuccess = true
                    }
                )
                3 -> UserNotificationScreen(onBackClick = { selectedIndex = 0 })
                4 -> UserProfileScreen(
                    onBackClick = { selectedIndex = 0 },
                    onEditProduct = { product ->
                        addItemMode = AddItemMode.EDIT
                        editingProduct = product
                        selectedIndex = 2
                    },
                    showEditSuccess = showEditSuccess,
                    onSnackbarShown = { showEditSuccess = false }
                )
                else -> UserExploreScreen()
            }
        }
    }
}

