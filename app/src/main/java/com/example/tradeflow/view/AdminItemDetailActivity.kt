package com.example.tradeflow.view

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
import com.example.tradeflow.repository.ReviewRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.ReviewViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.content.Intent

import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.repository.UserNotificationRepoImpl

class AdminItemDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminItemDetailsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminItemDetailsScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val productId = activity?.intent?.getStringExtra("productId") ?: ""

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val reviewViewModel = remember { ReviewViewModel(ReviewRepoImpl()) }
    val userNotificationRepo = remember { UserNotificationRepoImpl() }

    // State variables
    val product by productViewModel.product.collectAsState()
    val owner by userViewModel.users.collectAsState()
    val reviews by reviewViewModel.reviews.collectAsState()
    val loading by productViewModel.loading.collectAsState()
    val ownerProducts by productViewModel.allProducts.collectAsState()

    // Reviewer Details Cache
    val reviewerMap = remember { mutableStateMapOf<String, UserModel>() }

    // Owner Stats
    var ownerAverageRating by remember { mutableFloatStateOf(0f) }
    var ownerReviewCount by remember { mutableIntStateOf(0) }

    // Editing states
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedCategory by remember { mutableStateOf("") }
    var editedLocation by remember { mutableStateOf("") }
    var editedType by remember { mutableStateOf("") }
    // Note: Price is not editable as per request

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId)
            reviewViewModel.getReviewsByProductId(productId)
        }
    }

    // Fetch Reviewer Details when reviews are loaded
    LaunchedEffect(reviews) {
        reviews.forEach { review ->
            if (review.userId.isNotEmpty() && !reviewerMap.containsKey(review.userId)) {
                // Use fetchUser to avoid updating the main 'users' state which tracks the item owner
                userViewModel.fetchUser(review.userId) { user ->
                    if (user != null) {
                        reviewerMap[review.userId] = user
                    }
                }
            }
        }
    }

    LaunchedEffect(product) {
        product?.let {
            editedName = it.name
            editedDescription = it.description
            editedCategory = it.category
            editedLocation = it.location
            editedType = it.type
            
            if (it.ownerId.isNotEmpty()) {
                userViewModel.getUserById(it.ownerId) { _, _, _ ->
                    // Owner loaded
                }
                // Fetch owner's products to calculate stats
                productViewModel.getProductsByOwner(it.ownerId)
            }
        }
    }

    LaunchedEffect(ownerProducts) {
        if (ownerProducts.isNotEmpty()) {
            val productIds = ownerProducts.map { it.productId }
            reviewViewModel.getOwnerStats(productIds) { avg, count ->
                ownerAverageRating = avg
                ownerReviewCount = count
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Item Details", color = White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            painterResource(id = R.drawable.outline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            // Save Logic
                            val updatedProduct = product?.copy(
                                name = editedName,
                                description = editedDescription,
                                category = editedCategory,
                                location = editedLocation,
                                type = editedType
                            )
                            if (updatedProduct != null) {
                                productViewModel.updateProduct(updatedProduct) { success, message ->
                                    if (success) {
                                        // Notify User
                                        val notification = UserNotificationModel(
                                            type = "ADMIN_UPDATE",
                                            title = "Product Updated by Admin",
                                            message = "Your product '${updatedProduct.name}' details have been updated by an admin.",
                                            senderName = "Admin",
                                            receiverId = updatedProduct.ownerId,
                                            productId = updatedProduct.productId,
                                            productName = updatedProduct.name,
                                            productImage = updatedProduct.imageUrl,
                                            isRead = false,
                                            createdAt = System.currentTimeMillis()
                                        )
                                        userNotificationRepo.createNotification(notification) { _, _ -> }

                                        Toast.makeText(context, "Product Updated Successfully", Toast.LENGTH_SHORT).show()
                                        isEditing = false
                                        // Refresh product data
                                        productViewModel.getProductById(productId)
                                    } else {
                                        Toast.makeText(context, "Update Failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Save, contentDescription = "Save", tint = White)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish
                )
            )
        }
    ) { paddingValues ->
        if (loading || (product == null && productId.isNotEmpty())) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Greenish)
            }
        } else if (product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Product not found")
            }
        } else {
            val allImages = remember(product) {
                product?.let { productItem ->
                    val list = mutableListOf<String>()
                    if (productItem.imageUrl.isNotEmpty()) list.add(productItem.imageUrl)
                    if (productItem.imageUrls.isNotEmpty()) list.addAll(productItem.imageUrls)
                    if (productItem.imageUrl2.isNotEmpty()) list.add(productItem.imageUrl2)
                    if (productItem.imageUrl3.isNotEmpty()) list.add(productItem.imageUrl3)
                    if (productItem.imageUrl4.isNotEmpty()) list.add(productItem.imageUrl4)
                    list.filter { it.isNotEmpty() }.distinct()
                } ?: emptyList()
            }

            val pagerState = rememberPagerState(pageCount = { if (allImages.isEmpty()) 1 else allImages.size })
            val scope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(White)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Item Image (Main Display) - Not editable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        if (allImages.isNotEmpty()) {
                            AsyncImage(
                                model = allImages[page],
                                contentDescription = "Item Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.placeholderimage),
                                error = painterResource(R.drawable.placeholderimage)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.placeholderimage),
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Image Indicator
                    if (allImages.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${allImages.size}",
                                color = White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Image Gallery (Thumbnails)
                if (allImages.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allImages.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .border(
                                        width = if (pagerState.currentPage == index) 2.dp else 1.dp,
                                        color = if (pagerState.currentPage == index) Greenish else Color.Gray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.placeholderimage),
                                    error = painterResource(R.drawable.placeholderimage)
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    if (isEditing) {
                        // Editable Fields
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Price (Read-only)
                        OutlinedTextField(
                            value = product?.price.toString(),
                            onValueChange = { },
                            label = { Text("Price (Not Editable)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Category
                        OutlinedTextField(
                            value = editedCategory,
                            onValueChange = { editedCategory = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Type
                        OutlinedTextField(
                            value = editedType,
                            onValueChange = { editedType = it },
                            label = { Text("Type (Rent/Barter)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Location
                        OutlinedTextField(
                            value = editedLocation,
                            onValueChange = { editedLocation = it },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        OutlinedTextField(
                            value = editedDescription,
                            onValueChange = { editedDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                    } else {
                        // Display Mode
                        product?.let { productItem ->
                            Text(
                                text = productItem.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (productItem.type == "Rent") {
                                        "Rs${productItem.price} / Day"
                                    } else {
                                        "Rs${productItem.price}"
                                    },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Greenish
                                )

                                ContainerTag(
                                    text = productItem.type,
                                    color = if (productItem.type == "Rent") Color(0xFFE0F7FA) else Color(0xFFFFF3E0),
                                    textColor = if (productItem.type == "Rent") Color(0xFF006064) else Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Category: ${productItem.category}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                             Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Location: ${productItem.location}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                             Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Status: ${productItem.status}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Owner Info (Read-only)
                            Text(
                                text = "Owner",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        product?.ownerId?.let { ownerId ->
                                            if (ownerId.isNotEmpty()) {
                                                val intent = Intent(context, AdminUserDetailActivity::class.java)
                                                intent.putExtra("userId", ownerId)
                                                context.startActivity(intent)
                                            }
                                        }
                                    }
                            ) {
                                if (owner?.profileImageUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                        contentDescription = "Owner Profile",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color.Gray, CircleShape)
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = owner?.profileImageUrl,
                                        contentDescription = "Owner Profile",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color.Gray, CircleShape)
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                        error = painterResource(R.drawable.ic_launcher_foreground)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = owner?.name ?: "Loading...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = String.format("%.1f (%d reviews)", ownerAverageRating, ownerReviewCount),
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Description
                            Text(
                                text = "Description",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product?.description ?: "",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    if (!isEditing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Reviews (Read-only)
                        Text(
                            text = "Reviews (${reviews.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (reviews.isEmpty()) {
                            Text("No reviews yet.", fontSize = 14.sp, color = Color.Gray)
                        } else {
                            reviews.forEach { review ->
                                val reviewer = reviewerMap[review.userId]
                                ReviewItem(
                                    username = reviewer?.name ?: review.userName,
                                    userImage = reviewer?.profileImageUrl ?: "",
                                    rating = review.rating.toInt(),
                                    comment = review.comment
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
