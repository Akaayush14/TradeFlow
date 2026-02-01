package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.RequestModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.ReviewRepoImpl
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ReviewViewModel
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.CheckCircle

class UserTradeHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                UserTradeHistoryScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTradeHistoryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val notificationViewModel = remember { UserNotificationViewModel(UserNotificationRepoImpl(), ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val reviewViewModel = remember { ReviewViewModel(ReviewRepoImpl()) }
    val currentUser = userViewModel.getCurrentUser()

    val tradeHistory by notificationViewModel.tradeHistory.collectAsState()
    val userReviews by reviewViewModel.userReviews.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            notificationViewModel.loadTradeHistory(it.uid)
            reviewViewModel.getReviewsByUserId(it.uid)

            // Debug log
            Log.d("TradeHistory", "Loading trades for user: ${it.uid}")
        }
    }

    // Debug: Log when trades load
    LaunchedEffect(tradeHistory) {
        Log.d("TradeHistory", "Total trades loaded: ${tradeHistory.size}")
        tradeHistory.forEach { trade ->
            Log.d("TradeHistory", "Trade: ${trade.productName}, Status: ${trade.status}, Type: ${trade.productType}")
        }
    }

    // Calculate stats
    val totalTrades = tradeHistory.size
    val barterCount = tradeHistory.count { it.productType.equals("BARTER", ignoreCase = true) || it.productType.equals("BOTH", ignoreCase = true) }
    val rentalCount = tradeHistory.count { it.productType.equals("RENT", ignoreCase = true) }

    // Calculate unreviewed count for reminder
    val unreviewedCount = tradeHistory.count { request ->
        val isCompletedOrReturned = request.status.equals("COMPLETED", ignoreCase = true) ||
                request.status.equals("RETURNED", ignoreCase = true) ||
                (request.status.equals("ACCEPTED", ignoreCase = true) && 
                (request.productType.equals("BARTER", ignoreCase = true) || request.productType.equals("BOTH", ignoreCase = true)))
        if (!isCompletedOrReturned) return@count false

        val isOwner = request.ownerId == currentUser?.uid
        val isBarter = request.productType.equals("BARTER", ignoreCase = true) || request.productType.equals("BOTH", ignoreCase = true)
        val canRate = if (isBarter) true else !isOwner

        if (!canRate) return@count false

        val targetProductId = if (isBarter) {
            if (isOwner) request.offerProductId else request.productId
        } else {
            request.productId
        }

        !userReviews.any { it.productId == targetProductId }
    }

    // Debug log
    LaunchedEffect(unreviewedCount) {
        Log.d("TradeHistory", "Unreviewed count: $unreviewedCount")
    }

    // Filter list based on selection
    val displayedTrades = remember(tradeHistory, selectedFilter) {
        when (selectedFilter) {
            "Barter" -> tradeHistory.filter { it.productType.equals("BARTER", ignoreCase = true) || it.productType.equals("BOTH", ignoreCase = true) }
            "Rental" -> tradeHistory.filter { it.productType.equals("RENT", ignoreCase = true) }
            else -> tradeHistory
        }
    }

    // Group trades by date
    val groupedTrades = remember(displayedTrades) {
        displayedTrades.groupBy { trade ->
            val date = if (trade.completedAt > 0) Date(trade.completedAt) else Date(trade.createdAt)
            val today = Date()
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.time

            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val tradeDate = sdf.format(date)
            val todayDate = sdf.format(today)
            val yesterdayDate = sdf.format(yesterday)

            when (tradeDate) {
                todayDate -> "Today"
                yesterdayDate -> "Yesterday"
                else -> "Older"
            }
        }
    }

    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    Text(
                        "My Trade History",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = if (unreviewedCount > 0) 120.dp else 16.dp)
            ) {
                // Summary Section
                item {
                    Text(
                        text = "TRADING SUMMARY",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(
                            count = totalTrades,
                            label = "Total Trades",
                            backgroundColor = Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            count = barterCount,
                            label = "Barters",
                            backgroundColor = Color(0xFFE8F5E9),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            count = rentalCount,
                            label = "Rentals",
                            backgroundColor = Color(0xFFFFFDE7),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Filter Section
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Greenish, RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFilter = "All" }
                                    .background(if (selectedFilter == "All") Greenish else Color.Transparent)
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All",
                                    color = if (selectedFilter == "All") White else Greenish,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFilter = "Rental" }
                                    .background(if (selectedFilter == "Rental") Greenish else Color.Transparent)
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Rent",
                                    color = if (selectedFilter == "Rental") White else Greenish,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFilter = "Barter" }
                                    .background(if (selectedFilter == "Barter") Greenish else Color.Transparent)
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Barter",
                                    color = if (selectedFilter == "Barter") White else Greenish,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Trades List
                if (groupedTrades.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No completed trades found",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    val order = listOf("Today", "Yesterday", "Older")
                    val sortedGroups = groupedTrades.toSortedMap(compareBy {
                        order.indexOf(it)
                    })

                    sortedGroups.forEach { (dateLabel, trades) ->
                        item {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(trades) { trade ->
                            val isOwner = trade.ownerId == currentUser?.uid
                            val isBarter = trade.productType.equals("BARTER", ignoreCase = true) || trade.productType.equals("BOTH", ignoreCase = true)

                            val targetProductId = if (isBarter) {
                                if (isOwner) trade.offerProductId else trade.productId
                            } else {
                                trade.productId
                            }

                            val review = userReviews.find { it.productId == targetProductId }

                            TradeHistoryItem(
                                request = trade,
                                currentUserId = currentUser?.uid ?: "",
                                isReviewSubmitted = review != null,
                                reviewRating = review?.rating?.toInt() ?: 0,
                                onRateClick = { productId, productName ->
                                    Log.d("TradeHistory", "Rating button clicked for: $productName (ID: $productId)")
                                    val intent = Intent(context, RateAndReviewActivity::class.java).apply {
                                        putExtra("productId", productId)
                                        putExtra("productName", productName)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }

            // Floating Reminder Card at the bottom
            if (unreviewedCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    ReviewReminderCard(count = unreviewedCount)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    count: Int,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun TradeHistoryItem(
    request: RequestModel,
    currentUserId: String,
    isReviewSubmitted: Boolean = false,
    reviewRating: Int = 0,
    onRateClick: (String, String) -> Unit
) {
    val isOwner = request.ownerId == currentUserId
    val otherUserName = if (isOwner) request.requesterName else request.ownerName
    val otherUserImage = if (isOwner) request.requesterImage else request.ownerImage

    val isBarter = request.productType.equals("BARTER", ignoreCase = true) || request.productType.equals("BOTH", ignoreCase = true)

    // Colors
    val cardBackgroundColor = if (isBarter) Color(0xFFE8F5E9) else Color(0xFFFFFDE7)

    // Debug log
    LaunchedEffect(Unit) {
        Log.d("TradeHistoryItem", """
            Product: ${request.productName}
            Status: ${request.status}
            Type: ${request.productType}
            IsOwner: $isOwner
            IsBarter: $isBarter
            IsReviewed: $isReviewSubmitted
        """.trimIndent())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Tag + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBarter) "BARTER" else "RENTAL",
                        color = if (isBarter) Color(0xFF2E7D32) else Color(0xFFF9A825),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = getRelativeTime(if (request.completedAt > 0) request.completedAt else request.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Middle Section
            if (isBarter) {
                // Barter Layout
                val myItemName = if (isOwner) request.productName else request.offerProductName
                val myItemImage = if (isOwner) request.productImage else request.offerProductImage
                val receivedItemName = if (isOwner) request.offerProductName else request.productName
                val receivedItemImage = if (isOwner) request.offerProductImage else request.productImage

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // You Traded
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "You Traded",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RoundedImage(imageUrl = myItemImage, size = 48.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = myItemName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                val value = if (isOwner) request.productPrice else request.offerProductPrice
                                Text(
                                    text = if (value > 0) "Value: Rs ${value.toInt()}" else "Value: Free",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF00695C)
                                )
                            }
                        }
                    }

                    // Arrow
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Swapped",
                        tint = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Received
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Received",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RoundedImage(imageUrl = receivedItemImage, size = 48.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = receivedItemName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                val value = if (isOwner) request.offerProductPrice else request.productPrice
                                Text(
                                    text = if (value > 0) "Value: Rs ${value.toInt()}" else "Value: Free",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF00695C)
                                )
                            }
                        }
                    }
                }
            } else {
                // Rental Layout
                val itemName = request.productName
                val itemImage = request.productImage

                Text(
                    text = if (isOwner) "You Rented Out" else "You Rented",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundedImage(imageUrl = itemImage, size = 56.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = itemName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val price = try { request.rentalPriceFormatted } catch (e: Exception) { "" }
                        Text(
                            text = if (price.isNotEmpty()) "Rs $price" else "Rent",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF00695C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // User Info & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(imageUrl = otherUserImage, name = otherUserName, size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = otherUserName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status Badge
                val isReturned = request.status.equals("RETURNED", ignoreCase = true)
                val isAccepted = request.status.equals("ACCEPTED", ignoreCase = true)
                val isConfirmed = request.status.equals("CONFIRMED", ignoreCase = true)
                val isRented = (isAccepted || isConfirmed) && request.productType.equals("RENT", ignoreCase = true)
                
                val statusText = when {
                    isReturned -> "Returned"
                    isRented -> "Rented"
                    else -> "Completed"
                }
                
                val statusColor = when {
                    isReturned -> Color(0xFF1565C0) // Blue
                    isRented -> Color(0xFFEF6C00)   // Orange
                    else -> Color(0xFF2E7D32)       // Green
                }
                
                val statusBgColor = when {
                    isReturned -> Color(0xFFE3F2FD)
                    isRented -> Color(0xFFFFF3E0)
                    else -> Color(0xFFE8F5E9)
                }

                Surface(
                    color = statusBgColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // CRITICAL: Rate & Review Button Section
            // Checks if status is COMPLETED, RETURNED, or ACCEPTED
            val isCompletedOrReturned = request.status.equals("COMPLETED", ignoreCase = true) ||
                    request.status.equals("RETURNED", ignoreCase = true) ||
                    request.status.equals("ACCEPTED", ignoreCase = true)

            Log.d("RateButton", "Show button check: isCompletedOrReturned=$isCompletedOrReturned for ${request.productName}")

            if (isCompletedOrReturned) {
                val canRate = if (isBarter) {
                    true  // Both can rate in barter
                } else {
                    !isOwner  // Only renter can rate in rental
                }

                Log.d("RateButton", "Can rate: $canRate (isBarter=$isBarter, isOwner=$isOwner)")

                if (canRate) {
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isReviewSubmitted) {
                        // Review Submitted Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Submitted",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Review Submitted",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                }

                                Row {
                                    Text(
                                        text = "Your rating: ",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < reviewRating) Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // ⭐ RATE & REVIEW BUTTON ⭐
                        Log.d("RateButton", "Showing Rate button for ${request.productName}")

                        Button(
                            onClick = {
                                // Determine which product to rate
                                val targetProductId = if (isBarter) {
                                    if (isOwner) request.offerProductId else request.productId
                                } else {
                                    request.productId
                                }

                                val targetProductName = if (isBarter) {
                                    if (isOwner) request.offerProductName else request.productName
                                } else {
                                    request.productName
                                }

                                Log.d("RateButton", "Button clicked: $targetProductName")
                                onRateClick(targetProductId, targetProductName)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF7043),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rate & Review Item",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewReminderCard(count: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6F00)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFF6F00),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$count Items Need Your Review!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Help the community by sharing your experience",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color(0xFFFF6F00),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Helper Functions

fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} mins ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
fun Avatar(imageUrl: String, name: String, size: androidx.compose.ui.unit.Dp) {
    if (imageUrl.isNotEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            modifier = Modifier
                .size(size)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .border(1.dp, Color.LightGray, androidx.compose.foundation.shape.CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholderimage),
            error = painterResource(R.drawable.placeholderimage)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Greenish),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.5).sp
            )
        }
    }
}

@Composable
fun RoundedImage(imageUrl: String, size: androidx.compose.ui.unit.Dp) {
    if (imageUrl.isNotEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Item Image",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholderimage),
            error = painterResource(R.drawable.placeholderimage)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.placeholderimage),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}