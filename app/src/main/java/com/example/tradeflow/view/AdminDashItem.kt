package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import kotlin.math.abs
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import com.example.tradeflow.R
import com.example.tradeflow.model.NotificationModel
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.NotificationRepoImpl
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.NotificationViewModel
import com.example.tradeflow.viewmodel.ProductViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

class AdminDashItem : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val targetTab = intent.getIntExtra("target_tab", 0)
        setContent {
            AdminItemScreen(
                initialTab = targetTab,
                onBackClick = {
                    val intent = Intent(this, AdminDashExp::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminItemScreen(initialTab: Int = 0, onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) } // 0 for Listed Items, 1 for Unlisted Items

    // Notification view model for unread count
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    
    LaunchedEffect(Unit) {
        notificationViewModel.getUnreadCount()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    // Optional: Only show back if needed, or if this screen is pushed on a stack.
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Items",
                            color = White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tab Row for Listed Items and Unlisted Items
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
                            "Listed Items",
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
                            "Unlisted Items",
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
                    0 -> ListedItemsContent()
                    1 -> UnlistedItemsContent()
                }
            }
        }
    }
}

@Composable
fun ListedItemsContent() {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val allProducts by productViewModel.allProducts.collectAsState()

    var showUnlistDialog by remember { mutableStateOf<ProductModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ProductModel?>(null) }
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    val hasInternet = isInternetAvailableItem(context)
    val listedProducts = allProducts?.filter { it.isListed } ?: emptyList()

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                productViewModel.getAllProduct()
                delay(800)
                isRefreshing = false
            }
        }
    ) {
        if (!hasInternet) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_internet),
                    contentDescription = null
                )
            }
        } else if (listedProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_data),
                    contentDescription = null
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = listedProducts,
                    key = { it.productId }
                ) { product ->
                    ItemCardItem(
                        product = product,
                        onListClick = { },
                        onUnlistClick = { showUnlistDialog = product },
                        onDeleteClick = { showDeleteDialog = product }
                    )
                }
            }
        }
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
                                // Create notification
                                val notification = NotificationModel(
                                    message = "Item '${product.name}' has been unlisted successfully",
                                    type = "item_unlisted",
                                    itemId = product.productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
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
                                // Create notification
                                val notification = NotificationModel(
                                    message = "Item '${product.name}' has been deleted successfully",
                                    type = "item_deleted",
                                    itemId = product.productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
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
fun UnlistedItemsContent() {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }
    val allProducts by productViewModel.allProducts.collectAsState()

    var showListDialog by remember { mutableStateOf<ProductModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ProductModel?>(null) }
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    val hasInternet = isInternetAvailableItem(context)
    val unlistedProducts = allProducts?.filter { !it.isListed } ?: emptyList()

    PullToRefreshLayout(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                productViewModel.getAllProduct()
                delay(800)
                isRefreshing = false
            }
        }
    ) {
        if (!hasInternet) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_internet),
                    contentDescription = null
                )
            }
        } else if (unlistedProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.no_data),
                    contentDescription = null
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = unlistedProducts,
                    key = { it.productId }
                ) { product ->
                    ItemCardItem(
                        product = product,
                        onListClick = { showListDialog = product },
                        onUnlistClick = { },
                        onDeleteClick = { showDeleteDialog = product }
                    )
                }
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
                                // Create notification
                                val notification = NotificationModel(
                                    message = "Item '${product.name}' has been listed successfully",
                                    type = "item_listed",
                                    itemId = product.productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
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
                                // Create notification
                                val notification = NotificationModel(
                                    message = "Item '${product.name}' has been deleted successfully",
                                    type = "item_deleted",
                                    itemId = product.productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
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

fun isInternetAvailableItem(context: android.content.Context): Boolean {
    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}
@Composable
fun ItemCardItem(
    product: ProductModel,
    onListClick: () -> Unit,
    onUnlistClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable {
                val intent = Intent(context, AdminItemDetailActivity::class.java)
                intent.putExtra("productId", product.productId)
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!product.isListed) Color(0xFFFFEBEE) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image on the left
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
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
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_items),
                        placeholder = painterResource(R.drawable.ic_items)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_items),
                        contentDescription = "Product Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Content on the right
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Product name
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Category
                Text(
                    text = "Category: ${product.category}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Price
                Text(
                    text = "Price: ${product.price}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Type
                Text(
                    text = "Type: ${product.type}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Description
                Text(
                    text = "Description: ${product.description}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Unlisted status
                if (!product.isListed) {
                    Text(
                        text = "UNLISTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (product.isListed) {
                        // Unlist Button
                        Button(
                            onClick = onUnlistClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            modifier = Modifier.height(40.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_visibility_off_24),
                                contentDescription = "Unlist",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unlist",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    } else {
                        // List Button
                        Button(
                            onClick = onListClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            modifier = Modifier.height(40.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_visibility_24),
                                contentDescription = "List",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "List",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Delete Button
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.height(40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgedNotificationIconItem(
    unreadCount: Int,
    iconPainter: Painter,
    contentDescription: String
) {
    Box {
        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            tint = Color.White
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .offset(x = 12.dp, y = (-8).dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun RefreshSpinnerItem() {
    val transition = rememberInfiniteTransition(label = "refresh")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "rotation"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = "Refreshing",
                tint = DarkGreen,
                modifier = Modifier.size(20.dp).rotate(rotation.value)
            )
        }
    }
}
