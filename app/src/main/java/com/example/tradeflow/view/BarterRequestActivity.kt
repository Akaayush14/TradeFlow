package com.example.tradeflow.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.max

class BarterRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeWrapper {
                BarterRequestScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarterRequestScreen() {
    val context = LocalContext.current
    val activity = context as? BarterRequestActivity

    // Get passed data from intent
    val productId = activity?.intent?.getStringExtra("productId") ?: ""
    val ownerId = activity?.intent?.getStringExtra("ownerId") ?: ""

    // Initialize ViewModels
    val notificationViewModel = remember {
        UserNotificationViewModel(UserNotificationRepoImpl(), ProductRepoImpl())
    }
    val productViewModel = remember {
        ProductViewModel(ProductRepoImpl())
    }
    val userViewModel = remember {
        UserViewModel(UserRepoImpl())
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    // State management
    var currentUserData by remember { mutableStateOf<UserModel?>(null) }
    var userProducts by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var selectedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingProducts by remember { mutableStateOf(true) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Credit Points State
    var creditAmountStr by remember { mutableStateOf("") }
    val creditAmount = creditAmountStr.toDoubleOrNull() ?: 0.0

    // Load product data
    var productData by remember { mutableStateOf<ProductModel?>(null) }
    var ownerData by remember { mutableStateOf<UserModel?>(null) }

    // Load product details
    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId)
        }
    }

    // Load owner details
    LaunchedEffect(ownerId) {
        if (ownerId.isNotEmpty()) {
            userViewModel.getUserById(ownerId) { success, _, user ->
                if (success) {
                    ownerData = user
                }
            }
        }
    }

