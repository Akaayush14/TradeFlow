package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.foundation.layout.size
import com.example.tradeflow.R
import com.example.tradeflow.model.NotificationModel
import com.example.tradeflow.repository.NotificationRepoImpl
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.NotificationViewModel
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminNotification : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminNotificationScreen(
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
fun AdminNotificationScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(4) } // Notification tab selected
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val notifications by notificationViewModel.notifications.collectAsState()
    val selectedIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedIds.isNotEmpty()

    BackHandler {
        if (isSelectionMode) {
            selectedIds.clear()
        } else {
            val intent = Intent(context, AdminDashExp::class.java)
            context.startActivity(intent)
            if (context is ComponentActivity) {
                context.finish()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Notification",
                            color = DarkGreen,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Greenish) {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = {
                        val intent = Intent(context, AdminDashExp::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
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
                            contentDescription = "Users",
                            tint = Color.White
                        )
                    },
                    label = { Text("Users", color = Color.White) }
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
                    onClick = { selectedIndex = 4 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.notification_filled),
                            contentDescription = "Notification",
                            tint = Color.White
                        )
                    },
                    label = { Text("Notification", color = Color.White) }
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
            // Control Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    TextButton(onClick = {
                        val allIds = notifications?.map { it.notificationId } ?: emptyList()
                        if (selectedIds.size == allIds.size && allIds.isNotEmpty()) {
                            selectedIds.clear()
                        } else {
                            selectedIds.clear()
                            selectedIds.addAll(allIds)
                        }
                    }) {
                        val allIds = notifications?.map { it.notificationId } ?: emptyList()
                        Text(
                            text = if (selectedIds.size == allIds.size && allIds.isNotEmpty()) "Deselect All" else "Select All",
                            color = DarkGreen
                        )
                    }

                    TextButton(onClick = {
                        val idsToDelete = selectedIds.toList()
                        idsToDelete.forEach { id ->
                            notificationViewModel.deleteNotification(id) { _, _ -> }
                        }
                        selectedIds.clear()
                        Toast.makeText(context, "${idsToDelete.size} notifications deleted", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { showDeleteAllDialog = true }) {
                        Text("Delete all", color = Color.Red)
                    }
                }
            }

            NotificationContent(
                notificationViewModel = notificationViewModel,
                selectedIds = selectedIds,
                onSelectionChange = { id, selected ->
                    if (selected) selectedIds.add(id) else selectedIds.remove(id)
                },
                onLongClick = { id ->
                    if (!selectedIds.contains(id)) selectedIds.add(id)
                }
            )
        }
        
        // Delete All Confirmation Dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Delete All Notifications") },
                text = { Text("Are you sure you want to delete all notifications? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            notificationViewModel.deleteAllNotifications { success, message ->
                                if (success) {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                                showDeleteAllDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete All", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteAllDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationContent(
    notificationViewModel: NotificationViewModel,
    selectedIds: List<String> = emptyList(),
    onSelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    onLongClick: (String) -> Unit = {}
) {
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val notifications by notificationViewModel.notifications.collectAsState()

    LaunchedEffect(Unit) {
        notificationViewModel.getAllNotifications()
        // Mark all notifications as read when screen is opened
        notificationViewModel.markAllAsRead()
    }

    if (notifications.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No notifications yet",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(
                items = notifications ?: emptyList(),
                key = { it.notificationId }
            ) { notification ->
                NotificationCard(
                    notification = notification,
                    notificationViewModel = notificationViewModel,
                    productViewModel = productViewModel,
                    userViewModel = userViewModel,
                    isSelected = selectedIds.contains(notification.notificationId),
                    isSelectionMode = selectedIds.isNotEmpty(),
                    onSelectionChange = onSelectionChange,
                    onLongClick = onLongClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationCard(
    notification: NotificationModel,
    notificationViewModel: NotificationViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onSelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    onLongClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Format timestamp
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(notification.timestamp))

    // Determine card color based on type
    val cardColor = when (notification.type) {
        "item_listed", "user_unblocked", "user_unrestricted" -> Color(0xFFE8F5E9) // Light green
        "item_unlisted", "user_blocked", "user_restricted" -> Color(0xFFFFEBEE) // Light red
        "item_deleted", "user_deleted" -> Color(0xFFFFF3E0) // Light orange
        else -> Color.White
    }

    // Check if undo is possible (not for deletions, and must have valid ID)
    val canUndo = notification.type !in listOf("item_deleted", "user_deleted") &&
            ((notification.type.startsWith("item_") && notification.itemId.isNotEmpty()) ||
             (notification.type.startsWith("user_") && notification.userId.isNotEmpty()))

    // Undo function
    val onUndoClick: () -> Unit = {
        try {
            when (notification.type) {
                "item_listed" -> {
                    // Undo: unlist the item
                    if (notification.itemId.isNotEmpty()) {
                        productViewModel.listProduct(notification.itemId, false) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Item unlisted", Toast.LENGTH_SHORT).show()
                                // Delete the notification after successful undo
                                notificationViewModel.deleteNotification(notification.notificationId) { _, _ -> }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                "item_unlisted" -> {
                    // Undo: list the item
                    if (notification.itemId.isNotEmpty()) {
                        productViewModel.listProduct(notification.itemId, true) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Item listed", Toast.LENGTH_SHORT).show()
                                // Delete the notification after successful undo
                                notificationViewModel.deleteNotification(notification.notificationId) { _, _ -> }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                "user_blocked" -> {
                    // Undo: unblock the user
                    if (notification.userId.isNotEmpty()) {
                        userViewModel.blockUser(notification.userId, false) { success, message ->
                            if (success) {
                                Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show()
                                // Delete the notification after successful undo
                                notificationViewModel.deleteNotification(notification.notificationId) { _, _ -> }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                "user_unblocked" -> {
                    // Undo: block the user
                    if (notification.userId.isNotEmpty()) {
                        userViewModel.blockUser(notification.userId, true) { success, message ->
                            if (success) {
                                Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()
                                // Delete the notification after successful undo
                                notificationViewModel.deleteNotification(notification.notificationId) { _, _ -> }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                "user_restricted" -> {
                    // Undo: unrestrict the user
                    if (notification.userId.isNotEmpty()) {
                        userViewModel.restrictUser(notification.userId, false) { success, message ->
                            if (success) {
                                Toast.makeText(context, "User unrestricted", Toast.LENGTH_SHORT).show()
                                // Delete the notification after successful undo
                                notificationViewModel.deleteNotification(notification.notificationId) { _, _ -> }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                "user_unrestricted" -> {
                    // Undo: restrict the user
                    if (notification.userId.isNotEmpty()) {
                        userViewModel.restrictUser(notification.userId, true) { success, message ->
                            if (success) {
                                Toast.makeText(context, "User restricted", Toast.LENGTH_SHORT).show()
                                // Delete the notification after successful undo
                                notificationViewModel.deleteNotification(notification.notificationId) { _, _ -> }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onSelectionChange(notification.notificationId, !isSelected)
                    }
                },
                onLongClick = {
                    onLongClick(notification.notificationId)
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_box),
                        contentDescription = null,
                        tint = if (isSelected) Greenish else Color.Gray,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isSelected) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Selected",
                            tint = Greenish,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    if (canUndo) {
                        Button(
                            onClick = onUndoClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107) // Yellow color
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Undo",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}