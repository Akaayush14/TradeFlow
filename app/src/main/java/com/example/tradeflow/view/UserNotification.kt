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
import androidx.compose.material.icons.filled.Email
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
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNotificationScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: (UserNotificationModel) -> Unit = {},
    onViewDetails: (String) -> Unit = {},
    onMessageClick: () -> Unit = {}
) {
    val viewModel = remember { UserNotificationViewModel(UserNotificationRepoImpl(), ProductRepoImpl()) }
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
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            val filters = listOf("All", "Incoming Requests", "My Requests")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),  // ✅ Increased spacing
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" Button - Fixed width with circular shape
                Button(  // ✅ Changed to Button component
                    onClick = { selectedFilter = "All" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFilter == "All")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    shape = CircleShape,  // ✅ Fully rounded shape
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    modifier = Modifier.height(48.dp),  // ✅ NO weight() - natural width
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (selectedFilter == "All") 0.dp else 0.dp
                    ),
                    border = if (selectedFilter != "All")
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    else null
                ) {
                    Text(
                        text = "All",
                        fontSize = 14.sp,  // ✅ Larger font
                        fontWeight = FontWeight.Medium,
                        color = if (selectedFilter == "All")
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                // Other filters - Share remaining space equally
                FilterChip(
                    selected = selectedFilter == "Incoming Request",
                    onClick = { selectedFilter = "Incoming Request" },
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Incoming Request",
                                fontSize = 14.sp,
                                fontWeight = if (selectedFilter == "Incoming Request") FontWeight.Medium else FontWeight.Normal,
                                color = if (selectedFilter == "Incoming Request")
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = if (selectedFilter == "Incoming Request") null else FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                )

                FilterChip(
                    selected = selectedFilter == "My Requests",
                    onClick = { selectedFilter = "My Requests" },
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "My Requests",
                                fontSize = 14.sp,
                                fontWeight = if (selectedFilter == "My Requests") FontWeight.Medium else FontWeight.Normal,
                                color = if (selectedFilter == "My Requests")
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = if (selectedFilter == "My Requests") null else FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                )
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        onMessage = onMessageClick
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else if (hasNotifications) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "My Requests",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            title = {
                Text(
                    "Accept Request?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Do you want to accept this ${selectedNotification?.productName} request?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Accept",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = false }) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Reject Dialog
    if (showRejectDialog && selectedNotification != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text(
                    "Reject Request?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Do you want to reject this ${selectedNotification?.productName} request?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        "Reject",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Rating
                            if (notification.senderRating > 0) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "${notification.senderRating} (${notification.senderReviewCount})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Request Type Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (notification.requestType == "BARTER")
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = notification.requestType.ifEmpty { "Message" },
                                fontSize = 10.sp,
                                color = if (notification.requestType == "BARTER")
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message
            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (notification.productName.isNotEmpty()) {
                ProductDetailsSection(
                    header = "Your Item",
                    productId = notification.productId,
                    fallbackName = notification.productName,
                    fallbackImage = notification.productImage
                )
            }

            if (notification.requestType == "BARTER" && notification.offerProductName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ProductDetailsSection(
                    header = "Offering in exchange",
                    productId = notification.offerProductId,
                    fallbackName = notification.offerProductName,
                    fallbackImage = notification.offerProductImage
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = notification.rentalPeriod,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Price",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = notification.rentalPrice,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
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
                                painter = painterResource(R.drawable.placeholderimage),
                                contentDescription = "Accepted",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Request accepted",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = onViewDetails) {
                            Text(
                                "View Details",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                notification.status == "REJECTED" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.placeholderimage), // Use close icon
                            contentDescription = "Rejected",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Request declined",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
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
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Accept",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                "Reject",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        OutlinedButton(
                            onClick = onMessage,
                            modifier = Modifier
                                .width(40.dp)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    fallbackImage: String
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
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = header,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (desc.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = priceText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Location Icon from drawable
                        Image(
                            painter = painterResource(R.drawable.location_on),
                            contentDescription = "Location",
                            modifier = Modifier.size(16.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    var ownerImage by remember { mutableStateOf(request.ownerImage) }

    LaunchedEffect(request.ownerId) {
        if (request.ownerId.isNotEmpty()) {
            UserRepoImpl().getUserById(request.ownerId) { success, _, user ->
                if (success && user != null) {
                    ownerImage = user.profileImageUrl
                }
            }
        }
    }

    val statusColor = when (request.status) {
        "PENDING" -> MaterialTheme.colorScheme.secondary
        "ACCEPTED" -> MaterialTheme.colorScheme.tertiary
        "CANCELED" -> MaterialTheme.colorScheme.outline
        "REJECTED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    val statusBackground = when (request.status) {
        "PENDING" -> MaterialTheme.colorScheme.secondaryContainer
        "ACCEPTED" -> MaterialTheme.colorScheme.tertiaryContainer
        "CANCELED" -> MaterialTheme.colorScheme.surfaceVariant
        "REJECTED" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProductDetailsSection(
                header = "Item you're requesting",
                productId = request.productId,
                fallbackName = request.productName,
                fallbackImage = request.productImage
            )

            if (request.offerProductId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ProductDetailsSection(
                    header = "Your offering",
                    productId = request.offerProductId,
                    fallbackName = request.offerProductName,
                    fallbackImage = request.offerProductImage
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                model = ownerImage.ifEmpty { R.drawable.placeholderimage },
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
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Owner",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatTime(request.createdAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Cancel Request",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onError
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
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No notifications yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "When you get notifications, they'll show up here",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    MaterialTheme {
        UserNotificationScreen()
    }
}