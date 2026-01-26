package com.example.tradeflow.view
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
 
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.animate
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.StrokeCap

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import com.example.tradeflow.R
import com.example.tradeflow.model.NotificationModel
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.AdminRepoImpl
import com.example.tradeflow.repository.NotificationRepoImpl
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.AdminViewModel
import com.example.tradeflow.viewmodel.NotificationViewModel
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserViewModel

class AdminDashExp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminExp()
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminExp() {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(0) }
    var userTab by remember { mutableStateOf(0) }
    var itemTab by remember { mutableStateOf(0) }
    var backPressedTime by remember { mutableLongStateOf(0L) }
    
    // Notification view model for unread count
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        notificationViewModel.getUnreadCount()
    }

    // Handle back button press
    BackHandler {
        if (selectedIndex != 0) {
            selectedIndex = 0
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                if (context is ComponentActivity) {
                    context.finishAffinity()
                }
            } else {
                backPressedTime = currentTime
                Toast.makeText(context, "Click again to quit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Greenish) {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_explore),
                            contentDescription = "Explore",
                            tint = Color.White
                        )
                    },
                    label = { Text("Explore", color = Color.White) }
                )

                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = { selectedIndex = 1 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_user),
                            contentDescription = "User",
                            tint = Color.White
                        )
                    },
                    label = { Text("User", color = Color.White) }
                )
                
                NavigationBarItem(
                    selected = selectedIndex == 3,
                    onClick = { selectedIndex = 3 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_items),
                            contentDescription = "Items",
                            tint = Color.White
                        )
                    },
                    label = { Text("Items", color = Color.White) }
                )
                
                NavigationBarItem(
                    selected = selectedIndex == 4,
                    onClick = { selectedIndex = 4 },
                    icon = {
                        BadgedNotificationIconExp(
                            unreadCount = unreadCount,
                            iconPainter = painterResource(R.drawable.notification_filled),
                            contentDescription = "notification"
                        )
                    },
                    label = { Text("notification", color = Color.White) }
                )

                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = { selectedIndex = 2 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_profile),
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    },
                    label = { Text("Profile", color = Color.White) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            when (selectedIndex) {
                0 -> AdminExploreScreen(
                    onNavigateToUser = { tab ->
                        userTab = tab
                        selectedIndex = 1
                    },
                    onNavigateToItem = { tab ->
                        itemTab = tab
                        selectedIndex = 3
                    }
                )
                1 -> AdminUserScreen(initialTab = userTab, onBackClick = { selectedIndex = 0 })
                2 -> AdminProfileScreen(onBackClick = { selectedIndex = 0 })
                3 -> AdminItemScreen(initialTab = itemTab, onBackClick = { selectedIndex = 0 })
                4 -> AdminNotificationScreen(onBackClick = { selectedIndex = 0 })
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminExploreScreen(
    onNavigateToUser: (Int) -> Unit,
    onNavigateToItem: (Int) -> Unit
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(1) } // 0 for User, 1 for Metrics, 2 for Items
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    // Hide search bar on metrics tab
                    if (selectedTab != 1) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_search),
                                    contentDescription = "Search",
                                    modifier = Modifier.size(22.dp),
                                    tint = Greenish
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Dashboard",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AdminSettings::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPasswordDialog = true },
                containerColor = Greenish,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Add Admin")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tab Row for Metrics, Items and User
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Gray,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DarkGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "User",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 0) Color.Black else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Metrics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 1) Color.Black else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Items",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 2) Color.Black else Color.Gray
                        )
                    }
                )
            }

            // Content based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> UsersContent(searchText = searchText)
                    1 -> MetricsContent(
                        onRequireAdminAccess = { showPasswordDialog = true },
                        onNavigateToUser = onNavigateToUser,
                        onNavigateToItem = onNavigateToItem
                    )
                    2 -> ItemsContent(searchText = searchText)
                }
            }
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                passwordInput = ""
            },
            title = { Text("Admin Access") },
            text = {
                Column {
                    Text("Enter password to access Admin controls:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordInput == "1234") {
                            showPasswordDialog = false
                            passwordInput = ""
                            val intent = Intent(context, AdminAdmin::class.java)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Greenish)
                ) {
                    Text("Enter", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showPasswordDialog = false
                        passwordInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun MetricsContent(
    onRequireAdminAccess: () -> Unit,
    onNavigateToUser: (Int) -> Unit,
    onNavigateToItem: (Int) -> Unit
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val adminViewModel = remember { AdminViewModel(AdminRepoImpl()) }

    val allUsers by userViewModel.allUsers.collectAsState()
    val allProducts by productViewModel.allProducts.collectAsState()
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val allAdmins by adminViewModel.allAdmins.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
        productViewModel.getAllProduct()
        notificationViewModel.getAllNotifications()
        notificationViewModel.getUnreadCount()
        adminViewModel.getAllAdmins()
    }

    // User Metrics
    val totalUsers = allUsers?.size ?: 0
    val blockedUsers = allUsers?.count { it.isBlocked } ?: 0
    val restrictedUsers = allUsers?.count { it.isRestricted } ?: 0
    val normalUsers = totalUsers - blockedUsers - restrictedUsers

    // Admin Metrics
    val totalAdmins = allAdmins?.size ?: 0
    val blockedAdmins = allAdmins?.count { it.isBlocked } ?: 0
    val restrictedAdmins = allAdmins?.count { it.isRestricted } ?: 0

    // Product Metrics
    val totalProducts = allProducts?.size ?: 0
    val listedProducts = allProducts?.count { it.isListed } ?: 0
    val unlistedProducts = allProducts?.count { !it.isListed } ?: 0
    val avgPrice = if (totalProducts > 0) {
        val totalPrice = allProducts?.sumOf { it.price } ?: 0.0
        totalPrice / totalProducts
    } else {
        0.0
    }

    // Notification Metrics
    val totalNotifications = notifications?.size ?: 0

    val hasInternet = isInternetAvailableExp(context)
    val scrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                userViewModel.getAllUser()
                productViewModel.getAllProduct()
                notificationViewModel.getAllNotifications()
                notificationViewModel.getUnreadCount()
                adminViewModel.getAllAdmins()
                delay(1000) // Simulate delay for better UI
                isRefreshing = false
            }
        }
    ) {
        if (!hasInternet) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_internet),
                    contentDescription = null
                )
            }
        } else if (totalUsers == 0 && totalAdmins == 0 && totalProducts == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_data),
                    contentDescription = null
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Metrics Section
                Text(
                    text = "User Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Users",
                        value = "$totalUsers",
                        icon = painterResource(R.drawable.ic_user),
                        color = Greenish,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToUser(0) }
                    )
                    MetricCard(
                        title = "Blocked",
                        value = "$blockedUsers",
                        icon = painterResource(R.drawable.ic_user),
                        color = Color.Red,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToUser(2) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Restricted",
                        value = "$restrictedUsers",
                        icon = painterResource(R.drawable.ic_user),
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToUser(1) }
                    )

                    // User Status Pie Chart Card
                    UserStatusPieChartCard(
                        normalUsers = normalUsers,
                        blockedUsers = blockedUsers,
                        restrictedUsers = restrictedUsers,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Admin Metrics Section
                Text(
                    text = "Admin Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Admins",
                        value = "$totalAdmins",
                        icon = painterResource(R.drawable.ic_user),
                        color = Greenish,
                        modifier = Modifier.weight(1f),
                        onClick = { onRequireAdminAccess() }
                    )
                    MetricCard(
                        title = "Blocked",
                        value = "$blockedAdmins",
                        icon = painterResource(R.drawable.ic_user),
                        color = Color.Red,
                        modifier = Modifier.weight(1f),
                        onClick = { onRequireAdminAccess() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Restricted",
                        value = "$restrictedAdmins",
                        icon = painterResource(R.drawable.ic_user),
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f),
                        onClick = { onRequireAdminAccess() }
                    )
                    AdminStatusPieChartCard(
                        totalAdmins = totalAdmins,
                        blockedAdmins = blockedAdmins,
                        restrictedAdmins = restrictedAdmins,
                        onRequireAdminAccess = onRequireAdminAccess,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Metrics Section
                Text(
                    text = "Product Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Products",
                        value = "$totalProducts",
                        icon = painterResource(R.drawable.ic_items),
                        color = Greenish,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToItem(0) } // Default to listed/all? Or use separate logic. Let's say 0 is Listed.
                    )
                    MetricCard(
                        title = "Listed",
                        value = "$listedProducts",
                        icon = painterResource(R.drawable.ic_items),
                        color = DarkGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToItem(0) }
                    )
                }

                val bucketLt100 = allProducts?.count { it.price < 100.0 } ?: 0
                val bucket100_499 = allProducts?.count { it.price >= 100.0 && it.price <= 499.0 } ?: 0
                val bucket500_999 = allProducts?.count { it.price >= 500.0 && it.price <= 999.0 } ?: 0
                val bucket1000_1499 = allProducts?.count { it.price >= 1000.0 && it.price <= 1499.0 } ?: 0
                val bucket1500_2000 = allProducts?.count { it.price >= 1500.0 && it.price <= 2000.0 } ?: 0
                val bucketGt2000 = allProducts?.count { it.price > 2000.0 } ?: 0

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Unlisted",
                            value = "$unlistedProducts",
                            icon = painterResource(R.drawable.ic_items),
                            color = Color.Red,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToItem(1) }
                        )
                        MetricCard(
                            title = "Avg Price",
                            value = "$${String.format("%.2f", avgPrice)}",
                            icon = painterResource(R.drawable.ic_items),
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val intent = Intent(context, AdminProductPriceMetric::class.java)
                                context.startActivity(intent)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductListingPieChartCard(
                            listed = listedProducts,
                            unlisted = unlistedProducts,
                            modifier = Modifier.weight(1f)
                        )
                        ProductPriceRangePieChartCard(
                            lt100 = bucketLt100,
                            r100_499 = bucket100_499,
                            r500_999 = bucket500_999,
                            r1000_1499 = bucket1000_1499,
                            r1500_2000 = bucket1500_2000,
                            gt2000 = bucketGt2000,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserStatusPieChartCard(
    normalUsers: Int,
    blockedUsers: Int,
    restrictedUsers: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val total = normalUsers + blockedUsers + restrictedUsers

    Card(
        modifier = modifier
            .height(120.dp)
            .clickable {
                val intent = Intent(context, AdminUserMetric::class.java)
                context.startActivity(intent)
                if (context is ComponentActivity) {
                    context.finish()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "User Status (Tap For More)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (total > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pie Chart
                    PieChart(
                        normalUsers = normalUsers,
                        blockedUsers = blockedUsers,
                        restrictedUsers = restrictedUsers,
                        total = total,
                        modifier = Modifier.size(50.dp)
                    )

                    // Legend
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LegendItem(color = Greenish, label = "Normal", count = normalUsers)
                        LegendItem(color = Color.Red, label = "Blocked", count = blockedUsers)
                        LegendItem(color = Color(0xFFFF9800), label = "Restricted", count = restrictedUsers)
                    }
                }
            } else {
                Text(
                    text = "No data",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PieChart(
    normalUsers: Int,
    blockedUsers: Int,
    restrictedUsers: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2
        val strokeWidth = 15f

        var startAngle = -90f

        // Normal users (Green)
        if (normalUsers > 0) {
            val sweepAngle = (normalUsers.toFloat() / total) * 360f
            drawArc(
                color = Greenish,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }

        // Blocked users (Red)
        if (blockedUsers > 0) {
            val sweepAngle = (blockedUsers.toFloat() / total) * 360f
            drawArc(
                color = Color.Red,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }

        // Restricted users (Orange)
        if (restrictedUsers > 0) {
            val sweepAngle = (restrictedUsers.toFloat() / total) * 360f
            drawArc(
                color = Color(0xFFFF9800),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
        }
    }
}

@Composable
fun AdminStatusPieChartCard(
    totalAdmins: Int,
    blockedAdmins: Int,
    restrictedAdmins: Int,
    onRequireAdminAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalAdmins = totalAdmins - blockedAdmins - restrictedAdmins
    val total = totalAdmins
    val context = LocalContext.current
    Card(
        modifier = modifier.height(120.dp).clickable {
            val intent = Intent(context, AdminAdminMetric::class.java)
            context.startActivity(intent)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Admin Status",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (total > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChart(
                        normalUsers = normalAdmins,
                        blockedUsers = blockedAdmins,
                        restrictedUsers = restrictedAdmins,
                        total = total,
                        modifier = Modifier.size(50.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LegendItem(color = Greenish, label = "Normal", count = normalAdmins)
                        LegendItem(color = Color.Red, label = "Blocked", count = blockedAdmins)
                        LegendItem(color = Color(0xFFFF9800), label = "Restricted", count = restrictedAdmins)
                    }
                }
            } else {
                Text(text = "No data", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ProductListingPieChartCard(
    listed: Int,
    unlisted: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val total = listed + unlisted
    Card(
        modifier = modifier.height(120.dp).clickable {
            val intent = Intent(context, AdminProductMetric::class.java)
            context.startActivity(intent)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Listing Status",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (total > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductPieChartSegments(
                        segments = listOf(listed, unlisted),
                        colors = listOf(DarkGreen, Color.Red),
                        modifier = Modifier.size(50.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LegendItem(color = DarkGreen, label = "Listed", count = listed)
                        LegendItem(color = Color.Red, label = "Unlisted", count = unlisted)
                    }
                }
            } else {
                Text(text = "No data", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ProductPriceRangePieChartCard(
    lt100: Int,
    r100_499: Int,
    r500_999: Int,
    r1000_1499: Int,
    r1500_2000: Int,
    gt2000: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val total = lt100 + r100_499 + r500_999 + r1000_1499 + r1500_2000 + gt2000
    val colors = listOf(
        Color(0xFF4CAF50), // <100
        Color(0xFFFFC107), // 100-499
        Color(0xFF03A9F4), // 500-999
        Color(0xFF9C27B0), // 1000-1499
        Color(0xFFFF5722), // 1500-2000
        Color(0xFF607D8B)  // >2000
    )
    Card(
        modifier = modifier.height(120.dp).clickable {
            val intent = Intent(context, AdminProductPriceMetric::class.java)
            context.startActivity(intent)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Price Range",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (total > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductPieChartSegments(
                        segments = listOf(lt100, r100_499, r500_999, r1000_1499, r1500_2000, gt2000),
                        colors = colors,
                        modifier = Modifier.size(50.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LegendItem(color = colors[0], label = "<100", count = lt100)
                        LegendItem(color = colors[1], label = "100-499", count = r100_499)
                        LegendItem(color = colors[2], label = "500-999", count = r500_999)
                        LegendItem(color = colors[3], label = "1000-1499", count = r1000_1499)
                        LegendItem(color = colors[4], label = "1500-2000", count = r1500_2000)
                        LegendItem(color = colors[5], label = ">2000", count = gt2000)
                    }
                }
            } else {
                Text(text = "No data", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ProductPieChartSegments(
    segments: List<Int>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val strokeWidth = 15f
        var startAngle = -90f
        val total = segments.sum()
        if (total <= 0) return@Canvas
        segments.forEachIndexed { idx, value ->
            if (value > 0) {
                val sweepAngle = (value.toFloat() / total) * 360f
                drawArc(
                    color = colors[idx % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$label: $count",
            fontSize = 10.sp,
            color = Color.Black
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: Painter,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) {
            modifier
                .height(120.dp)
                .clickable { onClick() }
        } else {
            modifier
                .height(120.dp)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ItemsContent(searchText: String) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val allProducts by productViewModel.allProducts.collectAsState()

    var showUnlistDialog by remember { mutableStateOf<ProductModel?>(null) }
    var showListDialog by remember { mutableStateOf<ProductModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ProductModel?>(null) }
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    val hasInternet = isInternetAvailableExp(context)
    val products = if (searchText.isBlank()) {
        allProducts ?: emptyList()
    } else {
        (allProducts ?: emptyList()).filter { product ->
            product.name.contains(searchText, ignoreCase = true)
        }
    }

    

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                productViewModel.getAllProduct()
                delay(1000)
                isRefreshing = false
            }
        }
    ) {
        
        if (!hasInternet) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_internet),
                    contentDescription = null
                )
            }
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_data),
                    contentDescription = null
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = products,
                    key = { it.productId }
                ) { product ->
                    ItemCardExp(
                        product = product,
                        onListClick = { showListDialog = product },
                        onUnlistClick = { showUnlistDialog = product },
                        onDeleteClick = { showDeleteDialog = product }
                    )
                }
            }
        }
    }

    // Unlist Dialog
    showUnlistDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showUnlistDialog = null },
            title = { Text("Unlist Item") },
            text = {
                Text("Are you sure you want to unlist ${product.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.listProduct(product.productId, false) { success, message ->
                            if (success) {
                                // Create notification
                                val notification = NotificationModel(
                                    message = "Item '${product.name}' has been unlisted successfully",
                                    type = "item_unlisted",
                                    itemId = product.productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showUnlistDialog = null
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showUnlistDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Unlist", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showUnlistDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // List Dialog
    showListDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showListDialog = null },
            title = { Text("List Item") },
            text = {
                Text("Are you sure you want to list ${product.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.listProduct(product.productId, true) { success, message ->
                            if (success) {
                                // Create notification
                                val notification = NotificationModel(
                                    message = "Item '${product.name}' has been listed successfully",
                                    type = "item_listed",
                                    itemId = product.productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showListDialog = null
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showListDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("List", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showListDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Delete Dialog
    showDeleteDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Item") },
            text = {
                Text("Are you sure you want to delete ${product.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.deleteProduct(product.productId) { success, message ->
                            if (success) {
                                // Create notification
                                val notification = NotificationModel(
                                    message = "Item '${product.name}' has been deleted successfully",
                                    type = "item_deleted",
                                    itemId = product.productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                productViewModel.getAllProduct()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            showDeleteDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun UsersContent(searchText: String) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val allUsers by userViewModel.allUsers.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<UserModel?>(null) }
    var showBlockDialog by remember { mutableStateOf<UserModel?>(null) }
    var showRestrictDialog by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }

    val hasInternet = isInternetAvailableExp(context)
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val users = if (searchText.isBlank()) {
        allUsers ?: emptyList()
    } else {
        (allUsers ?: emptyList()).filter { user ->
            user.userId.isNotEmpty() && user.name.isNotEmpty() &&
                    user.name.contains(searchText, ignoreCase = true)
        }
    }

    

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                userViewModel.getAllUser()
                delay(1000)
                isRefreshing = false
            }
        }
    ) {
        
        if (!hasInternet) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_internet),
                    contentDescription = null
                )
            }
        } else if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_data),
                    contentDescription = null
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = users,
                    key = { it.userId }
                ) { user ->
                    UserCardExp(
                        user = user,
                        onDeleteClick = { showDeleteDialog = user },
                        onBlockClick = { showBlockDialog = user },
                        onRestrictClick = { showRestrictDialog = user }
                    )
                }
            }
        }
    }

    // Dialogs
    UserDialogsExp(
        showDeleteDialog = showDeleteDialog,
        showBlockDialog = showBlockDialog,
        showRestrictDialog = showRestrictDialog,
        onDismissDelete = { showDeleteDialog = null },
        onDismissBlock = { showBlockDialog = null },
        onDismissRestrict = { showRestrictDialog = null },
        userViewModel = userViewModel,
        notificationViewModel = notificationViewModel
    )
}

fun isInternetAvailableExp(context: android.content.Context): Boolean {
    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}
@Composable
fun ItemCardExp(
    product: ProductModel,
    onListClick: () -> Unit,
    onUnlistClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable {
                val intent = Intent(context, AdminItemDetailActivity::class.java)
                intent.putExtra("productId", product.productId)
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!product.isListed) Color(0xFFFFEBEE) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image on the left
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                val displayImage = if (product.imageUrl.isNotEmpty()) {
                    product.imageUrl
                } else if (product.imageUrls.isNotEmpty()) {
                    product.imageUrls.first()
                } else {
                    ""
                }

                if (displayImage.isNotEmpty()) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_items),
                        placeholder = painterResource(R.drawable.ic_items)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_items),
                        contentDescription = "Product Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Content on the right
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Product name
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Category
                Text(
                    text = "Category: ${product.category}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Price
                Text(
                    text = "Price: ${product.price}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Type
                Text(
                    text = "Type: ${product.type}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Description
                Text(
                    text = "Description: ${product.description}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Unlisted status
                if (!product.isListed) {
                    Text(
                        text = "UNLISTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (product.isListed) {
                        // Unlist Button
                        Button(
                            onClick = onUnlistClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            modifier = Modifier.height(40.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_visibility_off_24),
                                contentDescription = "Unlist",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unlist",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    } else {
                        // List Button
                        Button(
                            onClick = onListClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            modifier = Modifier.height(40.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_visibility_24),
                                contentDescription = "List",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "List",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Delete Button
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.height(40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserCardExp(
    user: UserModel,
    onDeleteClick: () -> Unit,
    onBlockClick: () -> Unit,
    onRestrictClick: () -> Unit
) {
    // Determine card color: blocked = light red, restricted = light orange, normal = white
    val cardColor = when {
        user.isBlocked -> Color(0xFFFFEBEE) // Light red
        user.isRestricted -> Color(0xFFFFF3E0) // Light orange
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Name
            Text(
                text = user.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Email
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = Color.Gray
            )
            // Status labels
            if (user.isBlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BLOCKED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
            if (user.isRestricted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RESTRICTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800) // Orange
                )
            }
            // Buttons at the bottom
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Block/Unblock button
                Button(
                    onClick = onBlockClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) DarkGreen else Color.Red
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (user.isBlocked) "Unblock" else "Block",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                // Restrict/Unrestrict button
                Button(
                    onClick = onRestrictClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRestricted) DarkGreen else Color(0xFFFF9800) // Orange
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (user.isRestricted) "Unrestrict" else "Restrict",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                // Delete button
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UserDialogsExp(
    showDeleteDialog: UserModel?,
    showBlockDialog: UserModel?,
    showRestrictDialog: UserModel?,
    onDismissDelete: () -> Unit,
    onDismissBlock: () -> Unit,
    onDismissRestrict: () -> Unit,
    userViewModel: UserViewModel,
    notificationViewModel: NotificationViewModel
) {
    val context = LocalContext.current

    // Delete Confirmation Dialog
    showDeleteDialog?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${user.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.deleteUser(user.userId) { success, message ->
                            if (success) {
                                // Create notification
                                val notification = NotificationModel(
                                    message = "User '${user.name}' has been deleted successfully",
                                    type = "user_deleted",
                                    userId = user.userId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                userViewModel.getAllUser()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            onDismissDelete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Block/Unblock Dialog
    showBlockDialog?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissBlock,
            title = { Text(if (user.isBlocked) "Unblock User" else "Block User") },
            text = {
                Text("Are you sure you want to ${if (user.isBlocked) "unblock" else "block"} ${user.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.blockUser(user.userId, !user.isBlocked) { success, message ->
                            if (success) {
                                // Create notification
                                val notification = NotificationModel(
                                    message = if (user.isBlocked) {
                                        "User '${user.name}' has been unblocked successfully"
                                    } else {
                                        "User '${user.name}' has been blocked successfully"
                                    },
                                    type = if (user.isBlocked) "user_unblocked" else "user_blocked",
                                    userId = user.userId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                userViewModel.getAllUser()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            onDismissBlock()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) DarkGreen else Color.Red
                    )
                ) {
                    Text(if (user.isBlocked) "Unblock" else "Block", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissBlock,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Restrict/Unrestrict Dialog
    showRestrictDialog?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissRestrict,
            title = { Text(if (user.isRestricted) "Unrestrict User" else "Restrict User") },
            text = {
                Text("Are you sure you want to ${if (user.isRestricted) "unrestrict" else "restrict"} ${user.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.restrictUser(user.userId, !user.isRestricted) { success, message ->
                            if (success) {
                                // Create notification
                                val notification = NotificationModel(
                                    message = if (user.isRestricted) {
                                        "User '${user.name}' has been unrestricted successfully"
                                    } else {
                                        "User '${user.name}' has been restricted successfully"
                                    },
                                    type = if (user.isRestricted) "user_unrestricted" else "user_restricted",
                                    userId = user.userId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                userViewModel.getAllUser()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            onDismissRestrict()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRestricted) DarkGreen else Color(0xFFFF9800) // Orange
                    )
                ) {
                    Text(if (user.isRestricted) "Unrestrict" else "Restrict", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissRestrict,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun BadgedNotificationIconExp(
    unreadCount: Int,
    iconPainter: Painter,
    contentDescription: String
) {
    Box {
        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            tint = Color.White
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

@Composable
fun PullToRefreshLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val triggerDistance = with(density) { 160.dp.toPx() }
    val maxDragDistance = with(density) { 200.dp.toPx() }

    var pullOffset by remember { mutableFloatStateOf(0f) }
    val mutatorMutex = remember { MutatorMutex() }
    val coroutineScope = rememberCoroutineScope()
    var animationJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag) {
                    animationJob?.cancel()
                }
                if (available.y < 0 && pullOffset > 0) {
                    val newOffset = (pullOffset + available.y).coerceAtLeast(0f)
                    val consumed = newOffset - pullOffset
                    pullOffset = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.Drag) {
                    animationJob?.cancel()
                }
                if (available.y > 0) {
                    val dragMultiplier = 0.25f
                    val newOffset = (pullOffset + available.y * dragMultiplier).coerceAtMost(maxDragDistance)
                    pullOffset = newOffset
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                animationJob?.cancel()
                val target = if (pullOffset >= triggerDistance) {
                    onRefresh()
                    triggerDistance
                } else {
                    0f
                }
                animationJob = coroutineScope.launch {
                    mutatorMutex.mutate {
                        val animSpec: AnimationSpec<Float> = if (target == 0f) {
                            tween(durationMillis = 150)
                        } else {
                            spring()
                        }
                        animate(pullOffset, target, animationSpec = animSpec) { value, _ ->
                            pullOffset = value
                        }
                    }
                }
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            mutatorMutex.mutate {
                animate(pullOffset, 0f, animationSpec = tween(durationMillis = 200)) { value, _ ->
                    pullOffset = value
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        if (pullOffset > 0 || isRefreshing) {
            val progress = (pullOffset / triggerDistance).coerceIn(0f, 1f)
            // Scale and alpha animation to hide on slight scroll
            val animatedScale = if (isRefreshing) 1f else progress.coerceIn(0f, 1f)
            val animatedAlpha = if (isRefreshing) 1f else (progress * 2).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, (pullOffset).roundToInt()) }
                    .padding(top = 16.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        alpha = animatedAlpha
                    }
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DarkGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_refresh),
                                contentDescription = "Pull to refresh",
                                tint = DarkGreen,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(progress * 360f)
                            )
                        }
                    }
                }
            }
        }
    }
}
