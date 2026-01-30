package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.viewmodel.ProductViewModel
import com.example.tradeflow.viewmodel.UserNotificationViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.painterResource

class RentalRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeWrapper {
                RentalRequestScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalRequestScreen() {
    val context = LocalContext.current
    val activity = context as? RentalRequestActivity

    // Get passed data from intent
    val productId = activity?.intent?.getStringExtra("productId") ?: ""
    val ownerId = activity?.intent?.getStringExtra("ownerId") ?: ""

    // Initialize ViewModels
    val notificationViewModel = remember {
        UserNotificationViewModel(UserNotificationRepoImpl(), ProductRepoImpl())
    }
    val productViewModel = remember {
        ProductViewModel(ProductRepoImpl())
    }
    val userViewModel = remember {
        UserViewModel(UserRepoImpl())
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    // State management
    var currentUserData by remember { mutableStateOf<UserModel?>(null) }
    var selectedStartDate by remember { mutableStateOf<Long?>(null) }
    var selectedEndDate by remember { mutableStateOf<Long?>(null) }
    var rentalDays by remember { mutableStateOf(0) }
    var totalPrice by remember { mutableStateOf(0.0) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load product and owner data
    var productData by remember { mutableStateOf<ProductModel?>(null) }
    var ownerData by remember { mutableStateOf<UserModel?>(null) }

    // Load product details
    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId)
        }
    }

    // Load owner details
    LaunchedEffect(ownerId) {
        if (ownerId.isNotEmpty()) {
            userViewModel.getUserById(ownerId) { success, _, user ->
                if (success) {
                    ownerData = user
                }
            }
        }
    }