    // Load current user data
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId) { success, _, user ->
                if (success && user != null) {
                    currentUserData = user
                }
            }
        }
    }

    // Load user's products
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            isLoadingProducts = true
            productViewModel.getProductsByOwner(currentUserId)
        }
    }

    // Observe user products
    val allProducts by productViewModel.allProducts.collectAsState()
    val productDetails by productViewModel.product.collectAsState()

    LaunchedEffect(allProducts) {
        userProducts = allProducts.filter {
            it.isDeleted != true &&
                    it.status == "Available" &&
                    it.type == "Barter"
        }
        isLoadingProducts = false
    }

    LaunchedEffect(productDetails) {
        productData = productDetails
    }

    // Calculations
    val theirItemValue = productData?.price ?: 0.0
    val yourItemValue = userProducts.filter { selectedItems.contains(it.productId) }
        .sumOf { it.price }
    val difference = theirItemValue - yourItemValue
    val availablePoints = currentUserData?.points?.toDouble() ?: 0.0
    val totalOfferValue = yourItemValue + creditAmount
    val isShort = totalOfferValue < theirItemValue
    val shortAmount = theirItemValue - totalOfferValue

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Barter Request",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product they want
            item {
                productData?.let { product ->
                    ProductWantCard(product = product, owner = ownerData)
                }
            }

            // Selection instruction
            item {
                SelectionInstructionCard(selectedCount = selectedItems.size)
            }

            // User's products to offer
            if (isLoadingProducts) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (userProducts.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(userProducts) { userProduct ->
                    UserProductCard(
                        product = userProduct,
                        isSelected = selectedItems.contains(userProduct.productId),
                        onSelectionChange = { isSelected ->
                            selectedItems = if (isSelected) {
                                selectedItems + userProduct.productId
                            } else {
                                selectedItems - userProduct.productId
                            }
                        }
                    )
                }
            }

            // Value Calculation
            item {
                ValueCalculationCard(
                    theirItemValue = theirItemValue,
                    yourItemValue = yourItemValue
                )
            }

            // Add Credit Points
            item {
                AddCreditPointsCard(
                    creditAmountStr = creditAmountStr,
                    onCreditChange = { creditAmountStr = it },
                    availablePoints = availablePoints,
                    difference = if (difference > 0) difference else 0.0
                )
            }

            // Trade Summary
            item {
                TradeSummaryCard(
                    yourItemValue = yourItemValue,
                    creditPoints = creditAmount,
                    totalOffer = totalOfferValue,
                    theirItemValue = theirItemValue,
                    isShort = isShort,
                    shortAmount = shortAmount
                )
            }

            // Message section
            item {
                BarterMessageSection(
                    message = message,
                    onMessageChange = { message = it }
                )
            }

            // Action button
            item {
                BarterActionButton(
                    isEnabled = selectedItems.isNotEmpty() && !isLoading && creditAmount <= availablePoints,
                    isLoading = isLoading,
                    selectedCount = selectedItems.size,
                    onClick = {
                        val product = productData
                        val owner = ownerData
                        val user = currentUserData

                        // Null checks before sending request
                        if (product == null) {
                            errorMessage = "Product information not available"
                            showErrorDialog = true
                        } else if (owner == null) {
                            errorMessage = "Owner information not available"
                            showErrorDialog = true
                        } else if (user == null) {
                            errorMessage = "User information not available. Please try again."
                            showErrorDialog = true
                        } else if (selectedItems.isEmpty()) {
                            errorMessage = "Please select at least one item to offer"
                            showErrorDialog = true
                        } else if (creditAmount > availablePoints) {
                            errorMessage = "Insufficient credit points"
                            showErrorDialog = true
                        } else {
                            val selectedProductList = userProducts.filter {
                                selectedItems.contains(it.productId)
                            }

                            if (selectedProductList.isNotEmpty()) {
                                isLoading = true

                                notificationViewModel.createItemRequest(
                                    product = product,
                                    owner = owner,
                                    requester = user,
                                    requestType = "BARTER",
                                    message = message,
                                    offerProducts = selectedProductList,
                                    creditPoints = creditAmount
                                ) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        showSuccessDialog = true
                                    } else {
                                        errorMessage = msg
                                        showErrorDialog = true
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                activity?.finish()
            },
            title = {
                Text(
                    "Barter Request Sent!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Your barter request has been sent successfully to the owner.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        activity?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    "Error",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}

@Composable
fun ValueCalculationCard(
    theirItemValue: Double,
    yourItemValue: Double
) {
    val difference = theirItemValue - yourItemValue
    val needsMore = difference > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9E6) // Light Yellow background
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊 ", fontSize = 16.sp) // Bar chart emoji
                    Text(
                        "Value Calculation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Text("🧮", fontSize = 16.sp) // Abacus emoji
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Their Item Value:", color = Color.Gray)
                Text("Rs$theirItemValue", fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp) // Dotted effect simulated with low alpha
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Your Item(s) Value:", color = Color.Gray)
                Text("Rs$yourItemValue", fontWeight = FontWeight.Bold, color = Color(0xFF009688)) // Teal color
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Difference:", fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.7f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Rs${if (needsMore) difference else 0.0}",
                        fontWeight = FontWeight.Bold,
                        color = if (needsMore) Color(0xFF4CAF50) else Color.Gray // Green if needs more? Wait, if difference is positive, user needs to add more.
                    )
                    if (needsMore) {
                        Text(
                            "(You need to add more)",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddCreditPointsCard(
    creditAmountStr: String,
    onCreditChange: (String) -> Unit,
    availablePoints: Double,
    difference: Double
) {
    val needsPoints = difference > 0
    
    Column {
        if (needsPoints) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0) // Light Orange
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡 ", fontSize = 18.sp)
                    Column {
                        Text(
                            "Value Mismatch!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            "You can add credit points below to balance the trade.",
                            fontSize = 12.sp,
                            color = Color(0xFFEF6C00)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF7986CB) // Purple/Blue shade
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💳 ", fontSize = 16.sp) // Credit card emoji replacement
                        Text(
                            "Add Credit Points",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Available: Rs$availablePoints",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Enter credit amount to add:",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = creditAmountStr,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onCreditChange(newValue)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    leadingIcon = {
                        Text("Rs", fontWeight = FontWeight.Bold, color = Color(0xFF7986CB))
                    },
                    trailingIcon = {
                        Column {
                            Icon(
                                painter = painterResource(android.R.drawable.arrow_up_float), // Using system arrow
                                contentDescription = "Up",
                                modifier = Modifier.size(16.dp).clickable { /* Increment logic? */ },
                                tint = Color.Gray
                            )
                            Icon(
                                painter = painterResource(android.R.drawable.arrow_down_float), // Using system arrow
                                contentDescription = "Down",
                                modifier = Modifier.size(16.dp).clickable { /* Decrement logic? */ },
                                tint = Color.Gray
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onCreditChange("") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Clear", color = Color.White, fontSize = 12.sp)
                    }

                    listOf(500, 1000, 2000).forEach { amount ->
                        Button(
                            onClick = {
                                val current = creditAmountStr.toDoubleOrNull() ?: 0.0
                                onCreditChange((current + amount).toString())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("+$amount", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onCreditChange(if (difference > 0) difference.toString() else "0")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text("Auto Balance", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_dialog_info),
                        contentDescription = "Info",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Credit points will be deducted from your account when the trade is accepted.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TradeSummaryCard(
    yourItemValue: Double,
    creditPoints: Double,
    totalOffer: Double,
    theirItemValue: Double,
    isShort: Boolean,
    shortAmount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9) // Light Green
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Trade Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Your Item(s):", color = Color.Black)
                Text("Rs$yourItemValue", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Credit Points:", color = Color.Black)
                Text("Rs$creditPoints", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFF4CAF50), thickness = 2.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total Offer:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontSize = 18.sp
                )
                Text(
                    "Rs$totalOffer",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Their Item:", color = Color.Black)
                Text("Rs$theirItemValue", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Status:", color = Color(0xFF2E7D32))
                if (isShort) {
                    Text(
                        "⚠ Short by Rs${String.format("%.1f", shortAmount)}",
                        color = Color(0xFFFF9800), // Orange
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "✅ Balanced",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProductWantCard(product: ProductModel, owner: UserModel?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = "Product",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                error = painterResource(R.drawable.placeholderimage),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "You want:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Rs${product.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (owner != null) {
                    Text(
                        text = "From: ${owner.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionInstructionCard(selectedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_items), // Ensure this resource exists or use default
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Select items to offer",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "$selectedCount items selected",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.ic_items),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You don't have any barter items available.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun BarterMessageSection(
    message: String,
    onMessageChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Message to Owner (Optional)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("Add a message for the owner...") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun UserProductCard(
    product: ProductModel,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Product image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = "Your product",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                error = painterResource(R.drawable.placeholderimage),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // Product details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "Rs${product.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BarterActionButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    selectedCount: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF009688), // Teal color from screenshot
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = "Send Barter Request",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
