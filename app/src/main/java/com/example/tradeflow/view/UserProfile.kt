package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userId = intent.getStringExtra("userId")
            UserProfileScreen(
                onBackClick = { finish() },
                onEditProduct = {},
                showEditSuccess = false,
                onSnackbarShown = {},
                profileUserId = userId
            )
        }
    }
}

enum class ListingType { BARTER, RENTAL, BOTH }
enum class ListingStatus { ALL, AVAILABLE, PENDING, COMPLETED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProduct: (ProductModel) -> Unit = {},
    showEditSuccess: Boolean = false,
    onSnackbarShown: () -> Unit = {},
    profileUserId: String? = null
) {
    val userViewModel: UserViewModel = remember { UserViewModel(UserRepoImpl()) }
    val productViewModel: ProductViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val context = LocalContext.current

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    val targetUserId = profileUserId ?: currentUserId

    // Use collectAsState() for StateFlow
    val userData by userViewModel.users.collectAsState()
    val allProducts by productViewModel.allProducts.collectAsState()

    // State for the selected listing type and status
    var selectedTab by remember { mutableStateOf(ListingType.BOTH) }
    var selectedStatus by remember { mutableStateOf(ListingStatus.ALL) }

    LaunchedEffect(targetUserId) {
        if (targetUserId.isNotEmpty()) {
            Log.d("ProfileScreen", "Fetching user data for userId: $targetUserId")
            userViewModel.getUserById(targetUserId) { _, _, _ -> }
            productViewModel.getProductsByOwner(targetUserId)
        } else {
            Log.d("ProfileScreen", "userId is empty: $targetUserId")
        }
    }

    // Log user data changes for debugging
    LaunchedEffect(userData) {
        Log.d("ProfileScreen", "User data updated: $userData")
        Log.d("ProfileScreen", "User name: ${userData?.name}")
        Log.d("ProfileScreen", "User email: ${userData?.email}")
    }

    // Memoized filtering logic
    val filteredListings = remember(selectedTab, selectedStatus, allProducts) {
        val typeFiltered = when (selectedTab) {
            ListingType.BARTER -> allProducts.filter { it.type == "Barter" && it.isDeleted != true }
            ListingType.RENTAL -> allProducts.filter { it.type == "Rent" && it.isDeleted != true }
            ListingType.BOTH -> allProducts.filter { it.isDeleted != true }
        }

        when (selectedStatus) {
            ListingStatus.ALL -> typeFiltered
            ListingStatus.AVAILABLE -> typeFiltered.filter { it.status == "Available" }
            ListingStatus.PENDING -> typeFiltered.filter { it.status == "Pending" }
            ListingStatus.COMPLETED -> typeFiltered.filter { it.status == "Completed" }
        }
    }

    // Get user display info - prioritize database data over Firebase Auth display name
    val userName = userData?.name ?: currentUser?.displayName ?: "User"
    val userEmail = userData?.email ?: currentUser?.email ?: ""
    val userDisplayEmail = userEmail

    // Debug logging
    Log.d("ProfileScreen", "Final userName: $userName")
    Log.d("ProfileScreen", "userData?.name: ${userData?.name}")
    Log.d("ProfileScreen", "currentUser?.displayName: ${currentUser?.displayName}")

    // Show loading state while user data is being fetched
    val isLoading = userData == null
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(showEditSuccess) {
        if (showEditSuccess) {
            snackbarHostState.showSnackbar("Item updated successfully")
            onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    Text(
                        "Profile",
                        color = White
                    )
                },
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        // Single scrollable column so header + listings scroll together
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                ProfileHeaderSection(
                    userName = userName,
                    userDisplayEmail = userDisplayEmail,
                    barterCount = allProducts.count { it.type == "Barter" },
                    rentalCount = allProducts.count { it.type == "Rent" },
                    completedCount = allProducts.count { it.status == "Completed" },
                    isLoading = isLoading,
                    onEditProfileClick = {
                        try {
                            // Navigate to Edit Profile screen
                            val intent = Intent(context, UserSettingEditProfile::class.java)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("UserProfile", "Error navigating to Edit Profile: ${e.message}")
                            // Show error message to user if navigation fails
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Unable to open Edit Profile")
                            }
                        }
                    }
                )

                // Segmented Tabs for Barter / Rental / Both listings
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(ListingType.BARTER, ListingType.RENTAL, ListingType.BOTH).forEach { type ->
                            val isSelected = selectedTab == type
                            val title = when (type) {
                                ListingType.BARTER -> "Barter"
                                ListingType.RENTAL -> "Rental"
                                ListingType.BOTH -> "Both"
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(if (isSelected) Greenish else Color.Transparent)
                                    .clickable { selectedTab = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) White else Color.Black,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Status filter Chips (All, Available, Pending, Completed)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ListingStatus.values()) { status ->
                        val isSelected = selectedStatus == status
                        val label = when (status) {
                            ListingStatus.ALL -> "All"
                            ListingStatus.AVAILABLE -> "Available"
                            ListingStatus.PENDING -> "Pending"
                            ListingStatus.COMPLETED -> "Completed"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) Greenish else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedStatus = status }
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Listing section title
                Text(
                    text = when (selectedTab) {
                        ListingType.BARTER -> "My Barter Listings"
                        ListingType.RENTAL -> "My Rental Listings"
                        ListingType.BOTH -> "My Listings"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Display products from database
            val data = filteredListings
            if (data.isEmpty()) {
                item {
                    Text(
                        text = "No items match the current filters.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(data) { product ->
                    val isOwner = product.ownerId == targetUserId
                    ProductItemCard(
                        product = product,
                        onClick = {
                            val intent = Intent(context, UserItemDetails::class.java)
                            intent.putExtra("productId", product.productId)
                            context.startActivity(intent)
                        },
                        onEdit = { onEditProduct(it) },
                        isOwner = isOwner,
                        onDeleteRequest = { toDelete ->
                            if (toDelete.status != "Available") return@ProductItemCard
                            val id = toDelete.productId
                            if (id.isNotEmpty()) {
                                productViewModel.deleteProduct(id) { success, message ->
                                    if (success) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Item deleted successfully")
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                                }
                            }
                        },
                        onMarkTradedRequest = { toComplete ->
                            if (toComplete.status != "Available") return@ProductItemCard
                            val updated = toComplete.copy(status = "Completed", completedAt = System.currentTimeMillis())
                            productViewModel.updateProduct(updated) { success, message ->
                                if (success) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Traded successfully")
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    userName: String,
    userDisplayEmail: String,
    barterCount: Int,
    rentalCount: Int,
    completedCount: Int,
    isLoading: Boolean = false,
    onEditProfileClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Profile Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF26A69A)) // Teal-ish color
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        // Edit Icon
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00695C)) // Darker Teal
                                .border(1.dp, White, CircleShape)
                                .align(Alignment.BottomEnd)
                                .clickable { onEditProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = "Edit",
                                tint = White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = userDisplayEmail,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(
                        count = barterCount.toString(),
                        label = "Barter Items",
                        backgroundColor = Color(0xFFE0F2F1), // Light Teal
                        textColor = Color(0xFF00695C),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        count = rentalCount.toString(),
                        label = "Rental Items",
                        backgroundColor = Color(0xFFE8F5E9), // Light Green
                        textColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        count = completedCount.toString(),
                        label = "Completed",
                        backgroundColor = Color(0xFFFFFDE7), // Light Yellow
                        textColor = Color(0xFFF57F17),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(
    count: String,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductModel,
    onClick: () -> Unit,
    onEdit: (ProductModel) -> Unit,
    isOwner: Boolean,
    onDeleteRequest: (ProductModel) -> Unit,
    onMarkTradedRequest: (ProductModel) -> Unit
) {
    // Determine background color based on status and type
    val cardBackgroundColor = when {
        product.status == "Completed" -> Color(0xFFFFFDE7) // Light Yellow for Completed
        product.type == "Barter" -> Color(0xFFE0F2F1) // Light Teal for Barter
        product.type == "Rent" -> Color(0xFFE8F5E9) // Light Green for Rent
        else -> Color(0xFFF8F8F8) // Default fallback
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Display Area (Left Side)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholderimage),
                        error = painterResource(R.drawable.placeholderimage)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Product Image",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF0288D1)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Status and Identity (Top Right/Center)
            Column(modifier = Modifier.weight(1f)) {
                // Availability Badge at Top
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (product.status) {
                                "Available" -> Color(0xFF00897B) // Teal
                                "Pending" -> Color(0xFFFF9800) // Orange
                                "Completed" -> Color(0xFF2196F3) // Blue
                                else -> Color(0xFF9E9E9E) // Gray
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = product.status,
                        color = White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(2.dp))

                // Description
                Text(
                    product.description.takeIf { it.isNotEmpty() } ?: "No description",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Transaction Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Trade Type Badge (Left)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (product.type == "Barter") Color(0xFF00897B)
                                else Color(0xFF795548) // Brown for Rent
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.type,
                            color = White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Price (Right)
                    Text(
                        text = if (product.type == "Rent") {
                            "Rs ${String.format("%.2f", product.price)} / Day"
                        } else {
                            "Rs ${String.format("%.2f", product.price)}"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Location Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Location Icon from drawable
                    Image(
                        painter = painterResource(R.drawable.location_on), // You'll need to add this drawable
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.location.takeIf { it.isNotEmpty() } ?: "No location specified",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Gray divider line below location
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons (Bottom Row)
                if (isOwner) {
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    var showDeleteBlocked by remember { mutableStateOf(false) }
                    var showMarkConfirm by remember { mutableStateOf(false) }
                    var showMarkBlocked by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Edit Button
                        if (product.status != "Completed") {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onEdit(product) },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Edit",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Image(
                                        painter = painterResource(R.drawable.edit),
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Mark as Traded Button
                        if (product.status != "Completed") {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            showMarkConfirm = product.status == "Available"
                                            showMarkBlocked = product.status == "Pending" || product.status == "Completed"
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = " Traded",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Image(
                                        painter = painterResource(R.drawable.traded),
                                        contentDescription = "Traded",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Delete Button
                        if (product.status != "Completed") {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            showDeleteConfirm = product.status == "Available"
                                            showDeleteBlocked = product.status == "Pending" || product.status == "Completed"
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Delete",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Image(
                                        painter = painterResource(R.drawable.delete),
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (product.status == "Completed") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Traded successfully",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete item?") },
                            text = { Text("This item will be permanently removed from your listings.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteConfirm = false
                                    onDeleteRequest(product)
                                }) {
                                    Text("Delete", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    if (showMarkConfirm) {
                        AlertDialog(
                            onDismissRequest = { showMarkConfirm = false },
                            title = { Text("Mark item as traded?") },
                            text = { Text("This will move the item to completed and remove it from active listings.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showMarkConfirm = false
                                    onMarkTradedRequest(product)
                                }) {
                                    Text("Mark as Traded")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showMarkConfirm = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    if (showMarkBlocked) {
                        AlertDialog(
                            onDismissRequest = { showMarkBlocked = false },
                            title = { Text("Not allowed") },
                            text = { Text("You can mark this item as traded only after completion.") },
                            confirmButton = {
                                TextButton(onClick = { showMarkBlocked = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    if (showDeleteBlocked) {
                        AlertDialog(
                            onDismissRequest = { showDeleteBlocked = false },
                            title = { Text("Delete not allowed") },
                            text = { Text("You can't delete this item while a trade is in progress.") },
                            confirmButton = {
                                TextButton(onClick = { showDeleteBlocked = false }) {
                                    Text("Mark as completed")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteBlocked = false }) {
                                    Text("Cancel trade")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