    // Load current user data
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getUserById(currentUserId) { success, _, user ->
                if (success && user != null) {
                    currentUserData = user
                }
            }
        }
    }

    // Observe product data
    val productDetails by productViewModel.product.collectAsState()
    LaunchedEffect(productDetails) {
        productData = productDetails
    }

    // Calculate rental details when dates change
    LaunchedEffect(selectedStartDate, selectedEndDate, productData) {
        if (selectedStartDate != null && selectedEndDate != null) {
            rentalDays = ((selectedEndDate!! - selectedStartDate!!) / (1000 * 60 * 60 * 24)).toInt() + 1
            totalPrice = (productData?.price ?: 0.0) * rentalDays
        }
    }

    // Date pickers
    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            selectedStartDate = calendar.timeInMillis
            // Reset end date if it's before start date
            if (selectedEndDate != null && selectedEndDate!! < selectedStartDate!!) {
                selectedEndDate = null
            }
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    val endDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            selectedEndDate = calendar.timeInMillis
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    // Disable past dates
    startDatePicker.datePicker.minDate = System.currentTimeMillis() - 1000
    if (selectedStartDate != null) {
        endDatePicker.datePicker.minDate = selectedStartDate!! + 1000
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rental Request",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Product Summary
            item {
                productData?.let { product ->
                    ProductSummaryCard(product = product)
                }
            }

            // Duration Selection and Price
            item {
                DurationSelectionSection(
                    selectedStartDate = selectedStartDate,
                    selectedEndDate = selectedEndDate,
                    rentalDays = rentalDays,
                    totalPrice = totalPrice,
                    pricePerDay = productData?.price ?: 0.0,
                    onStartDateClick = { startDatePicker.show() },
                    onEndDateClick = {
                        if (selectedStartDate != null) {
                            endDatePicker.show()
                        }
                    }
                )
            }

            // Security Deposit (If applicable)
            if ((productData?.securityDeposit ?: 0.0) > 0) {
                item {
                    SecurityDepositCard(depositAmount = productData?.securityDeposit ?: 0.0)
                }
            }

            // Payment Summary (If rental days > 0)
            if (rentalDays > 0) {
                item {
                    PaymentSummaryCard(
                        rentalDays = rentalDays,
                        rentalFee = totalPrice,
                        securityDeposit = productData?.securityDeposit ?: 0.0
                    )
                }
            }

            // Message
            item {
                MessageSection(
                    message = message,
                    onMessageChange = { message = it }
                )
            }

            // Action Button
            item {
                ActionButton(
                    isEnabled = selectedStartDate != null && selectedEndDate != null && !isLoading,
                    isLoading = isLoading,
                    rentalDays = rentalDays,
                    totalPrice = totalPrice,
                    onClick = {
                        val product = productData
                        val owner = ownerData
                        val user = currentUserData

                        if (product == null) {
                            errorMessage = "Product information not available"
                            showErrorDialog = true
                        } else if (owner == null) {
                            errorMessage = "Owner information not available"
                            showErrorDialog = true
                        } else if (user == null) {
                            errorMessage = "User information not available. Please try again."
                            showErrorDialog = true
                        } else if (selectedStartDate == null || selectedEndDate == null) {
                            errorMessage = "Please select start and end dates"
                            showErrorDialog = true
                        } else {
                            isLoading = true
                            notificationViewModel.createItemRequest(
                                product = product,
                                owner = owner,
                                requester = user,
                                requestType = "RENT",
                                message = message,
                                rentalStartDate = selectedStartDate!!,
                                rentalEndDate = selectedEndDate!!,
                                rentalPricePerDay = product.price,
                                securityDeposit = product.securityDeposit
                            ) { success, msg ->
                                isLoading = false
                                if (success) {
                                    showSuccessDialog = true
                                } else {
                                    errorMessage = msg
                                    showErrorDialog = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                activity?.finish()
            },
            title = {
                Text(
                    "Request Sent!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Your rental request has been sent to the owner. You'll be notified when they respond.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        activity?.finish()
                    },
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

@Composable
fun ProductSummaryCard(product: ProductModel) {
    val allImages = remember(product) {
        val list = mutableListOf<String>()
        if (product.imageUrl.isNotEmpty()) list.add(product.imageUrl)
        if (product.imageUrls.isNotEmpty()) list.addAll(product.imageUrls)
        if (product.imageUrl2.isNotEmpty()) list.add(product.imageUrl2)
        if (product.imageUrl3.isNotEmpty()) list.add(product.imageUrl3)
        if (product.imageUrl4.isNotEmpty()) list.add(product.imageUrl4)
        list.filter { url -> url.isNotEmpty() }.distinct()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Product",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    error = painterResource(R.drawable.placeholderimage),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs${product.price}/day",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            if (allImages.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allImages) { imgUrl ->
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Sub image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(4.dp)
                                ),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            error = painterResource(R.drawable.placeholderimage)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DurationSelectionSection(
    selectedStartDate: Long?,
    selectedEndDate: Long?,
    rentalDays: Int,
    totalPrice: Double,
    pricePerDay: Double,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select Rental Duration",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Start Date
            DateSelectionRow(
                label = "Start Date",
                date = selectedStartDate,
                onClick = onStartDateClick,
                isEnabled = true
            )

            // End Date
            DateSelectionRow(
                label = "End Date",
                date = selectedEndDate,
                onClick = onEndDateClick,
                isEnabled = selectedStartDate != null
            )

            // Duration Summary (Green Card)
            if (rentalDays > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE0F2F1) // Light teal
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Duration:",
                                color = Color(0xFF00695C)
                            )
                            Text(
                                "$rentalDays days",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00695C)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Price per day:",
                                color = Color(0xFF00695C)
                            )
                            Text(
                                "Rs${String.format("%.2f", pricePerDay)}",
                                color = Color(0xFF00695C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Divider(
                            color = Color(0xFF80CBC4),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Price:",
                                color = Color(0xFF004D40),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Rs${String.format("%.2f", totalPrice)}",
                                color = Color(0xFF004D40),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityDepositCard(depositAmount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Color(0xFFFFC107), // Amber/Yellow border
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFDE7) // Light yellow bg (optional) or Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security Deposit",
                    tint = Color(0xFFFFA000), // Amber 700
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security Deposit Required",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            // Explanation box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8E1) // Very light amber
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF795548))) {
                            append("Why deposit? ")
                        }
                        append("This refundable amount protects the owner's item. It will be returned to you after the rental period ends and the item is returned in good condition.")
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037), // Brownish text
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 18.sp
                )
            }

            // Amount display
            OutlinedTextField(
                value = "Rs${String.format("%.2f", depositAmount)}",
                onValueChange = {},
                readOnly = true,
                label = { Text("Deposit Amount:", fontWeight = FontWeight.Medium) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFFFC107),
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedTextColor = Color(0xFFFF6F00),
                    focusedTextColor = Color(0xFFFF6F00)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            // Refund Policy Info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF42A5F5), // Blue
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Refund Policy: ")
                        }
                        append("Full deposit returned within 24 hours after item return. Partial deduction may apply if item is damaged.")
                    },
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(
    rentalDays: Int,
    rentalFee: Double,
    securityDeposit: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5C6BC0) // Indigo/Blueish
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security, // Or wallet/payment icon
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Rental Fee ($rentalDays days):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Text(
                    "Rs${String.format("%.2f", rentalFee)}",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Security Deposit:",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Text(
                    "Rs${String.format("%.2f", securityDeposit)}",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Divider(
                color = Color.White.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total to Pay:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Rs${String.format("%.2f", rentalFee + securityDeposit)}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun DateSelectionRow(
    label: String,
    date: Long?,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = isEnabled, onClick = onClick)
                .border(
                    1.dp,
                    if (isEnabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (date != null) {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(date))
                    } else {
                        "Select Date"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MessageSection(
    message: String,
    onMessageChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Message to Owner",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(
                        "Hi, I'm interested in renting this item...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun ActionButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    rentalDays: Int,
    totalPrice: Double,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = if (rentalDays > 0) "Request to Rent" else "Select Dates",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}