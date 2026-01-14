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
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.ReviewViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.content.Intent

class UserItemDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ItemDetailsScreen()
        }
    }
}

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

@Composable
fun ReviewItem(username: String, rating: Int, comment: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = username,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < rating) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Text(text = comment, fontSize = 14.sp, color = Color.Gray)
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



    // State variables
    val product by productViewModel.product.collectAsState()
    val owner by userViewModel.users.collectAsState()
    val reviews by reviewViewModel.reviews.collectAsState()
    val loading by productViewModel.loading.collectAsState()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId)
            reviewViewModel.getReviewsByProductId(productId)
        }
    }

    LaunchedEffect(product) {
        product?.ownerId?.let { ownerId ->
            if (ownerId.isNotEmpty()) {
                userViewModel.getUserById(ownerId) { success, _, user ->
                    // User data is handled in the ViewModel
                }
            }
        }
    }

    // Check if current user is the owner
    val isOwner = currentUserId == product?.ownerId

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Item Details", color = White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            painterResource(id = R.drawable.outline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish
                )
            )
        },
        bottomBar = {
            // Only show request button if user is not the owner and product is loaded
            if (!isOwner && currentUserId.isNotEmpty() && product != null) {
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
                                Toast.makeText(context, "Opening Chat...", Toast.LENGTH_SHORT).show()
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
                                // Check if owner data is available
                                if (owner == null) {
                                    errorMessage = "Owner information not available. Please try again."
                                    showErrorDialog = true
                                } else {
                                    // Launch appropriate activity based on product type
                                    if (product?.type == "Rent") {
                                        // Launch Rental Request Activity
                                        val intent = Intent(context, RentalRequestActivity::class.java)
                                        intent.putExtra("product", product)
                                        intent.putExtra("owner", owner)
                                        context.startActivity(intent)
                                    } else {
                                        // Launch Barter Request Activity
                                        val intent = Intent(context, BarterRequestActivity::class.java)
                                        intent.putExtra("product", product)
                                        intent.putExtra("owner", owner)
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Greenish
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (product?.type) {
                                    "Barter" -> "Barter Now"
                                    "Rent" -> "Rent Now"
                                    else -> "Send Request"
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
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
                // 1. Item Image (Main Display)
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
                            text = "Status: ${productItem.status}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Owner Info
                        Text(
                            text = "Owner",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                        text = "4.8 (24 reviews)",
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

                        // 4. Description
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

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. Reviews
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
                                ReviewItem(
                                    username = review.userName,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Greenish)
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}