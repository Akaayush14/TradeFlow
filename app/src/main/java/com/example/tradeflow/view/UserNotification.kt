package com.example.tradeflow.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNotificationScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: (UserNotificationModel) -> Unit = {},
    onViewDetails: (String) -> Unit = {}
) {
    val viewModel = remember { UserNotificationViewModel(UserNotificationRepoImpl()) }
    val notifications by viewModel.notifications.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var selectedFilter by remember { mutableStateOf("All") }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<UserNotificationModel?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadNotifications(userId)
        }
    }

    // Filter notifications
    val filteredNotifications = when (selectedFilter) {
        "Barter" -> notifications.filter { it.requestType == "BARTER" }
        "Rent" -> notifications.filter { it.requestType == "RENT" }
        else -> notifications
    }

    // Group notifications by date
    val groupedNotifications = filteredNotifications.groupBy { notification ->
        val now = Calendar.getInstance()
        val notifTime = Calendar.getInstance().apply { timeInMillis = notification.createdAt }

        when {
            now.get(Calendar.DAY_OF_YEAR) == notifTime.get(Calendar.DAY_OF_YEAR) &&
                    now.get(Calendar.YEAR) == notifTime.get(Calendar.YEAR) -> "TODAY"

            now.get(Calendar.DAY_OF_YEAR) - 1 == notifTime.get(Calendar.DAY_OF_YEAR) &&
                    now.get(Calendar.YEAR) == notifTime.get(Calendar.YEAR) -> "YESTERDAY"

            else -> "OLDER"
        }
    }

    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    Text(
                        "Notifications",
                        color = White
                    )
                },
                onBackClick = onBackClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    label = { Text("All", fontSize = 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        selectedLabelColor = White
                    )
                )
                FilterChip(
                    selected = selectedFilter == "Barter",
                    onClick = { selectedFilter = "Barter" },
                    label = { Text("Barter", fontSize = 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        selectedLabelColor = White
                    )
                )
                FilterChip(
                    selected = selectedFilter == "Rent",
                    onClick = { selectedFilter = "Rent" },
                    label = { Text("Rent", fontSize = 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        selectedLabelColor = White
                    )
                )
            }

            if (filteredNotifications.isEmpty()) {
                EmptyNotificationState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Display grouped notifications
                    listOf("TODAY", "YESTERDAY", "OLDER").forEach { section ->
                        groupedNotifications[section]?.let { notificationList ->
                            item {
                                Text(
                                    text = section,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            items(notificationList) { notification ->
                                EnhancedNotificationCard(
                                    notification = notification,
                                    onClick = {
                                        viewModel.markAsRead(notification.notificationId)
                                        onNotificationClick(notification)
                                    },
                                    onAccept = {
                                        selectedNotification = notification
                                        showAcceptDialog = true
                                    },
                                    onReject = {
                                        selectedNotification = notification
                                        showRejectDialog = true
                                    },
                                    onViewDetails = { onViewDetails(notification.requestId) },
                                    onMessage = { /* Handle messaging */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Accept Dialog
    if (showAcceptDialog && selectedNotification != null) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Accept Request?") },
            text = { Text("Do you want to accept this ${selectedNotification?.productName} request?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.acceptRequest(selectedNotification!!.requestId) { success, message ->
                            if (success) {
                                viewModel.loadNotifications(userId)
                            }
                        }
                        showAcceptDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reject Dialog
    if (showRejectDialog && selectedNotification != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Request?") },
            text = { Text("Do you want to reject this ${selectedNotification?.productName} request?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectRequest(selectedNotification!!.requestId) { success, message ->
                            if (success) {
                                viewModel.loadNotifications(userId)
                            }
                        }
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text("Reject", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EnhancedNotificationCard(
    notification: UserNotificationModel,
    onClick: () -> Unit,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
    onViewDetails: () -> Unit = {},
    onMessage: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Avatar with status indicator
                    Box {
                        AsyncImage(
                            model = notification.senderImage.ifEmpty { R.drawable.placeholderimage },
                            contentDescription = "Sender",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.placeholderimage)
                        )
                        // Unread indicator
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3))
                                    .border(2.dp, White, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${notification.senderName}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Rating
                            if (notification.senderRating > 0) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFA000)
                                )
                                Text(
                                    text = "${notification.senderRating} (${notification.senderReviewCount})",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Request Type Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (notification.requestType == "BARTER")
                                Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = notification.requestType.ifEmpty { "Message" },
                                fontSize = 10.sp,
                                color = if (notification.requestType == "BARTER")
                                    Color(0xFFD32F2F) else Color(0xFF1976D2),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Time
                Text(
                    text = formatTime(notification.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message
            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = Color.Black,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Product Section - Your Item
            if (notification.productName.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Your Item",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = notification.productImage.ifEmpty { R.drawable.placeholderimage },
                            contentDescription = "Product",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.placeholderimage)
                        )
                        Text(
                            text = notification.productName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }

            // Exchange Offer or Rental Details
            if (notification.requestType == "BARTER" && notification.offerProductName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.placeholderimage), // Use exchange icon
                            contentDescription = "Exchange",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFF6F00)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Offering in exchange",
                            fontSize = 11.sp,
                            color = Color(0xFFFF6F00),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = notification.offerProductImage.ifEmpty { R.drawable.placeholderimage },
                            contentDescription = "Offer Product",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.placeholderimage)
                        )
                        Text(
                            text = notification.offerProductName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            } else if (notification.requestType == "RENT" && notification.rentalPeriod.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Rental Period",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = notification.rentalPeriod,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Price",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = notification.rentalPrice,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            when {
                notification.status == "ACCEPTED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.placeholderimage), // Use checkmark icon
                                contentDescription = "Accepted",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Request accepted",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = onViewDetails) {
                            Text("View Details", fontSize = 12.sp)
                        }
                    }
                }
                notification.status == "REJECTED" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.placeholderimage), // Use close icon
                            contentDescription = "Rejected",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFE53935)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Request declined",
                            fontSize = 12.sp,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                notification.type == "REQUEST" && notification.status.isEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text("Reject", fontSize = 13.sp, color = Color.Black)
                        }
                        IconButton(
                            onClick = onMessage,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.placeholderimage), // Use message icon
                                contentDescription = "Message",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.placeholderimage),
                contentDescription = "No notifications",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFBDBDBD)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No notifications yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "When you get notifications, they'll show up here",
                fontSize = 13.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} min ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        diff < 604800000 -> "${diff / 86400000} days ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
