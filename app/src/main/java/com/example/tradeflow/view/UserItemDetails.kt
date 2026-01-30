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
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.ReviewViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.material.icons.filled.LocationOn
import android.net.Uri

import com.example.tradeflow.model.RequestModel
import com.example.tradeflow.repository.UserNotificationRepoImpl
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserItemDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeWrapper {
                ItemDetailsScreen()
            }
        }
    }
}

// FRIEND'S IMPROVED CONTAINER TAG
@Composable
fun ContainerTag(text: String, color: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = textColor)
    }
}

// FRIEND'S IMPROVED REVIEW ITEM WITH PROFILE IMAGES
@Composable
fun ReviewItem(username: String, userImage: String, rating: Int, comment: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Reviewer Image - FROM FRIEND'S CODE
            if (userImage.isNotEmpty()) {
                AsyncImage(
                    model = userImage,
                    contentDescription = "Reviewer Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    error = painterResource(R.drawable.ic_launcher_foreground)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Reviewer Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary, // KEEP YOUR COLOR
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant // KEEP YOUR COLOR
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val productId = activity?.intent?.getStringExtra("productId") ?: ""

    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val reviewViewModel = remember { ReviewViewModel(ReviewRepoImpl()) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    // State variables - ADD FROM FRIEND'S CODE
    val product by productViewModel.product.collectAsState()
    val owner by userViewModel.users.collectAsState()
    val reviews by reviewViewModel.reviews.collectAsState()
    val loading by productViewModel.loading.collectAsState()
    val ownerProducts by productViewModel.allProducts.collectAsState() // ADDED FROM FRIEND'S CODE

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // FROM FRIEND'S CODE: Owner Stats
    var ownerAverageRating by remember { mutableFloatStateOf(0f) }
    var ownerReviewCount by remember { mutableIntStateOf(0) }

    // FROM FRIEND'S CODE: Reviewer Details Cache
    val reviewerMap = remember { mutableStateMapOf<String, UserModel>() }

    // EARLY RETURN / RENT PAYMENT STATE
    var activeRequest by remember { mutableStateOf<RequestModel?>(null) }
    var showRentDialog by remember { mutableStateOf(false) }
    var rentAmountInput by remember { mutableStateOf("") }
    
    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId)
            reviewViewModel.getReviewsByProductId(productId)
        }
    }

    LaunchedEffect(product) {
        product?.let { p ->
            // Check for rental expiration
            if (p.status == "Rented" && p.rentalEndDate > 0 && System.currentTimeMillis() > p.rentalEndDate) {
                productViewModel.updateProductStatus(p.productId, "Available") { success, _ ->
                    if (success) {
                        productViewModel.getProductById(p.productId)
                    }
                }
            }
            
            // FETCH ACTIVE REQUEST IF EXISTS
            if (p.activeRequestId.isNotEmpty()) {
                UserNotificationRepoImpl().getRequestById(p.activeRequestId) { success, _, req ->
                    if (success) {
                        activeRequest = req
                        // Pre-fill rent amount if calculating
                        if (req?.rentalStartDate != null && req.rentalStartDate > 0) {
                            val days = ((System.currentTimeMillis() - req.rentalStartDate) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
                            val estimatedRent = days * req.rentalPricePerDay
                            rentAmountInput = String.format("%.2f", estimatedRent)
                        }
                    }
                }
            }

            if (p.ownerId.isNotEmpty()) {
                userViewModel.getUserById(p.ownerId) { success, _, user ->
                    // User data is handled in the ViewModel
                }
                // FROM FRIEND'S CODE: Fetch owner's products to calculate stats
                productViewModel.getProductsByOwner(p.ownerId)
            }
        }
    }

    // FROM FRIEND'S CODE: Calculate Owner Stats when ownerProducts are loaded
    LaunchedEffect(ownerProducts) {
        if (ownerProducts.isNotEmpty()) {
            val productIds = ownerProducts.map { it.productId }
            reviewViewModel.getOwnerStats(productIds) { avg, count ->
                ownerAverageRating = avg
                ownerReviewCount = count
            }
        }
    }

    // FROM FRIEND'S CODE: Fetch Reviewer Details when reviews are loaded
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

    val isOwner = currentUserId == product?.ownerId
    // FROM FRIEND'S CODE: Check if item is completed
    val isCompleted = product?.status?.trim()
        ?.equals("Completed", ignoreCase = true) ?: false
    val isRented = product?.status?.trim()
        ?.equals("Rented", ignoreCase = true) ?: false

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Item Details",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            painterResource(id = R.drawable.outline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            val isRenter = activeRequest?.requesterId == currentUserId
            
            // RENTER ACTIONS
            if (isRenter && !isCompleted && currentUserId.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                     Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                         if (activeRequest?.status == "CONFIRMED") {
                             Button(
                                onClick = {
                                    activeRequest?.let { req ->
                                        UserNotificationRepoImpl().updateRequestStatus(req.requestId, "RETURN_REQUESTED") { success, _ ->
                                            if (success) {
                                                activeRequest = activeRequest?.copy(status = "RETURN_REQUESTED")
                                                Toast.makeText(context, "Return requested sent to owner", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                             ) {
                                 Text("Return Early")
                             }

                             OutlinedButton(
                                onClick = {
                                    val ownerId = product?.ownerId ?: ""
                                    if (ownerId.isNotEmpty()) {
                                        val intent = Intent(context, UserDashboard::class.java).apply {
                                            putExtra("openChat", true)
                                            putExtra("chatUserId", ownerId)
                                        }
                                        context.startActivity(intent)
                                        activity?.finish()
                                    } else {
                                        Toast.makeText(context, "Owner not available", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                             ) {
                                 Icon(
                                     Icons.Default.Email,
                                     contentDescription = "Message",
                                     modifier = Modifier.size(20.dp)
                                 )
                                 Spacer(modifier = Modifier.width(8.dp))
                                 Text("Message")
                             }
                         } else if (activeRequest?.status == "PAYMENT_PENDING") {
                             Button(
                                onClick = {
                                     val intent = Intent(context, FinalPaymentActivity::class.java)
                                     intent.putExtra("requestId", activeRequest?.requestId)
                                     intent.putExtra("amount", activeRequest?.finalRentAmount)
                                     context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                             ) {
                                 Text("Pay Rent: Rs ${activeRequest?.finalRentAmount}")
                             }
                         } else if (activeRequest?.status == "RETURN_REQUESTED") {
                             Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                                 Text("Waiting for Owner to calculate rent...", fontWeight = FontWeight.SemiBold)
                             }
                         } else {
                              OutlinedButton(
                                onClick = {
                                    val ownerId = product?.ownerId ?: ""
                                    if (ownerId.isNotEmpty()) {
                                        val intent = Intent(context, UserDashboard::class.java).apply {
                                            putExtra("openChat", true)
                                            putExtra("chatUserId", ownerId)
                                        }
                                        context.startActivity(intent)
                                        activity?.finish()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Message Owner") }
                         }
                    }
                }
            }
            // OWNER ACTIONS
            else if (isOwner && !isCompleted) {
                 if (activeRequest?.status == "RETURN_REQUESTED") {
                      Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                          Row(modifier = Modifier.padding(16.dp)) {
                              Button(
                                onClick = { showRentDialog = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                              ) { Text("Calculate Rent") }
                          }
                      }
                 } else if (activeRequest?.status == "RENT_PAID") {
                      Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                          Row(modifier = Modifier.padding(16.dp)) {
                              Button(
                                onClick = {
                                    activeRequest?.let { req ->
                                        UserNotificationRepoImpl().updateRequestStatus(req.requestId, "COMPLETED") { success, _ ->
                                            if (success) {
                                                 productViewModel.updateProductStatus(req.productId, "Available") { _, _ -> 
                                                     Toast.makeText(context, "Deposit Returned & Item Available", Toast.LENGTH_SHORT).show()
                                                     activity?.finish()
                                                 }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                              ) { Text("Confirm Deposit Returned") }
                          }
                      }
                 } else {
                     // Default Owner View (Edit/Delete or just Message logic if any)
                 }
            }
            // DEFAULT USER (Not Renter, Not Owner, or No Active Request)
            else if (!isOwner && currentUserId.isNotEmpty() && product != null && !isCompleted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // CHAT UPDATE FROM FRIEND'S CODE - START
                                val ownerId = product?.ownerId ?: ""
                                if (ownerId.isNotEmpty()) {
                                    val intent = Intent(context, UserDashboard::class.java).apply {
                                        putExtra("openChat", true)
                                        putExtra("chatUserId", ownerId)
                                    }
                                    context.startActivity(intent)
                                    activity?.finish() // Close current activity after opening chat
                                } else {
                                    Toast.makeText(context, "Owner not available", Toast.LENGTH_SHORT).show()
                                }
                                // CHAT UPDATE FROM FRIEND'S CODE - END
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Message",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message")
                        }

                        Button(
                            onClick = {
                                if (!isRented) {
                                    if (owner == null) {
                                        errorMessage = "Owner information not available. Please try again."
                                        showErrorDialog = true
                                    } else {
                                        if (product?.type == "Rent") {
                                            val intent = Intent(context, RentalRequestActivity::class.java)
                                            // Pass product ID and owner ID instead of objects
                                            intent.putExtra("productId", product?.productId ?: "")
                                            intent.putExtra("ownerId", product?.ownerId ?: "")
                                            context.startActivity(intent)
                                        } else {
                                            val intent = Intent(context, BarterRequestActivity::class.java)
                                            // Pass product ID and owner ID instead of objects
                                            intent.putExtra("productId", product?.productId ?: "")
                                            intent.putExtra("ownerId", product?.ownerId ?: "")
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRented) Color.Gray else MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isRented
                        ) {
                            Text(
                                if (isRented) "Currently Rented" else (if (product?.type == "Rent") "Rent Now" else "Barter Now"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (loading || (product == null && productId.isNotEmpty())) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Product not found",
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
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
                                error = painterResource(R.drawable.placeholderimage),
                                onSuccess = {
                                    Log.d(
                                        "TF_UI_IMAGE",
                                        "Details main image loaded productId=${product?.productId} page=$page"
                                    )
                                },
                                onError = {
                                    Log.e(
                                        "TF_UI_IMAGE",
                                        "Details main image failed productId=${product?.productId} url=${allImages[page]}"
                                    )
                                }
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

                    // Image Indicator (e.g., 1/4)
                    if (allImages.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${allImages.size}",
                                color = MaterialTheme.colorScheme.surface,
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
                                        color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
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
                    product?.let { productItem ->
                        Text(
                            text = productItem.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
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
                                color = MaterialTheme.colorScheme.primary
                            )

                            // FROM FRIEND'S CODE: Improved ContainerTag with background colors
                            ContainerTag(
                                text = productItem.type,
                                color = if (productItem.type == "Rent") Color(0xFFE0F7FA) else Color(0xFFFFF3E0),
                                textColor = if (productItem.type == "Rent") Color(0xFF006064) else Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Product Location
                        if (!productItem.location.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(productItem.location)}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = productItem.location,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = "Status: ${when {
                                productItem.status == "Completed" -> "Completed"
                                productItem.status == "Rented" -> "Rented"
                                !productItem.isListed -> "Pending"
                                else -> "Available"
                            }}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Owner Info
                        Text(
                            text = "Owner",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // FROM FRIEND'S CODE: Show owner profile image
                            if (owner?.profileImageUrl.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                        contentDescription = "Owner Profile",
                                        modifier = Modifier.size(40.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = owner?.profileImageUrl,
                                    contentDescription = "Owner Profile",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        ),
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
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    // FROM FRIEND'S CODE: Show calculated owner rating
                                    Text(
                                        text = String.format("%.1f (%d reviews)", ownerAverageRating, ownerReviewCount),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Description
                        Text(
                            text = "Description",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product?.description ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. Reviews
                        Text(
                            text = "Reviews (${reviews.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (reviews.isEmpty()) {
                            Text(
                                "No reviews yet.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // FROM FRIEND'S CODE: Enhanced reviews with profile images
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

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // RENT CALCULATION DIALOG
        if (showRentDialog) {
            AlertDialog(
                onDismissRequest = { showRentDialog = false },
                title = { Text("Calculate Final Rent", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter the total rent amount to be paid by the renter:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rentAmountInput,
                            onValueChange = { rentAmountInput = it },
                            label = { Text("Amount (Rs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = rentAmountInput.toDoubleOrNull()
                            if (amount != null && amount >= 0) {
                                activeRequest?.let { req ->
                                    val updates = mapOf(
                                        "status" to "PAYMENT_PENDING",
                                        "finalRentAmount" to amount,
                                        "returnDate" to System.currentTimeMillis()
                                    )
                                    UserNotificationRepoImpl().updateRequestDetails(req.requestId, updates) { success, _ ->
                                        if (success) {
                                            activeRequest = activeRequest?.copy(
                                                status = "PAYMENT_PENDING",
                                                finalRentAmount = amount
                                            )
                                            showRentDialog = false
                                            Toast.makeText(context, "Rent request sent to renter", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to update request", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Invalid Amount", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Send Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRentDialog = false }) {
                        Text("Cancel")
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
}