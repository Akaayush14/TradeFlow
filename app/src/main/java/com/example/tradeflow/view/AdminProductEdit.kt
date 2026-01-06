package com.example.tradeflow.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.tradeflow.model.NotificationModel
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.NotificationRepoImpl
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.utils.ImageUtils
import com.example.tradeflow.viewmodel.NotificationViewModel
import com.example.tradeflow.viewmodel.ProductViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

class AdminProductEdit : ComponentActivity() {
    private lateinit var imageUtils: ImageUtils
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productId = intent.getStringExtra("product_id") ?: ""

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            selectedImageUri = uri
        }

        setContent {
            ProductEditScreen(
                productId = productId,
                onImageClick = { imageUtils.launchImagePicker() },
                selectedImageUri = selectedImageUri,
                onBackClick = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ProductEditScreen(
    productId: String,
    onImageClick: () -> Unit,
    selectedImageUri: Uri?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl()) }

    val allProducts by productViewModel.allProducts.collectAsState()
    val product = remember(allProducts) {
        allProducts?.find { it.productId == productId }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnlistDialog by remember { mutableStateOf(false) }

    // Load product data
    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.name
            description = it.description
            price = it.price.toString()
            location = it.location
            category = it.category
            type = it.type
        }
    }

    // Image URLs list
    val imageUrls = remember(product, selectedImageUri) {
        val urls = mutableListOf<String>()
        if (selectedImageUri != null) {
            urls.add(selectedImageUri.toString())
        } else {
            product?.let {
                if (it.imageUrl.isNotEmpty()) urls.add(it.imageUrl)
                if (it.imageUrl2.isNotEmpty()) urls.add(it.imageUrl2)
                if (it.imageUrl3.isNotEmpty()) urls.add(it.imageUrl3)
                if (it.imageUrl4.isNotEmpty()) urls.add(it.imageUrl4)
            }
        }
        urls.ifEmpty { listOf("") }
    }

    val pagerState = rememberPagerState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Edit Product",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // Image Carousel Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFF5F5F5))
            ) {
                HorizontalPager(
                    count = imageUrls.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrls[page].isEmpty()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_items),
                                contentDescription = "Add Image",
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray
                            )
                        } else {
                            AsyncImage(
                                model = imageUrls[page],
                                contentDescription = "Product Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.ic_items)
                            )
                        }
                    }
                }

                // Pager Indicator
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    activeColor = Greenish,
                    inactiveColor = Color.LightGray
                )
            }

            // Product Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    maxLines = 5
                )

                // Price
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    singleLine = true
                )

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    singleLine = true
                )

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    singleLine = true
                )

                // Type
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (Barter/Rent/Both)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Greenish,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Apply Changes Button
                Button(
                    onClick = {
                        product?.let { currentProduct ->
                            val priceValue = price.toDoubleOrNull() ?: 0.0
                            val updatedProduct = currentProduct.copy(
                                name = name,
                                description = description,
                                price = priceValue,
                                location = location,
                                category = category,
                                type = type
                            )

                            productViewModel.updateProduct(updatedProduct) { success, message ->
                                if (success) {
                                    val notification = NotificationModel(
                                        message = "Product '${name}' has been updated successfully",
                                        type = "product_updated",
                                        itemId = productId
                                    )
                                    notificationViewModel.addNotification(notification) { _, _ -> }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Apply changes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Un-list/List Product Button
                Button(
                    onClick = { showUnlistDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (product?.isListed == true) Color(0xFFFF9800) else DarkGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (product?.isListed == true) "Un-list Product" else "List Product",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Delete Button
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Un-list/List Dialog
    if (showUnlistDialog) {
        AlertDialog(
            onDismissRequest = { showUnlistDialog = false },
            title = {
                Text(if (product?.isListed == true) "Un-list Product" else "List Product")
            },
            text = {
                Text("Are you sure you want to ${if (product?.isListed == true) "un-list" else "list"} this product?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        product?.let {
                            val newListedStatus = !it.isListed
                            productViewModel.listProduct(productId, newListedStatus) { success, message ->
                                if (success) {
                                    val notification = NotificationModel(
                                        message = "Product '${name}' has been ${if (newListedStatus) "listed" else "unlisted"} successfully",
                                        type = if (newListedStatus) "product_listed" else "product_unlisted",
                                        itemId = productId
                                    )
                                    notificationViewModel.addNotification(notification) { _, _ -> }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    productViewModel.getAllProduct()
                                    showUnlistDialog = false
                                    onBackClick()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    showUnlistDialog = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (product?.isListed == true) Color(0xFFFF9800) else DarkGreen
                    )
                ) {
                    Text(if (product?.isListed == true) "Un-list" else "List", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showUnlistDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Product") },
            text = {
                Text("Are you sure you want to delete this product? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.deleteProduct(productId) { success, message ->
                            if (success) {
                                val notification = NotificationModel(
                                    message = "Product '${name}' has been deleted successfully",
                                    type = "product_deleted",
                                    itemId = productId
                                )
                                notificationViewModel.addNotification(notification) { _, _ -> }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showDeleteDialog = false
                                onBackClick()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}