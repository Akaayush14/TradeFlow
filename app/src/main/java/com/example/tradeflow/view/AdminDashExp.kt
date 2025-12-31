package com.example.tradeflow.view
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(1) } // 0 for User, 1 for Metrics, 2 for Items
    var backPressedTime by remember { mutableLongStateOf(0L) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }

    // Notification view model for unread count
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        notificationViewModel.getUnreadCount()
    }

    // Handle back button press
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            // Exit app if pressed twice within 2 seconds
            if (context is ComponentActivity) {
                context.finishAffinity()
            }
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "Click again to quit", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
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
        },
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
                    onClick = {
                        val intent = Intent(context, AdminDashUser::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
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
                    onClick = {
                        val intent = Intent(context, AdminDashItem::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
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
                    onClick = {
                        val intent = Intent(context, AdminNotification::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
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
                    onClick = {
                        val intent = Intent(context, AdminProfile::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
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
                            "user",
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
                            "metrics",
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
                            "items",
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
                    1 -> MetricsContent()
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
                        if (passwordInput == "123") {
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
fun MetricsContent() {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
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
                onClick = {
                    val intent = Intent(context, AdminDashUser::class.java).apply {
                        putExtra("target_tab", 0)
                    }
                    context.startActivity(intent)
                }
            )
            MetricCard(
                title = "Blocked",
                value = "$blockedUsers",
                icon = painterResource(R.drawable.ic_user),
                color = Color.Red,
                modifier = Modifier.weight(1f),
                onClick = {
                    val intent = Intent(context, AdminDashUser::class.java).apply {
                        putExtra("target_tab", 2)
                    }
                    context.startActivity(intent)
                }
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
                onClick = {
                    val intent = Intent(context, AdminDashUser::class.java).apply {
                        putExtra("target_tab", 2)
                    }
                    context.startActivity(intent)
                }

            )
            Spacer(modifier = Modifier.weight(1f))
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
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Blocked",
                value = "$blockedAdmins",
                icon = painterResource(R.drawable.ic_user),
                color = Color.Red,
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
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
                onClick = {
                    val intent = Intent(context, AdminDashItem::class.java)
                    context.startActivity(intent)
                }
            )
            MetricCard(
                title = "Listed",
                value = "$listedProducts",
                icon = painterResource(R.drawable.ic_items),
                color = DarkGreen,
                modifier = Modifier.weight(1f),
                onClick = {
                    val intent = Intent(context, AdminDashItem::class.java).apply {
                        putExtra("target_tab", 0)
                    }
                    context.startActivity(intent)
                }




            )
        }

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
                onClick = {
                    val intent = Intent(context, AdminDashItem::class.java).apply {
                        putExtra("target_tab", 1)
                    }
                    context.startActivity(intent)
                }
            )
            MetricCard(
                title = "Avg Price",
                value = "$${String.format("%.2f", avgPrice)}",
                icon = painterResource(R.drawable.ic_items),
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        // Charts/Graphs placeholders removed as per request to focus on fetching values
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

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    // Filter products based on search text (case-insensitive search by product name)
    val products = if (searchText.isBlank()) {
        allProducts ?: emptyList()
    } else {
        (allProducts ?: emptyList()).filter { product ->
            product.name.contains(searchText, ignoreCase = true)
        }
    }

    if (products.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchText.isBlank()) "No items yet" else "No items found",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
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

    // Filter users based on search text (case-insensitive search by user name)
    val users = if (searchText.isBlank()) {
        allUsers ?: emptyList()
    } else {
        (allUsers ?: emptyList()).filter { user ->
            user.userId.isNotEmpty() && user.name.isNotEmpty() &&
                    user.name.contains(searchText, ignoreCase = true)
        }
    }

    if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchText.isBlank()) "No users yet" else "No users found",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
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

@Composable
fun ItemCardExp(
    product: ProductModel,
    onListClick: () -> Unit,
    onUnlistClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!product.isListed) Color(0xFFFFEBEE) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Price: $${product.price}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${product.type}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location: ${product.location}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (!product.isListed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "UNLISTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.isListed) {
                    // Show Unlist button when listed
                    Button(
                        onClick = onUnlistClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Unlist",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // Show List button (green) when unlisted
                    Button(
                        onClick = onListClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "List",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
                // Always show Delete button
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 12.sp,
                        color = Color.White
                    )
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
