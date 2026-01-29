package com.example.tradeflow.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tradeflow.R
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.Transparent
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.ui.components.ThemeWrapper
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.repository.ProductRepoImpl
import com.google.firebase.auth.FirebaseAuth

class UserDashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
                DashboardPageBody()
            }
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

    // Check for intent extra to set initial tab
    val initialTab = activity.intent.getStringExtra("start_tab")
    val initialIndex = when (initialTab) {
        "inbox" -> 1
        "alert" -> 3
        else -> 0
    }

    var selectedIndex by remember { mutableStateOf(initialIndex) }
    var addItemMode by remember { mutableStateOf(AddItemMode.ADD) }
    var editingProduct by remember { mutableStateOf<ProductModel?>(null) }
    var showEditSuccess by remember { mutableStateOf(false) }

    var isNavigating by remember { mutableStateOf(false) }

    val viewModel = remember { UserNotificationViewModel(UserNotificationRepoImpl(), ProductRepoImpl()) }
    val unreadCount by viewModel.unreadCount.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    androidx.compose.runtime.DisposableEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.startListeningToUnreadCount(userId)
        }
        onDispose {
            viewModel.stopListeningToUnreadCount()
        }
    }

    val listItem = listOf(
        NavItem(label = "Explore", R.drawable.explore, R.drawable.explore_filled),
        NavItem(label = "Inbox", R.drawable.inbox, R.drawable.inbox_filled),
        NavItem(label = "Add", R.drawable.additem, R.drawable.additem_filled),
        NavItem(label = "Alert", R.drawable.notification, R.drawable.notification_filled),
        NavItem(label = "profile", R.drawable.profile, R.drawable.profile_filled),
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                listItem.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                selectedIndex = index
                                GlobalScope.launch {
                                    delay(300)
                                    isNavigating = false
                                }
                            }
                        },
                        icon = {
                            if (item.label == "Alert") {
                                BadgedNotificationIcon(
                                    unreadCount = unreadCount,
                                    iconPainter = painterResource(if (isSelected) item.iconFilled else item.iconOutlined),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    painter = painterResource(if (isSelected) item.iconFilled else item.iconOutlined),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        label = {
                            Text(
                                item.label,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
                3 -> UserNotificationScreen(
                    viewModel = viewModel,
                    onBackClick = { selectedIndex = 0 },
                    onMessageClick = { selectedIndex = 1 }
                )
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
@Composable
fun BadgedNotificationIcon(
    unreadCount: Int,
    iconPainter: Painter,
    contentDescription: String?,
    tint: Color
) {
    Box {
        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            tint = tint
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .offset(x = 12.dp, y = (-8).dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
