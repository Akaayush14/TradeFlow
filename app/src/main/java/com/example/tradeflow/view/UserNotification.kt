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
import com.example.tradeflow.model.RequestModel
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
    val myRequests by viewModel.myRequests.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var selectedFilter by remember { mutableStateOf("All") }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<UserNotificationModel?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadNotifications(userId)
            viewModel.loadMyRequests(userId)
        }
    }

    val filteredNotifications = when (selectedFilter) {
        "Barter" -> notifications.filter { it.requestType == "BARTER" }
        "Rent" -> notifications.filter { it.requestType == "RENT" }
        "My Requests" -> emptyList()
        else -> notifications
    }

    val filteredMyRequests = when (selectedFilter) {
        "Barter" -> myRequests.filter { it.productType == "BARTER" }
        "Rent" -> myRequests.filter { it.productType == "RENT" }
        "My Requests" -> myRequests
        else -> myRequests
    }

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
            val filters = listOf("All", "Barter", "Rent", "My Requests")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2196F3),
                            selectedLabelColor = White
                        )
                    )
                }
            }

            val hasNotifications = filteredNotifications.isNotEmpty()
            val hasRequests = filteredMyRequests.isNotEmpty()

            if (!hasNotifications && !hasRequests) {
                EmptyNotificationState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (hasNotifications) {
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
                                        onMessage = {}
                                    )
                                }
                            }
                        }
                    }

                    if (hasRequests) {
                        item {
                            if (selectedFilter == "My Requests") {
                                Text(
                                    text = "My Requests",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else if (hasNotifications) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "My Requests",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        items(filteredMyRequests) { request ->
                            SentRequestCard(
                                request = request,
                                onCancel = {
                                    if (request.status == "PENDING") {
                                        viewModel.cancelRequest(request.requestId) { _, _ -> }
                                    }
                                }
                            )
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
fun SentRequestCard(
    request: RequestModel,
    onCancel: () -> Unit
) {
    val statusColor = when (request.status) {
        "PENDING" -> Color(0xFFFFA726)
        "ACCEPTED" -> Color(0xFF4CAF50)
        "CANCELED" -> Color(0xFF9E9E9E)
        "REJECTED" -> Color(0xFFE53935)
        else -> Color(0xFF9E9E9E)
    }

    val statusBackground = when (request.status) {
        "PENDING" -> Color(0xFFFFF3E0)
        "ACCEPTED" -> Color(0xFFE8F5E9)
        "CANCELED" -> Color(0xFFF5F5F5)
        "REJECTED" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = request.productImage.ifEmpty { R.drawable.placeholderimage },
                            contentDescription = "Item",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.placeholderimage)
                        )
                        Column {
                            Text(
                                text = request.productName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (request.productType == "BARTER") Color(0xFFFFEBEE) else Color(0xFFE3F2FD)
                            ) {
                                Text(
                                    text = request.productType,
                                    fontSize = 10.sp,
                                    color = if (request.productType == "BARTER") Color(0xFFD32F2F) else Color(0xFF1976D2),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = request.ownerImage.ifEmpty { R.drawable.placeholderimage },
                            contentDescription = "Receiver",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.placeholderimage)
                        )
                        Column {
                            Text(
                                text = request.ownerName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Receiver",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Text(
                    text = formatTime(request.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusBackground
                ) {
                    Text(
                        text = when (request.status) {
                            "PENDING" -> "Pending"
                            "ACCEPTED" -> "Accepted"
                            "CANCELED" -> "Canceled"
                            "REJECTED" -> "Rejected"
                            else -> request.status
                        },
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                if (request.status == "PENDING") {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7043)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Cancel Request",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = White
                        )
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
