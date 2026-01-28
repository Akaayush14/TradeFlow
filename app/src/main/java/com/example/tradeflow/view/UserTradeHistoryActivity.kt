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
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel
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
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val currentUser = userViewModel.getCurrentUser()
    
    val allProducts by productViewModel.allProducts.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            productViewModel.getProductsByOwner(it.uid)
        }
    }

    // Filter for completed trades only
    val completedTrades = remember(allProducts) {
        allProducts.filter { it.status == "Completed" }
    }

    // Calculate stats
    val totalTrades = completedTrades.size
    val barterCount = completedTrades.count { it.type.equals("Barter", ignoreCase = true) || it.type.equals("Both", ignoreCase = true) }
    val rentalCount = completedTrades.count { it.type.equals("Rent", ignoreCase = true) }

    // Filter list based on selection
    val displayedTrades = remember(completedTrades, selectedFilter) {
        when (selectedFilter) {
            "Barter" -> completedTrades.filter { it.type.equals("Barter", ignoreCase = true) || it.type.equals("Both", ignoreCase = true) }
            "Rental" -> completedTrades.filter { it.type.equals("Rent", ignoreCase = true) }
            else -> completedTrades
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
            if (displayedTrades.isEmpty()) {
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
                items(displayedTrades) { product ->
                    TradeHistoryItem(product)
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
fun TradeHistoryItem(product: ProductModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Completed: ${formatDate(product.completedAt ?: product.createdAt)}", // Fallback if completedAt null
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = product.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (product.type == "Rent") Color(0xFFFFA000) else Greenish
                )
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
