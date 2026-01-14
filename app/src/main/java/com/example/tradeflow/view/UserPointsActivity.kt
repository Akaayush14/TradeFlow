
package com.example.tradeflow.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
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
    // FIX: Add UserRepoImpl() as second parameter
    val pointDealViewModel = remember { PointDealViewModel(PointDealRepoImpl(), UserRepoImpl()) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf("Point Deals") }
    var showRedeemConfirmation by remember { mutableStateOf(false) }
    var selectedDeal by remember { mutableStateOf<PointDealModel?>(null) }

    val redemptionStatus by pointDealViewModel.redemptionStatus.observeAsState()

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
            pointDealViewModel.getActivePointDeals()
        }
    }

    // Show toast when redemption status changes
    redemptionStatus?.let { (success, message) ->
        LaunchedEffect(redemptionStatus) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if (success) {
                userViewModel.getUserById(userId)
                pointDealViewModel.getActivePointDeals()
            }
            pointDealViewModel.clearRedemptionStatus()
        }
    }

    val userData by userViewModel.users.collectAsState()
    val activeDeals by pointDealViewModel.activeDeals.observeAsState(initial = emptyList())

    val userPoints = userData?.points ?: 0L

    // Calculate tier based on points - FIXED: Moved inside composable
    val currentTier: String
    val nextTier: String
    val pointsToNextTier: Long
    val progress: Float

    when {
        userPoints < 1000 -> {
            currentTier = "Bronze"
            nextTier = "Silver"
            pointsToNextTier = 1000L - userPoints
            progress = userPoints.toFloat() / 1000f
        }
        userPoints < 5000 -> {
            currentTier = "Silver"
            nextTier = "Gold"
            pointsToNextTier = 5000L - userPoints
            progress = (userPoints - 1000).toFloat() / 4000f
        }
        else -> {
            currentTier = "Gold"
            nextTier = "Platinum"
            pointsToNextTier = 0L
            progress = 1f
        }
    }

    // Add confirmation dialog
    if (showRedeemConfirmation && selectedDeal != null) {
        AlertDialog(
            onDismissRequest = {
                showRedeemConfirmation = false
                selectedDeal = null
            },
            title = { Text("Confirm Redemption") },
            text = {
                Text("Are you sure you want to redeem ${selectedDeal?.offer} for ${selectedDeal?.pointsRequired} points?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedDeal?.let { deal ->
                            pointDealViewModel.redeemPointDeal(
                                deal.dealId,
                                deal.pointsRequired,
                                deal.title,
                                deal.offer
                            )
                        }
                        showRedeemConfirmation = false
                        selectedDeal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Greenish)
                ) {
                    Text("Yes, Redeem")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRedeemConfirmation = false
                        selectedDeal = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
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
            when (selectedTab) {
                "Point Deals" -> {
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
                            PointDealCard(
                                deal = deal,
                                userPoints = userPoints,
                                onRedeemClick = {
                                    selectedDeal = it
                                    showRedeemConfirmation = true
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                "How it works?" -> {
                    item {
                        HowItWorksContent()
                    }
                }
                "Point history" -> {
                    item {
                        PointHistoryContent()
                    }
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
    val tierColors = when (currentTier) {
        "Bronze" -> Pair(Color(0xFFFF6B35), Color(0xFFFF8C42))
        "Silver" -> Pair(Color(0xFFC0C0C0), Color(0xFFE8E8E8))
        "Gold" -> Pair(Color(0xFFFFD700), Color(0xFFFFF8DC))
        else -> Pair(Greenish, Color(0xFF90EE90))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(tierColors.first, tierColors.second)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Tier Icon Placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentTier.first().toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentTier,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$userPoints Points",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (pointsToNextTier > 0) {
                    Text(
                        text = "Earn $pointsToNextTier more point(s) to reach $nextTier.",
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .width(200.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = White,
                        trackColor = White.copy(alpha = 0.3f)
                    )
                } else {
                    Text(
                        text = "You've reached the highest tier!",
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.9f)
                    )
                }
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
private fun PointDealCard(
    deal: PointDealModel,
    userPoints: Long,
    onRedeemClick: (PointDealModel) -> Unit
) {
    val canRedeem = userPoints >= deal.pointsRequired
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val validTillDate = dateFormat.format(Date(deal.validTill))

    val tierColor = when (deal.tier) {
        "Bronze" -> Color(0xFFFF6B35)
        "Silver" -> Color(0xFFC0C0C0)
        "Gold" -> Color(0xFFFFD700)
        else -> Greenish
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tier Icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tierColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = deal.tier.first().toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${deal.tier} DEAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deal.offer,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Valid till $validTillDate",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Service Category Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Greenish.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = deal.serviceCategory,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Greenish
                    )
                }

                // Points Required Button
                Button(
                    onClick = {
                        if (canRedeem) {
                            onRedeemClick(deal)
                        }
                    },
                    enabled = canRedeem,
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canRedeem) Greenish else Color.Gray,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp)
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
