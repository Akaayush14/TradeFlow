package com.example.tradeflow.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.model.RequestModel
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepoImpl
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
        "My Requests" -> emptyList()
        else -> notifications
    }

    val filteredMyRequests = when (selectedFilter) {
        "Incoming Request" -> emptyList()
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

    val groupedMyRequests = filteredMyRequests.groupBy { request ->
        val now = Calendar.getInstance()
        val reqTime = Calendar.getInstance().apply { timeInMillis = request.createdAt }

        when {
            now.get(Calendar.DAY_OF_YEAR) == reqTime.get(Calendar.DAY_OF_YEAR) &&
                    now.get(Calendar.YEAR) == reqTime.get(Calendar.YEAR) -> "TODAY"

            now.get(Calendar.DAY_OF_YEAR) - 1 == reqTime.get(Calendar.DAY_OF_YEAR) &&
                    now.get(Calendar.YEAR) == reqTime.get(Calendar.YEAR) -> "YESTERDAY"

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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val filters = listOf("All", "Incoming Request", "My Requests")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2196F3),
                            selectedLabelColor = White,
                            containerColor = White,
                            labelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFFE0E0E0)
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

                        listOf("TODAY", "YESTERDAY", "OLDER").forEach { section ->
                            groupedMyRequests[section]?.let { requestList ->
                                item {
                                    Text(
                                        text = section,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(requestList) { request ->
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
                ProductDetailsSection(
                    header = "Your Item",
                    productId = notification.productId,
                    fallbackName = notification.productName,
                    fallbackImage = notification.productImage,
                    highlightColor = Color(0xFFF5F5F5)
                )
            }

            // Exchange Offer or Rental Details
            if (notification.requestType == "BARTER" && notification.offerProductName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ProductDetailsSection(
                    header = "Offering in exchange",
                    productId = notification.offerProductId,
                    fallbackName = notification.offerProductName,
                    fallbackImage = notification.offerProductImage,
                    highlightColor = Color(0xFFFFF8E1)
                )
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
fun ProductDetailsSection(
    header: String,
    productId: String,
    fallbackName: String,
    fallbackImage: String,
    highlightColor: Color
) {
    var product by remember { mutableStateOf<ProductModel?>(null) }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            ProductRepoImpl().getProductById(productId) { success, _, data ->
                if (success) {
                    product = data
                }
            }
        }
    }

    val name = product?.name ?: fallbackName
    val image = product?.imageUrl ?: fallbackImage
    val desc = product?.description ?: ""
    val location = product?.location ?: ""
    val price = product?.price ?: 0.0
    val isRent = product?.type == "Rent"
    val priceText = if (isRent) "Rs ${formatAmount(price)} / Day" else "Rs ${formatAmount(price)}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlightColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = header,
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
                model = image.ifEmpty { R.drawable.placeholderimage },
                contentDescription = "Product",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.placeholderimage)
            )
            Column {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (desc.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = priceText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Location Icon from drawable
                        Image(
                            painter = painterResource(R.drawable.location_on), // You'll need to add this drawable
                            contentDescription = "Location",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun formatAmount(value: Double): String {
    return String.format("%.2f", value)
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
            Text(
                text = if (request.productType == "BARTER") "Barter Request" else "Rent Request",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProductDetailsSection(
                header = "Item you're requesting",
                productId = request.productId,
                fallbackName = request.productName,
                fallbackImage = request.productImage,
                highlightColor = Color(0xFFEFF6FF)
            )

            if (request.offerProductId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ProductDetailsSection(
                    header = "Your offering",
                    productId = request.offerProductId,
                    fallbackName = request.offerProductName,
                    fallbackImage = request.offerProductImage,
                    highlightColor = Color(0xFFE8F5E9)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = request.ownerImage.ifEmpty { R.drawable.placeholderimage },
                    contentDescription = "Owner",
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
                        text = "Owner",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
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
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "No notifications",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    tint = Greenish
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Greenish, CircleShape)
                        .border(2.dp, White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No notifications yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "When you get notifications, they'll show up here",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

@Preview
@Composable
fun PreviewNotification(){
    UserNotificationScreen ()
}

