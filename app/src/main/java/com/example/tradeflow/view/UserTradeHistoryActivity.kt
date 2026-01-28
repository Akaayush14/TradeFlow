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
    
    // Determine what to show based on type
    val isBarter = request.productType.equals("BARTER", ignoreCase = true) || request.productType.equals("BOTH", ignoreCase = true)
    
    // For Barter: 
    // If Owner: I gave 'productName', I got 'offerProductName'
    // If Requester: I gave 'offerProductName', I got 'productName'
    
    val itemGivenName = if (isOwner) request.productName else request.offerProductName
    val itemGivenImage = if (isOwner) request.productImage else request.offerProductImage
    
    val itemReceivedName = if (isOwner) request.offerProductName else request.productName
    val itemReceivedImage = if (isOwner) request.offerProductImage else request.productImage
    
    // For Rent:
    // If Owner: I rented out 'productName'
    // If Requester: I rented 'productName'
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: User Info and Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar (Initials if no image)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (otherUserImage.isNotEmpty()) {
                        AsyncImage(
                            model = otherUserImage,
                            contentDescription = otherUserName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = otherUserName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = otherUserName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (request.completedAt > 0) formatDate(request.completedAt) else formatDate(request.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                // Status Badge
                Surface(
                    color = Greenish.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (request.status == "COMPLETED") "Completed" else request.status,
                        color = Greenish,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            if (isBarter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Given Item
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = itemGivenName.takeIf { it.isNotEmpty() } ?: "", // Fallback or handle empty
                            contentDescription = itemGivenName,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = itemGivenName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(text = "You traded", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Traded with",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    // Received Item
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = itemReceivedImage,
                            contentDescription = itemReceivedName,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = itemReceivedName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(text = "You received", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            } else {
                // Rental
                Row(modifier = Modifier.fillMaxWidth()) {
                     AsyncImage(
                        model = request.productImage,
                        contentDescription = request.productName,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = request.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOwner) "Rented to $otherUserName" else "Rented from $otherUserName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "${request.rentalPriceFormatted} • ${request.rentalPeriod}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFA000)
                        )
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
