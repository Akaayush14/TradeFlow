package com.example.tradeflow.view

import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.window.Dialog
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.PointDealRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.PointDealViewModel
import com.example.tradeflow.viewmodel.PointHistoryViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.example.tradeflow.BuildConfig

// TODO: FOR TESTING ONLY! Replace with your actual Stripe Secret Key (sk_test_...) to test without a backend.
val TEST_STRIPE_SECRET_KEY = BuildConfig.STRIPE_SECRET_KEY
const val BACKEND_URL = "https://your-backend-url.com/create-payment-intent"

class UserPointsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Stripe with your publishable key
        PaymentConfiguration.init(
            context = this,
            publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY // Replace with your actual Stripe Publishable Key
        )

        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
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
    val pointHistoryViewModel = remember { PointHistoryViewModel() }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf("Buy Deals") }
    var showRedeemConfirmation by remember { mutableStateOf(false) }
    var selectedDeal by remember { mutableStateOf<PointDealModel?>(null) }
    var onPaymentSuccess by remember { mutableStateOf<() -> Unit>({}) }
    var isProcessing by remember { mutableStateOf(false) }

    val paymentSheet = rememberPaymentSheet { paymentSheetResult ->
        isProcessing = false
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(context, "Payment Successful", Toast.LENGTH_SHORT).show()
                onPaymentSuccess()
                onPaymentSuccess = {} // Reset callback to prevent accidental re-execution
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(context, "Payment Canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(context, "Payment Failed: ${paymentSheetResult.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val redemptionStatus by pointDealViewModel.redemptionStatus.observeAsState()

    val userData by userViewModel.users.collectAsState()
    val activeDeals by pointDealViewModel.activeDeals.observeAsState(initial = emptyList())
    val userGiftDeals by pointDealViewModel.userGiftDeals.observeAsState(initial = emptyList())
    val userRedemptions by pointDealViewModel.userRedemptions.observeAsState(initial = emptySet())
    val txList by pointHistoryViewModel.transactions.collectAsState()

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

    // Show toast when redemption status changes
    redemptionStatus?.let { (success, message) ->
        LaunchedEffect(redemptionStatus) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if (success) {
                // FIX: Added callback parameter
                userViewModel.getUserById(userId) { success, message, user ->
                    if (success) {
                        Log.d("PointsScreen", "User data refreshed")
                    }
                }
                pointDealViewModel.getEligibleDealsForUser(userId, currentTier)
            }
            pointDealViewModel.clearRedemptionStatus()
        }
    }

    LaunchedEffect(userId, currentTier) {
        if (userId.isNotEmpty()) {
            // FIX: Added callback parameter
            userViewModel.getUserById(userId) { success, message, user ->
                if (success) {
                    Log.d("PointsScreen", "User data loaded")
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            pointDealViewModel.getEligibleDealsForUser(userId, currentTier)
            pointHistoryViewModel.observeRecent(userId)
        }
    }

    // Add confirmation dialog
    if (showRedeemConfirmation && selectedDeal != null) {
        val requiredPoints = selectedDeal?.pointsRequired ?: 0L
        val isFreeDeal = requiredPoints == 0L
        val canRedeem = userPoints >= requiredPoints

        if (canRedeem || isFreeDeal) {
            Dialog(
                onDismissRequest = {
                    showRedeemConfirmation = false
                    selectedDeal = null
                }
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isFreeDeal) "Claim Deal" else "Redeem Deal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (isFreeDeal) "Claim ${selectedDeal?.offer} for free!"
                            else "Use points or pay directly to get ${selectedDeal?.offer}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Redeem with Points Button
                        Button(
                            onClick = {
                                selectedDeal?.let { deal ->
                                    if (isFreeDeal) {
                                        pointDealViewModel.claimPointDeal(deal.dealId, deal.title, deal.offer)
                                    } else {
                                        pointDealViewModel.redeemPointDeal(deal.dealId, requiredPoints, deal.title, deal.offer)
                                    }
                                }
                                showRedeemConfirmation = false
                                selectedDeal = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (isFreeDeal) "Claim Now" else "Redeem for $requiredPoints Points",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // OR Divider (only if not free)
                        if (!isFreeDeal) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f))
                                Text(
                                    text = "OR",
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f))
                            }

                            // Pay with Stripe Button
                            StripePaymentButton(
                                onClick = {
                                    selectedDeal?.let { deal ->
                                        val amountRs = requiredPoints.toDouble()
                                        onPaymentSuccess = {
                                            pointDealViewModel.buyDealDirectly(userId, deal, amountRs)
                                            showRedeemConfirmation = false
                                            selectedDeal = null
                                        }
                                        paymentSheet.presentWithPaymentIntent(
                                            "pi_mock_secret",
                                            PaymentSheet.Configuration("TradeFlow")
                                        )
                                    }
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                showRedeemConfirmation = false
                                selectedDeal = null
                            }
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            // Insufficient points - Show Payment Dialog
            Dialog(
                onDismissRequest = {
                    showRedeemConfirmation = false
                    selectedDeal = null
                }
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Insufficient Points",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "You need $requiredPoints points for this deal. Buy now to redeem instantly?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        StripePaymentButton(
                            enabled = !isProcessing,
                            onClick = {
                                selectedDeal?.let { deal ->
                                    val amountRs = requiredPoints.toDouble()
                                    onPaymentSuccess = {
                                        pointDealViewModel.buyDealDirectly(userId, deal, amountRs)
                                        showRedeemConfirmation = false
                                        selectedDeal = null
                                    }
                                    paymentSheet.presentWithPaymentIntent(
                                        "pi_mock_secret",
                                        PaymentSheet.Configuration("TradeFlow")
                                    )
                                }
                            }
                        )

                        TextButton(
                            onClick = {
                                showRedeemConfirmation = false
                                selectedDeal = null
                            }
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TradeFlow Points", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
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
                "Buy Points" -> {
                    item {
                        BuyPointsSection(
                            isProcessing = isProcessing,
                            onPayClick = { points ->
                                if (isProcessing) return@BuyPointsSection
                                isProcessing = true
                                val amountRs = points.toDouble()
                                val purchaseId = UUID.randomUUID().toString()
                                onPaymentSuccess = {
                                    pointDealViewModel.buyPoints(userId, points.toLong(), amountRs, purchaseId)
                                }

                                val client = OkHttpClient()
                                val request: Request

                                if (TEST_STRIPE_SECRET_KEY.isNotEmpty()) {
                                    // TEST MODE: Call Stripe API directly (INSECURE - FOR DEV ONLY)
                                    Toast.makeText(context, "Test Mode: Calling Stripe API directly...", Toast.LENGTH_SHORT).show()

                                    val formBody = FormBody.Builder()
                                        .add("amount", (points * 100).toString()) // Amount in cents
                                        .add("currency", "npr")
                                        .add("automatic_payment_methods[enabled]", "true")
                                        .build()

                                    request = Request.Builder()
                                        .url("https://api.stripe.com/v1/payment_intents")
                                        .addHeader("Authorization", "Bearer $TEST_STRIPE_SECRET_KEY")
                                        .post(formBody)
                                        .build()
                                } else {
                                    // PRODUCTION MODE: Call your backend
                                    if (BACKEND_URL.contains("your-backend-url.com")) {
                                        Toast.makeText(context, "Error: Backend URL not set. See code comments.", Toast.LENGTH_LONG).show()
                                        isProcessing = false
                                        return@BuyPointsSection
                                    }

                                    val mediaType = "application/json; charset=utf-8".toMediaType()
                                    val json = JSONObject()
                                        .put("amount", points * 100)
                                        .put("currency", "npr")
                                        .toString()

                                    val body = json.toRequestBody(mediaType)
                                    request = Request.Builder()
                                        .url(BACKEND_URL)
                                        .post(body)
                                        .build()

                                    Toast.makeText(context, "Fetching payment details from backend...", Toast.LENGTH_SHORT).show()
                                }

                                client.newCall(request).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        activity?.runOnUiThread {
                                            isProcessing = false
                                            Toast.makeText(context, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val responseBody = response.body?.string()

                                        if (response.isSuccessful && responseBody != null) {
                                            try {
                                                val json = JSONObject(responseBody)
                                                // Stripe API returns 'client_secret', some backends might return 'clientSecret'
                                                val clientSecret = json.optString("client_secret").ifEmpty {
                                                    json.optString("clientSecret")
                                                }

                                                if (clientSecret.isNotEmpty()) {
                                                    activity?.runOnUiThread {
                                                        paymentSheet.presentWithPaymentIntent(
                                                            clientSecret,
                                                            PaymentSheet.Configuration("TradeFlow")
                                                        )
                                                    }
                                                } else {
                                                    activity?.runOnUiThread {
                                                        isProcessing = false
                                                        Toast.makeText(context, "Error: Missing client_secret in response", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                activity?.runOnUiThread {
                                                    isProcessing = false
                                                    Toast.makeText(context, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            activity?.runOnUiThread {
                                                isProcessing = false
                                                Toast.makeText(context, "Server error: ${response.code} - ${response.message}", Toast.LENGTH_LONG).show()
                                                Log.e("Stripe", "Error: $responseBody")
                                            }
                                        }
                                    }
                                })
                            }
                        )
                    }
                }
                "Buy Deals" -> {
                    val dealsList = activeDeals ?: emptyList()

                    if (dealsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No active buy deals available")
                            }
                        }
                    } else {
                        items(dealsList) { deal ->
                            val isClaimed = deal.dealId in userRedemptions
                            PointDealCard(
                                deal = deal,
                                userPoints = userPoints,
                                isClaimed = isClaimed,
                                onRedeemClick = {
                                    if (!isClaimed) {
                                        selectedDeal = it
                                        showRedeemConfirmation = true
                                    }
                                },
                                onDeleteClick = null // User cannot delete buy deals
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
                    val gifts = userGiftDeals ?: emptyList()
                    val hasGifts = gifts.isNotEmpty()
                    val hasHistory = txList.isNotEmpty()

                    if (!hasGifts && !hasHistory) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No point activity in the last 30 days")
                            }
                        }
                    } else {
                        // Display Gifts Section
                        if (hasGifts) {
                            item {
                                Text(
                                    text = "Claimable Gifts",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            items(gifts) { deal ->
                                val isClaimed = deal.dealId in userRedemptions
                                PointDealCard(
                                    deal = deal,
                                    userPoints = userPoints,
                                    isClaimed = isClaimed,
                                    onRedeemClick = {
                                        if (!isClaimed) {
                                            selectedDeal = it
                                            showRedeemConfirmation = true
                                        }
                                    },
                                    onDeleteClick = if (isClaimed) {
                                        { pointDealViewModel.deleteRedemption(userId, deal.dealId) }
                                    } else null
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            // Separator if history exists
                            if (hasHistory) {
                                item {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }

                        // Display History Section
                        if (hasHistory) {
                            if (hasGifts) {
                                item {
                                    Text(
                                        text = "Transaction History",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            pointHistoryViewModel.deleteAllTransactions(userId) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete All")
                                    }
                                }
                            }
                            items(txList) { tx ->
                                TransactionItem(
                                    tx = tx,
                                    onDeleteClick = {
                                        pointHistoryViewModel.deleteTransaction(tx.id) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
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
private fun BuyPointsSection(
    isProcessing: Boolean,
    onPayClick: (Long) -> Unit
) {
    var pointsInput by remember { mutableStateOf("500") }
    val increments = listOf(100L, 250L, 500L, 1000L)
    val points = pointsInput.toLongOrNull() ?: 0L
    val ratePerPoint = 1 // Rs 1 per point
    val totalPayable = points * ratePerPoint

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Points to buy", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = pointsInput,
            onValueChange = { new ->
                if (new.all { it.isDigit() }) pointsInput = new
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            enabled = !isProcessing,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            increments.forEach { inc ->
                FilledTonalButton(
                    onClick = {
                        val current = pointsInput.toLongOrNull() ?: 0L
                        pointsInput = (current + inc).toString()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = "+$inc")
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "1 Point = Rs $ratePerPoint", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }

        Text(
            text = "Total Payable: Rs $totalPayable",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        StripePaymentButton(
            enabled = !isProcessing && points > 0,
            onClick = { onPayClick(points) }
        )
    }
}

@Composable
private fun NavigationTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Buy Points", "Buy Deals", "Point history")

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
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val tintColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    when (tab) {
                        "Buy Points" -> {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = tintColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        "Point history" -> {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = tintColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tintColor
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
    isClaimed: Boolean = false,
    onRedeemClick: (PointDealModel) -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val isFreeDeal = deal.pointsRequired == 0L
    val canRedeem = isFreeDeal || userPoints >= deal.pointsRequired
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
                        onRedeemClick(deal)
                    },
                    enabled = !isClaimed,
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClaimed) Color.Gray else Greenish,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isClaimed) "Claimed" else if (isFreeDeal) "Claim" else "${deal.pointsRequired} Points",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }

                if (isClaimed && onDeleteClick != null) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Claim",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
fun StripePaymentButton(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF635BFF), // Stripe Blurple
                disabledContainerColor = Color(0xFF635BFF).copy(alpha = 0.5f)
            )
        ) {
            Text(text = "Pay with Stripe", color = Color.White, fontSize = 16.sp)
        }

        Text(
            text = "Secure payment powered by Stripe",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun TransactionItem(
    tx: com.example.tradeflow.model.PointTransaction,
    onDeleteClick: () -> Unit
) {
    val isCredit = tx.type.equals("CREDIT", ignoreCase = true)
    val badgeColor = if (isCredit) Color(0xFF4CAF50) else Color(0xFFF44336) // green/red
    val sign = if (isCredit) "+" else "-"
    val dateText = SimpleDateFormat("dd MMM, yyyy • HH:mm", Locale.getDefault()).format(Date(tx.timestamp))

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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCredit) "C" else "D",
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.source,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val amountText = if (tx.amount > 0.0) " • Rs ${tx.amount.toInt()}" else ""
                    Text(
                        text = dateText + amountText,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$sign${kotlin.math.abs(tx.points)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
