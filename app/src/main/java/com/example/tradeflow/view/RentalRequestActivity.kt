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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

            // Duration Selection
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
                                rentalPricePerDay = product.price
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

            // Summary
            if (rentalDays > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$rentalDays days",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Price per day:",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Rs${String.format("%.2f", pricePerDay)}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Price:",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Rs${String.format("%.2f", totalPrice)}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
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
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = date?.let { dateFormat.format(Date(it)) } ?: "Select date"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isEnabled)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(enabled = isEnabled) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dateStr,
                fontWeight = FontWeight.SemiBold,
                color = if (isEnabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Default.CalendarToday,
            contentDescription = "Calendar",
            tint = if (isEnabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                text = "Message to Owner (Optional)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = {
                    Text(
                        "Add a message for the owner...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = MaterialTheme.colorScheme.onSurface
                )
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
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Send Rental Request",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (rentalDays > 0) {
                    Text(
                        text = "$rentalDays days • Rs${String.format("%.2f", totalPrice)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}