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
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.res.painterResource

class BarterRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BarterRequestScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarterRequestScreen() {
    val context = LocalContext.current
    val activity = context as? BarterRequestActivity

    // Get passed data from intent
    val product = activity?.intent?.getSerializableExtra("product") as? ProductModel
    val owner = activity?.intent?.getSerializableExtra("owner") as? UserModel

    // Initialize ViewModels
    val notificationViewModel = remember {
        UserNotificationViewModel(UserNotificationRepoImpl())
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
    LaunchedEffect(allProducts) {
        userProducts = allProducts.filter {
            it.isDeleted != true &&
                    it.status == "Available" &&
                    it.type == "Barter"
        }
        isLoadingProducts = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Barter Request",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Product they want
            product?.let {
                ProductWantCard(product = it, owner = owner)
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
                    CircularProgressIndicator(color = Greenish)
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
                    val productData = product
                    val ownerData = owner
                    val userData = currentUserData

                    // Null checks before sending request
                    if (productData == null) {
                        errorMessage = "Product information not available"
                        showErrorDialog = true
                    } else if (ownerData == null) {
                        errorMessage = "Owner information not available"
                        showErrorDialog = true
                    } else if (userData == null) {
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
                                product = productData,
                                owner = ownerData,
                                requester = userData,
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
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Your barter request has been sent to the owner. You'll be notified when they respond.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        activity?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Greenish
                    )
                ) {
                    Text("OK")
                }
            }
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
                    color = Color.Red
                )
            },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Greenish
                    )
                ) {
                    Text("OK")
                }
            }
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
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "You want to barter for:",
                fontSize = 12.sp,
                color = Color.Gray,
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
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Owner: ${owner?.name ?: "Unknown"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Value: $${product.price}",
                        color = Greenish,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = "Swap",
                    tint = Greenish,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (allImages.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "More images:",
                    fontSize = 10.sp,
                    color = Color.Gray,
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
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
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
            containerColor = Color(0xFFFFF3CD)
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
                tint = Color(0xFF856404)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Select items to offer in exchange",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF856404)
                )
                Text(
                    text = if (selectedCount == 0)
                        "Choose from your available barter items below"
                    else
                        "$selectedCount item${if (selectedCount > 1) "s" else ""} selected",
                    fontSize = 12.sp,
                    color = Color(0xFF856404)
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
            containerColor = Color(0xFFF8F9FA)
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
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You don't have any available barter items to offer.\nAdd some items to your profile first!",
                fontSize = 12.sp,
                color = Color.Gray
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
                color = if (isSelected) Greenish else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0FFF0) else White
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
            IconButton(
                onClick = { onSelectionChange(!isSelected) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected) Greenish else Color.LightGray,
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
                error = painterResource(R.drawable.placeholderimage)
            )

            // Product details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${product.price}",
                    color = Greenish,
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
        shape = RoundedCornerShape(12.dp)
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
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = { Text("Add a message for the owner...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Greenish,
                    cursorColor = Greenish
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
            containerColor = Greenish,
            disabledContainerColor = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = White
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Send Barter Request",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (selectedCount > 0) {
                    Text(
                        text = "$selectedCount item${if (selectedCount > 1) "s" else ""} offered",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}