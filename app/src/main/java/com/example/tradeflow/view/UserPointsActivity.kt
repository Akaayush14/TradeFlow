package com.example.tradeflow.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.PointDealRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.PointDealViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class UserPointsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                PointsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? ComponentActivity

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val pointDealViewModel = remember { PointDealViewModel(PointDealRepoImpl()) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf("Point Deals") }

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
        pointDealViewModel.getActivePointDeals()
    }

    val userData by userViewModel.users.collectAsState()
    val activeDeals by pointDealViewModel.activeDeals.observeAsState()

    val userPoints = userData?.points ?: 0L

    // Calculate tier based on points
    val currentTier: String
    val nextTier: String
    val pointsToNextTier: Long
    val progress: Float

    when {
        userPoints < 1000 -> {
            currentTier = "Bronze"
            nextTier = "Silver"
            pointsToNextTier = 1000 - userPoints
            progress = userPoints.toFloat() / 1000f
        }
        userPoints < 5000 -> {
            currentTier = "Silver"
            nextTier = "Gold"
            pointsToNextTier = 5000 - userPoints
            progress = (userPoints - 1000).toFloat() / 4000f
        }
        else -> {
            currentTier = "Gold"
            nextTier = "Platinum"
            pointsToNextTier = 0
            progress = 1f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TradeFlow Points", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Points Summary Card
            item {
                PointsSummaryCard(
                    currentTier = currentTier,
                    userPoints = userPoints,
                    nextTier = nextTier,
                    pointsToNextTier = pointsToNextTier,
                    progress = progress
                )
            }

            // Navigation Tabs
            item {
                NavigationTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Content based on selected tab
            if (selectedTab == "Point Deals") {
                if (activeDeals == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Greenish)
                        }
                    }
                } else {
                    val dealsList = activeDeals ?: emptyList()
                    if (dealsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active deals available",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(dealsList) { deal ->
                            PointDealCard(deal = deal, userPoints = userPoints)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            
            if (selectedTab == "How it works?") {
                item {
                    HowItWorksContent()
                }
            }
            
            if (selectedTab == "Point history") {
                item {
                    PointHistoryContent()
                }
            }
        }
    }
}

@Composable
private fun PointsSummaryCard(
    currentTier: String,
    userPoints: Long,
    nextTier: String,
    pointsToNextTier: Long,
    progress: Float
) {
    // Colors matching the reference image (Reddish-Orange Gradient)
    val gradientColors = listOf(Color(0xFFE64A19), Color(0xFFFF7043)) // Deep Orange to Light Orange

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tier Icon (Hexagon)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.GenericShape { size, _ ->
                            val width = size.width
                            val height = size.height
                            val radius = width.coerceAtMost(height) / 2
                            val centerX = width / 2
                            val centerY = height / 2

                            moveTo(centerX + radius * kotlin.math.cos(0.0).toFloat(), centerY + radius * kotlin.math.sin(0.0).toFloat())
                            for (i in 1 until 6) {
                                val angle = Math.toRadians(60.0 * i).toFloat()
                                lineTo(centerX + radius * kotlin.math.cos(angle.toDouble()).toFloat(), centerY + radius * kotlin.math.sin(angle.toDouble()).toFloat())
                            }
                            close()
                        })
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentTier,
                        fontSize = 14.sp,
                        color = White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "$userPoints Points",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (pointsToNextTier > 0) {
                Text(
                    text = "Earn $pointsToNextTier more point(s) to reach $nextTier",
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = White,
                    trackColor = White.copy(alpha = 0.3f)
                )
            } else {
                Text(
                    text = "You've reached the highest tier!",
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun NavigationTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("How it works?", "Point history", "Point Deals")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Greenish.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    when (tab) {
                        "How it works?" -> {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isSelected) Greenish else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        "Point history" -> {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = if (isSelected) Greenish else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Greenish else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun PointDealCard(deal: PointDealModel, userPoints: Long) {
    val canRedeem = userPoints >= deal.pointsRequired
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val validTillDate = dateFormat.format(Date(deal.validTill))

    val tierColor = when (deal.tier) {
        "Bronze" -> Color(0xFFFF6B35)
        "Silver" -> Color(0xFFC0C0C0)
        "Gold" -> Color(0xFFFFD700)
        else -> Greenish
    }
    
    // Define Hexagon Shape
    val hexagonShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
        val width = size.width
        val height = size.height
        val radius = width.coerceAtMost(height) / 2
        val centerX = width / 2
        val centerY = height / 2

        moveTo(centerX + radius * kotlin.math.cos(0.0).toFloat(), centerY + radius * kotlin.math.sin(0.0).toFloat())
        for (i in 1 until 6) {
            val angle = Math.toRadians(60.0 * i).toFloat()
            lineTo(centerX + radius * kotlin.math.cos(angle.toDouble()).toFloat(), centerY + radius * kotlin.math.sin(angle.toDouble()).toFloat())
        }
        close()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tier Icon (Hexagon)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(hexagonShape)
                        .background(tierColor),
                    contentAlignment = Alignment.Center
                ) {
                    // Star Icon inside
                     Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${deal.tier} DEAL".uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35) // Reddish orange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deal.offer,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Validity Pill
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Valid till $validTillDate",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Category Badge (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color(0xFFFFE0E0), RoundedCornerShape(4.dp)) // Light pink background
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = deal.serviceCategory.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F) // Red text
                )
            }

            // Points Button (Bottom Right)
            Button(
                onClick = { /* Handle redemption */ },
                enabled = canRedeem,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(top = 40.dp) // Push down to avoid overlap
                    .height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF8A80), // Salmon/Red color
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "${deal.pointsRequired} Points",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}

@Composable
private fun HowItWorksContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "How TradeFlow Points Work",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PointInfoItem("1", "Earn Points", "Add items to the marketplace and earn points based on item value. $100 = 72 points.")
        PointInfoItem("2", "Redeem Points", "Use your points to redeem exclusive deals and offers.")
        PointInfoItem("3", "Tier System", "Progress through tiers (Bronze → Silver → Gold) by earning more points.")
        PointInfoItem("4", "Better Deals", "Higher tiers unlock better deals and discounts.")
    }
}

@Composable
private fun PointInfoItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Greenish),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PointHistoryContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Point History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Your point history will appear here",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}