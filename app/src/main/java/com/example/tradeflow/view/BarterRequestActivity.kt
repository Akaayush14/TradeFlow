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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.res.painterResource

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Product they want
            productData?.let { product ->
                ProductWantCard(product = product, owner = ownerData)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Selection instruction
            SelectionInstructionCard(selectedCount = selectedItems.size)

            Spacer(modifier = Modifier.height(16.dp))

            // User's products to offer
            if (isLoadingProducts) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (userProducts.isEmpty()) {
                EmptyStateCard()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message section
            BarterMessageSection(
                message = message,
                onMessageChange = { message = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            BarterActionButton(
                isEnabled = selectedItems.isNotEmpty() && !isLoading,
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
                                offerProducts = selectedProductList
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
                    "Your barter request has been sent to the owner. You'll be notified when they respond.",
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
                    Text(
                        "OK",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "OK",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun ProductWantCard(product: ProductModel, owner: UserModel?) {
    val allImages = remember(product) {
        val list = mutableListOf<String>()
        if (product.imageUrl.isNotEmpty()) list.add(product.imageUrl)
        if (product.imageUrls.isNotEmpty()) list.addAll(product.imageUrls)
        if (product.imageUrl2.isNotEmpty()) list.add(product.imageUrl2)
        if (product.imageUrl3.isNotEmpty()) list.add(product.imageUrl3)
        if (product.imageUrl4.isNotEmpty()) list.add(product.imageUrl4)
        list.filter { it.isNotEmpty() }.distinct()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "You want to barter for:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Product",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    error = painterResource(R.drawable.placeholderimage),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Owner: ${owner?.name ?: "Unknown"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Value: Rs${product.price}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = "Swap",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (allImages.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "More images:",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allImages) { imgUrl ->
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Sub image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(4.dp)
                                ),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            error = painterResource(R.drawable.placeholderimage)
                        )
                    }
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Swap",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Select items to offer in exchange",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (selectedCount == 0)
                        "Choose from your available barter items below"
                    else
                        "$selectedCount item${if (selectedCount > 1) "s" else ""} selected",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Barter Items Available",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You don't have any available barter items to offer.\nAdd some items to your profile first!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
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
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onSelectionChange(!isSelected) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxSize()
                )
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rs${product.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BarterMessageSection(
    message: String,
    onMessageChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Message to Owner (Optional)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = {
                    Text(
                        "Add a message for the owner...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
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
            containerColor = MaterialTheme.colorScheme.primary,
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Send Barter Request",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (selectedCount > 0) {
                    Text(
                        text = "$selectedCount item${if (selectedCount > 1) "s" else ""} offered",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}