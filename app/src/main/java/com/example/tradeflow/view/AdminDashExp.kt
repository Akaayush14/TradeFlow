package com.example.tradeflow.view
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminDashExp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminExp()
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminExp() {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 for items, 1 for user
    var backPressedTime by remember { mutableLongStateOf(0L) }

    // Handle back button press
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            // Exit app if pressed twice within 2 seconds
            if (context is ComponentActivity) {
                context.finishAffinity()
            }
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "Click again to quit", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Search",
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                    }
                },
                title = {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(50.dp))
                    )
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AdminSettings::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Greenish) {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_explore),
                            contentDescription = "Explore",
                            tint = Color.White
                        )
                    },
                    label = { Text("Explore", color = Color.White) }
                )

                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = {
                        val intent = Intent(context, AdminDashUser::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_user),
                            contentDescription = "User",
                            tint = Color.White
                        )
                    },
                    label = { Text("User", color = Color.White) }
                )
                NavigationBarItem(
                    selected = selectedIndex == 3,
                    onClick = {
                        val intent = Intent(context, AdminDashItem::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_items),
                            contentDescription = "Items",
                            tint = Color.White
                        )
                    },
                    label = { Text("Items", color = Color.White) }
                )

                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = {
                        val intent = Intent(context, AdminProfile::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_profile),
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    },
                    label = { Text("Profile", color = Color.White) }
                )


            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tab Row for Items and User
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Gray,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DarkGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "items",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 0) Color.Black else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "user",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 1) Color.Black else Color.Gray
                        )
                    }
                )
            }

            // Content based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> ItemsContent()
                    1 -> UsersContent()
                }
            }
        }
    }
}

@Composable
fun ItemsContent() {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val allProducts by productViewModel.allProducts.collectAsState()
    
    var showListDialog by remember { mutableStateOf<ProductModel?>(null) }
    var showUnlistDialog by remember { mutableStateOf<ProductModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ProductModel?>(null) }
    
    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }
    
    // Items List - Just fetch and display data
    if (allProducts.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No items yet",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = allProducts ?: emptyList(),
                key = { it.productId }
            ) { product ->
                ItemCardExp(
                    product = product,
                    onListClick = { showListDialog = product },
                    onUnlistClick = { showUnlistDialog = product },
                    onDeleteClick = { showDeleteDialog = product }
                )
            }
        }
    }
    
    // List Dialog
    showListDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showListDialog = null },
            title = { Text("List Item") },
            text = { 
                Text("Are you sure you want to list ${product.name}?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.listProduct(product.productId, true) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showListDialog = null
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showListDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("List", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showListDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    
    // Unlist Dialog
    showUnlistDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showUnlistDialog = null },
            title = { Text("Unlist Item") },
            text = { 
                Text("Are you sure you want to unlist ${product.name}?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.listProduct(product.productId, false) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showUnlistDialog = null
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showUnlistDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Unlist", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showUnlistDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    
    // Delete Dialog
    showDeleteDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Item") },
            text = { 
                Text("Are you sure you want to delete ${product.name}?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.deleteProduct(product.productId) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                productViewModel.getAllProduct()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            showDeleteDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ItemCardExp(
    product: ProductModel,
    onListClick: () -> Unit,
    onUnlistClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!product.isListed) Color(0xFFFFEBEE) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Price: $${product.price}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${product.type}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location: ${product.location}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (!product.isListed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "UNLISTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.isListed) {
                    // Show Unlist button when listed
                    Button(
                        onClick = onUnlistClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Unlist",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // Show List button (green) when unlisted
                    Button(
                        onClick = onListClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "List",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
                // Always show Delete button
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UsersContent() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val allUsers by userViewModel.allUsers.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<UserModel?>(null) }
    var showBlockDialog by remember { mutableStateOf<UserModel?>(null) }
    var showRestrictDialog by remember { mutableStateOf<UserModel?>(null) }
    
    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }
    
    if (allUsers.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No users yet",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = allUsers ?: emptyList(),
                key = { it.userId }
            ) { user ->
                UserCard(
                    user = user,
                    onDeleteClick = { showDeleteDialog = user },
                    onBlockClick = { showBlockDialog = user },
                    onRestrictClick = { showRestrictDialog = user }
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${user.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.deleteUser(user.userId) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                userViewModel.getAllUser()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            showDeleteDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    
    // Block/Unblock Dialog
    showBlockDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showBlockDialog = null },
            title = { Text(if (user.isBlocked) "Unblock User" else "Block User") },
            text = { 
                Text("Are you sure you want to ${if (user.isBlocked) "unblock" else "block"} ${user.name}?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.blockUser(user.userId, !user.isBlocked) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showBlockDialog = null
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showBlockDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) DarkGreen else Color.Red
                    )
                ) {
                    Text(if (user.isBlocked) "Unblock" else "Block", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showBlockDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    
    // Restrict/Unrestrict Dialog
    showRestrictDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showRestrictDialog = null },
            title = { Text(if (user.isRestricted) "Unrestrict User" else "Restrict User") },
            text = { 
                Text("Are you sure you want to ${if (user.isRestricted) "unrestrict" else "restrict"} ${user.name}?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.restrictUser(user.userId, !user.isRestricted) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showRestrictDialog = null
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showRestrictDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRestricted) DarkGreen else Color(0xFFFF9800) // Orange
                    )
                ) {
                    Text(if (user.isRestricted) "Unrestrict" else "Restrict", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showRestrictDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: UserModel,
    onDeleteClick: () -> Unit,
    onBlockClick: () -> Unit,
    onRestrictClick: () -> Unit
) {
    // Determine card color: blocked = light red, restricted = light orange, normal = white
    val cardColor = when {
        user.isBlocked -> Color(0xFFFFEBEE) // Light red
        user.isRestricted -> Color(0xFFFFF3E0) // Light orange
        else -> Color.White
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Name
            Text(
                text = user.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Email
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = Color.Gray
            )
            // Status labels
            if (user.isBlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BLOCKED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
            if (user.isRestricted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RESTRICTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800) // Orange
                )
            }
            // Buttons at the bottom
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Block/Unblock button
                Button(
                    onClick = onBlockClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) DarkGreen else Color.Red
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (user.isBlocked) "Unblock" else "Block",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                // Restrict/Unrestrict button
                Button(
                    onClick = onRestrictClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRestricted) DarkGreen else Color(0xFFFF9800) // Orange
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (user.isRestricted) "Unrestrict" else "Restrict",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                // Delete button
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}