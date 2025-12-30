package com.example.tradeflow.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.ReviewRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.ReviewViewModel
import com.example.tradeflow.viewmodel.UserViewModel

class ItemDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ItemDetailsScreen()
        }
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

    val product by productViewModel.products.observeAsState()
    val owner by userViewModel.users.observeAsState()
    val reviews by reviewViewModel.reviews.observeAsState(emptyList())
    val loading by productViewModel.loading.observeAsState(false)

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId)
            reviewViewModel.getReviewsByProductId(productId)
        }
    }

    LaunchedEffect(product) {
        product?.ownerId?.let { ownerId ->
            if (ownerId.isNotEmpty()) {
                userViewModel.getUserById(ownerId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details", color = White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(White)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Item Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (product!!.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = product!!.imageUrl,
                            contentDescription = "Item Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Placeholder",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // 2. Item Title, Price, Type
                    Text(
                        text = product!!.name,
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
                            text = "$${product!!.price}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Greenish
                        )
                        ContainerTag(
                            text = product!!.type,
                            color = if (product!!.type == "Rent") Color(0xFFE0F7FA) else Color(0xFFFFF3E0),
                            textColor = if (product!!.type == "Rent") Color(0xFF006064) else Color(0xFFE65100)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: ${product!!.status}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
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
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "4.8 (24 reviews)", // Placeholder rating for user
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
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
                        text = product!!.description,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Reviews
                    Text(
                        text = "Reviews (${reviews?.size ?: 0})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (reviews.isNullOrEmpty()) {
                        Text("No reviews yet.", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        reviews!!.forEach { review ->
                            ReviewItem(
                                username = review.userName,
                                rating = review.rating.toInt(),
                                comment = review.comment
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 6. Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { Toast.makeText(context, "Opening Chat...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Email,
                                contentDescription = null, tint = White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message", color = White)
                        }

                        Button(
                            onClick = { Toast.makeText(context, "Requesting ${product!!.type}...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Greenish),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${product!!.type} Now", color = White)
                        }
                    }
                }
            }
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
            Text(text = username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
