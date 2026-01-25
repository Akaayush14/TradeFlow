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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import kotlin.math.abs
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import com.example.tradeflow.R
import com.example.tradeflow.model.NotificationModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.NotificationRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.NotificationViewModel
import com.example.tradeflow.viewmodel.UserViewModel

class AdminDashUser : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val targetTab = intent.getIntExtra("target_tab", 0)
        setContent {
            AdminUserScreen(
                initialTab = targetTab,
                onBackClick = {
                    val intent = Intent(this, AdminDashExp::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminUserScreen(initialTab: Int = 0, onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) } // 0 for None, 1 for Restricted, 2 for Blocked

    // Notification view model for unread count
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    
    LaunchedEffect(Unit) {
        notificationViewModel.getUnreadCount()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    // Optional: Only show back if needed, or if this screen is pushed on a stack.
                    // For main tabs, we might not want a back button, or it could go back to Explore.
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "User",
                            color = White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tab Row for None, Restricted, and Blocked
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
                            "None",
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
                            "Restricted",
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
                            "Blocked",
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
                    0 -> NoneContent()
                    1 -> RestrictedContent()
                    2 -> BlockedContent()
                }
            }
        }
    }
}

@Composable
fun NoneContent() {
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

    val hasInternet = isInternetAvailable(context)
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    // Filter users with no restrictions and no blocks (normal users)
    val noneUsers = allUsers?.filter { 
        it.userId.isNotEmpty() && it.name.isNotEmpty() && !it.isBlocked && !it.isRestricted 
    } ?: emptyList()

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                userViewModel.getAllUser()
                delay(800)
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
        } else if (noneUsers.isEmpty()) {
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
                    items = noneUsers,
                    key = { it.userId }
                ) { user ->
                    UserCardUser(
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
    UserDialogs(
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
fun RestrictedContent() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val notificationViewModel = remember { NotificationRepoImpl() }.let { NotificationViewModel(it) }
    val allUsers by userViewModel.allUsers.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<UserModel?>(null) }
    var showBlockDialog by remember { mutableStateOf<UserModel?>(null) }
    var showRestrictDialog by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }

    val hasInternet = isInternetAvailable(context)
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    // Filter restricted users
    val restrictedUsers = allUsers?.filter { 
        it.userId.isNotEmpty() && it.name.isNotEmpty() && it.isRestricted 
    } ?: emptyList()

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                userViewModel.getAllUser()
                delay(800)
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
        } else if (restrictedUsers.isEmpty()) {
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
                    items = restrictedUsers,
                    key = { it.userId }
                ) { user ->
                    UserCardUser(
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
    UserDialogs(
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
fun BlockedContent() {
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

    val hasInternet = isInternetAvailable(context)
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    // Filter blocked users
    val blockedUsers = allUsers?.filter { 
        it.userId.isNotEmpty() && it.name.isNotEmpty() && it.isBlocked 
    } ?: emptyList()

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                userViewModel.getAllUser()
                delay(800)
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
        } else if (blockedUsers.isEmpty()) {
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
                    items = blockedUsers,
                    key = { it.userId }
                ) { user ->
                    UserCardUser(
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
    UserDialogs(
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

fun isInternetAvailable(context: android.content.Context): Boolean {
    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}

@Composable
fun RefreshSpinnerUser() {
    val transition = rememberInfiniteTransition(label = "refresh")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "rotation"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = "Refreshing",
                tint = DarkGreen,
                modifier = Modifier.size(20.dp).rotate(rotation.value)
            )
        }
    }
}

@Composable
fun UserCardUser(
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture (Left)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .border(1.dp, Greenish, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_profile),
                            placeholder = painterResource(R.drawable.ic_profile)
                        )
                    } else {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(50.dp),
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                // Details Column (Right)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Name & Status
                    Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name.ifEmpty { "Unknown User" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                         if (user.isBlocked) {
                            Text(
                                text = "BLOCKED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        } else if (user.isRestricted) {
                            Text(
                                text = "RESTRICTED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }

                    // Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.email.ifEmpty { "No email" },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    // Phone
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Phone,
                            contentDescription = "Phone",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.phone.ifEmpty { "No phone" },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.location.ifEmpty { "No location" },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Gender & DOB
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Gender
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Person,
                            contentDescription = "Gender",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.gender.ifEmpty { "N/A" },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // DOB
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                            contentDescription = "DOB",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = (user.dob ?: "").ifEmpty { "N/A" },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Buttons at the bottom
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Block/Unblock button
                Button(
                    onClick = onBlockClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) DarkGreen else Color.Red
                    ),
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = if (user.isBlocked) androidx.compose.material.icons.Icons.Filled.Lock else androidx.compose.material.icons.Icons.Filled.Block,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isBlocked) "Unblock" else "Block",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
                // Restrict/Unrestrict button
                Button(
                    onClick = onRestrictClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRestricted) DarkGreen else Color(0xFFFF9800) // Orange
                    ),
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = if (user.isRestricted) androidx.compose.material.icons.Icons.Filled.Lock else androidx.compose.material.icons.Icons.Filled.Block,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isRestricted) "Unrestrict" else "Restrict",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
                // Delete button
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delete",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UserDialogs(
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
fun BadgedNotificationIconUser(
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
