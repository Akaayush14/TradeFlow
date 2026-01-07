package com.example.tradeflow.view

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.example.tradeflow.model.UserModel
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.tradeflow.R


// Add this annotation to use experimental Material 3 APIs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserExploreScreen() {
    val context = LocalContext.current
    val activity = context as Activity

    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val productViewModel: ProductViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userViewModel: UserViewModel = remember { UserViewModel(UserRepoImpl()) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    val allProducts by productViewModel.allProducts.collectAsState()
    val userData by userViewModel.users.collectAsState()
    val userPoints = userData?.points ?: 0L

    val filteredProducts = allProducts.filter { product ->
        val matchesTab = when (selectedTab) {
            "Rent" -> product.type == "Rent"
            "Trade" -> product.type == "Barter"
            else -> true
        }
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch && !product.isDeleted
    }

    Scaffold(
        topBar = {
            Column {
                // Points display row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Greenish)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable {
                                val intent = Intent(activity, UserPointsActivity::class.java)
                                activity.startActivity(intent)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$userPoints Points",
                                color = White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Use >",
                                color = White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                // Search bar
                TradeFlowTopBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search items...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon",
                                    tint = White
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear Search",
                                            tint = White
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                                disabledContainerColor = Color.White.copy(alpha = 0.15f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = White,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            textStyle = TextStyle(
                                color = White,
                                fontSize = 16.sp
                            ),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true)
                    },
                    actions = {
                        IconButton(onClick = {
                            val intent = Intent(activity, UserSetting::class.java)
                            activity.startActivity(intent)
                        }) {
                            // Use Icons.Default.Menu or your custom resource
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }

    ) { paddingValues ->
        // The main content uses the padding provided by the Scaffold (for the top bar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply the padding here
                .background(color = White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf("All","Rent","Barter")
                tabs.forEach { tabName ->
                    val isSelected = selectedTab == tabName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(if (isSelected) White else Color(0xFFF0F0F0))
                            .clickable { selectedTab = tabName },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            color = if (isSelected) Color.Black else Color.Gray
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    ExploreItemCard(
                        product = product,
                        onClick = {
                            val intent = Intent(context, UserItemDetails::class.java)
                            intent.putExtra("productId", product.productId)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreItemCard(product: ProductModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Product Image with Heart Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.LightGray)
            ) {
                val displayImage = if (product.imageUrl.isNotEmpty()) {
                    product.imageUrl
                } else if (product.imageUrls.isNotEmpty()) {
                    product.imageUrls.first()
                } else {
                    ""
                }

                if (displayImage.isNotEmpty()) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholderimage),
                        error = painterResource(R.drawable.placeholderimage)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.placeholderimage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Heart Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        .clickable { /* Handle favorite */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Product Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Status Row: Type (Left) and Status Badge (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.type,
                        fontSize = 12.sp,
                        color = Greenish,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val statusColor = when (product.status) {
                        "Available" -> Greenish
                        "Completed" -> Color(0xFF2196F3) // Blue
                        "Pending" -> Color(0xFFFF9800)   // Orange
                        else -> Color.Gray
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.status,
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Name
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Description (Single line)
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price and Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rs${product.price}",
                        fontSize = 14.sp,
                        color = Greenish, // Using Primary Color for Price
                        fontWeight = FontWeight.SemiBold
                    )

                    if (product.location.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = product.location,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 60.dp) // Limit width to prevent overflow
                            )
                        }
                    }
                }
            }
        }
    }
}
