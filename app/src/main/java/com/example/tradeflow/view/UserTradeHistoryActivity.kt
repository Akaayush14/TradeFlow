package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
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
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val currentUser = userViewModel.getCurrentUser()
    
    val tradeHistory by notificationViewModel.tradeHistory.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            notificationViewModel.loadTradeHistory(it.uid)
        }
    }

    // Calculate stats
    val totalTrades = tradeHistory.size
    val barterCount = tradeHistory.count { it.productType.equals("BARTER", ignoreCase = true) || it.productType.equals("BOTH", ignoreCase = true) }
    val rentalCount = tradeHistory.count { it.productType.equals("RENT", ignoreCase = true) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        backgroundColor = Color(0xFFE3F2FD), // Light Blue
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        count = barterCount,
                        label = "Barters",
                        backgroundColor = Color(0xFFE8F5E9), // Light Green
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        count = rentalCount,
                        label = "Rentals",
                        backgroundColor = Color(0xFFFFFDE7), // Light Yellow
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
                        .background(Color(0xFFE8F5E9)) // Light Greenish background
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // All Filter
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

                        // Rental Filter
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

                        // Barter Filter
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
                // Define order: Today, Yesterday, Older
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
                        TradeHistoryItem(trade, currentUser?.uid ?: "")
                    }
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
fun TradeHistoryItem(request: RequestModel, currentUserId: String) {
    val isOwner = request.ownerId == currentUserId
    val otherUserName = if (isOwner) request.requesterName else request.ownerName
    val otherUserImage = if (isOwner) request.requesterImage else request.ownerImage
    
    val isBarter = request.productType.equals("BARTER", ignoreCase = true) || request.productType.equals("BOTH", ignoreCase = true)
    
    // Colors
    val barterTagColor = Color(0xFFE8F5E9) // Light Green (Matches Summary)
    val barterTagTextColor = Color(0xFF2E7D32) // Dark Green
    val rentTagColor = Color(0xFFFFFDE7) // Light Yellow (Matches Summary)
    val rentTagTextColor = Color(0xFFF9A825) // Dark Yellow
    
    val completedBgColor = Color(0xFFE8F5E9) // Light Green
    val completedTextColor = Color(0xFF2E7D32) // Dark Green
    val returnedBgColor = Color(0xFFE3F2FD) // Light Blue
    val returnedTextColor = Color(0xFF1565C0) // Dark Blue
    
    // Card Background Color based on type
    val cardBackgroundColor = if (isBarter) Color(0xFFE8F5E9) else Color(0xFFFFFDE7)
    
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
                        color = if (isBarter) barterTagTextColor else rentTagTextColor,
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
                        // Use rentalPriceFormatted if available, or fallback
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
            
            // Footer
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
                Surface(
                    color = if (isReturned) returnedBgColor else completedBgColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isReturned) "Returned" else "Completed",
                            color = if (isReturned) returnedTextColor else completedTextColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoundedImage(imageUrl: String, size: androidx.compose.ui.unit.Dp) {
    if (imageUrl.isNotEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )
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
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFF00695C)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} mins ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
