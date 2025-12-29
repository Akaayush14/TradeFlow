
package com.example.tradeflow.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
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


// Enums to manage the state of the tabs
enum class ListingType { BARTER, RENTAL, BOTH }
enum class ListingStatus { ALL, AVAILABLE, PENDING, COMPLETED }

data class ListingItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: String,
    val imageResId: Int,
    val type: ListingType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBackClick: () -> Unit = {}) {
    val userViewModel: UserViewModel = remember { UserViewModel(UserRepoImpl()) }
    val productViewModel: ProductViewModel = remember { ProductViewModel(ProductRepoImpl()) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    // Observe user data from ViewModel
    val userData by userViewModel.users.observeAsState<UserModel?>()

    // Observe products from ViewModel
    val allProducts by productViewModel.allProducts.observeAsState()

    // State for the selected listing type and status
    var selectedTab by remember { mutableStateOf(ListingType.BOTH) }
    var selectedStatus by remember { mutableStateOf(ListingStatus.ALL) }


    // Fetch user data when screen loads
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d("ProfileScreen", "Fetching user data for userId: $userId")
            userViewModel.getUserById(userId)
            productViewModel.getProductsByOwner(userId)
        }
    }

    // Log user data changes for debugging
    LaunchedEffect(userData) {
        Log.d("ProfileScreen", "User data updated: $userData")
        Log.d("ProfileScreen", "User name: ${userData?.name}")
    }

    // Memoized filtering logic
    val filteredListings = remember(selectedTab, selectedStatus, allProducts) {
        val typeFiltered = when (selectedTab) {
            ListingType.BARTER -> allProducts.orEmpty().filter { it.type == "Barter" }
            ListingType.RENTAL -> allProducts.orEmpty().filter { it.type == "Rent" }
            ListingType.BOTH -> allProducts.orEmpty()
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
    val userDisplayEmail = userEmail  // Display full email instead of @username

    // Show loading state while user data is being fetched
    val isLoading = userData == null && userId.isNotEmpty()



    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        },
        containerColor = White
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
                    barterCount = allProducts.orEmpty().count { it.type == "Barter" },
                    rentalCount = allProducts.orEmpty().count { it.type == "Rent" },
                    completedCount = allProducts.orEmpty().count { it.status == "Completed" },
                    isLoading = isLoading
                )

                // Tabs for Barter / Rental / Both listings
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        ListingType.BARTER -> 0
                        ListingType.RENTAL -> 1
                        ListingType.BOTH -> 2
                    },
                    containerColor = White
                ) {
                    Tab(
                        selected = selectedTab == ListingType.BARTER,
                        onClick = { selectedTab = ListingType.BARTER },
                        text = { Text("Barter", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == ListingType.RENTAL,
                        onClick = { selectedTab = ListingType.RENTAL },
                        text = { Text("Rental", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == ListingType.BOTH,
                        onClick = { selectedTab = ListingType.BOTH },
                        text = { Text("Both", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                // Status filter tabs (All, Available, Pending, Completed)
                TabRow(
                    selectedTabIndex = when (selectedStatus) {
                        ListingStatus.ALL -> 0
                        ListingStatus.AVAILABLE -> 1
                        ListingStatus.PENDING -> 2
                        ListingStatus.COMPLETED -> 3
                    },
                    containerColor = Color(0xFFF5F5F5),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedStatus == ListingStatus.ALL,
                        onClick = { selectedStatus = ListingStatus.ALL },
                        text = { Text("All", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    )
                    Tab(
                        selected = selectedStatus == ListingStatus.AVAILABLE,
                        onClick = { selectedStatus = ListingStatus.AVAILABLE },
                        text = { Text("Available", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    )
                    Tab(
                        selected = selectedStatus == ListingStatus.PENDING,
                        onClick = { selectedStatus = ListingStatus.PENDING },
                        text = { Text("Pending", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    )
                    Tab(
                        selected = selectedStatus == ListingStatus.COMPLETED,
                        onClick = { selectedStatus = ListingStatus.COMPLETED },
                        text = { Text("Completed", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    )
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
                    ProductItemCard(product = product, onClick = { /* Handle item click */ })
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
    isLoading: Boolean = false
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start  // Changed from CenterHorizontally to Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture on the left
            Box {
                Box(
                    modifier = Modifier
                        .size(80.dp)  // Made slightly smaller for left alignment
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD))
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier
                            .size(50.dp)  // Adjusted icon size
                            .align(Alignment.Center),
                        tint = Color(0xFF0288D1)
                    )
                }

                // Pencil icon over avatar (bottom-right)
                Box(
                    modifier = Modifier
                        .size(24.dp)  // Made smaller
                        .clip(CircleShape)
                        .background(Greenish)
                        .align(Alignment.BottomEnd)
                        .clickable {
                            // TODO: replace SettingsActivity with your actual settings activity class name if different
                            // context.startActivity(Intent(context, SettingsActivity::class.java))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Edit Profile / Settings",
                        tint = White,
                        modifier = Modifier.size(12.dp)  // Made smaller
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and email on the right of profile picture
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isLoading) "Loading..." else userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,  // Slightly smaller for left layout
                    color = Color.Black
                )
                Text(userDisplayEmail, fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats row with better styling
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    label = "Barter Items",
                    value = barterCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                // Divider between stats
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color(0xFFE0E0E0))
                )
                ProfileStat(
                    label = "Rental Items",
                    value = rentalCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                // Divider between stats
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color(0xFFE0E0E0))
                )
                // Review Rating Stat
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLoading) "0.0" else String.format("%.1f", 4.5), // TODO: Use real rating
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "Rating",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ListingItemCard(item: ListingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Image Display Area ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                // Use the drawable resource ID provided in the data model
                Image(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = "Listing Image for ${item.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }


            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (item.type == ListingType.BARTER) Color(0xFFE0F2F1) else Color(0xFFEDE7F6)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.type == ListingType.BARTER) "Barter" else "Rental",
                            fontSize = 11.sp,
                            color = if (item.type == ListingType.BARTER) Greenish else Color(0xFF5E35B1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.description, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Text(
                text = item.price,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
        // Divider line
        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.LightGray))
    }
}


@Composable
fun ProductItemCard(product: ProductModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Display Area
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    // TODO: Load image from URL using Coil or Glide
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Product Image",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF0288D1)
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

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (product.type == "Barter") Color(0xFF00897B) else Color(0xFF7B1FA2)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            product.type,
                            fontSize = 10.sp,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(product.description, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
            }

            Text(
                text = "$${String.format("%.2f", product.price)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
        // Divider line
        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.LightGray))
    }
}
