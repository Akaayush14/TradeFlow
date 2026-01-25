package com.example.tradeflow.view

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.viewmodel.UserViewModel
import java.util.Calendar

import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.model.UserNotificationModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.viewmodel.ProductViewModel

class AdminUserDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminUserDetailScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val userId = activity?.intent?.getStringExtra("userId") ?: ""

    val viewModel = remember { UserViewModel(UserRepoImpl()) }
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val userNotificationRepo = remember { UserNotificationRepoImpl() }
    val user by viewModel.users.collectAsState()
    val userProducts by productViewModel.allProducts.collectAsState()

    // Editing states
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedDob by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }
    var editedLocation by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.getUserById(userId) { _, _, _ -> }
            productViewModel.getProductsByOwner(userId)
        }
    }

    LaunchedEffect(user) {
        user?.let {
            editedName = it.name
            editedPhone = it.phone
            editedDob = it.dob ?: ""
            editedGender = it.gender
            editedLocation = it.location
        }
    }

    // Date Picker
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            editedDob = "$dayOfMonth/${month + 1}/$year"
        }, year, month, day
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("User Details", color = White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            // Save Logic
                            val updates = mapOf(
                                "name" to editedName,
                                "phone" to editedPhone,
                                "dob" to editedDob,
                                "gender" to editedGender,
                                "location" to editedLocation
                            )
                            viewModel.updateUserProfile(userId, updates) { success, message ->
                                if (success) {
                                    Toast.makeText(context, "User Updated Successfully", Toast.LENGTH_SHORT).show()
                                    isEditing = false
                                    // Refresh user data
                                    viewModel.getUserById(userId) { _, _, _ -> }
                                } else {
                                    Toast.makeText(context, "Update Failed: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Save, contentDescription = "Save", tint = White)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish
                )
            )
        }
    ) { paddingValues ->
        if (user == null && userId.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Greenish)
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Greenish, RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (user!!.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user!!.profileImageUrl,
                            contentDescription = "User Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_profile),
                            placeholder = painterResource(R.drawable.ic_profile)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Default Image",
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isEditing) {
                    // Editable Fields
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedLocation,
                        onValueChange = { editedLocation = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Date of Birth Picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editedDob,
                            onValueChange = { },
                            label = { Text("Date of Birth") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calender),
                                    contentDescription = "Select Date",
                                    modifier = Modifier.clickable { datePickerDialog.show() }
                                )
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { datePickerDialog.show() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Gender Selection
                    Text("Gender", modifier = Modifier.align(Alignment.Start).padding(start = 8.dp), color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = editedGender == "Male",
                            onClick = { editedGender = "Male" },
                            colors = RadioButtonDefaults.colors(selectedColor = Greenish)
                        )
                        Text("Male", modifier = Modifier.padding(end = 16.dp))

                        RadioButton(
                            selected = editedGender == "Female",
                            onClick = { editedGender = "Female" },
                            colors = RadioButtonDefaults.colors(selectedColor = Greenish)
                        )
                        Text("Female", modifier = Modifier.padding(end = 16.dp))

                        RadioButton(
                            selected = editedGender == "Other",
                            onClick = { editedGender = "Other" },
                            colors = RadioButtonDefaults.colors(selectedColor = Greenish)
                        )
                        Text("Other")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Email (Read Only)
                    OutlinedTextField(
                        value = user!!.email,
                        onValueChange = {},
                        label = { Text("Email (Not Editable)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )

                } else {
                    // Display Mode
                    DetailItem(label = "Name", value = user!!.name)
                    DetailItem(label = "Email", value = user!!.email)
                    DetailItem(label = "Phone", value = user!!.phone)
                    DetailItem(label = "Location", value = user!!.location.ifEmpty { "Not Specified" })
                    DetailItem(label = "Date of Birth", value = (user!!.dob ?: "").ifEmpty { "Not Specified" })
                    DetailItem(label = "Gender", value = user!!.gender.ifEmpty { "Not Specified" })
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailItem(label = "Status", value = if (user!!.isBlocked) "Blocked" else "Active", color = if (user!!.isBlocked) Color.Red else Greenish)
                    DetailItem(label = "Restrictions", value = if (user!!.isRestricted) "Restricted" else "None", color = if (user!!.isRestricted) Color(0xFFFFA500) else Greenish)
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // User Listings Section
                    Text(
                        text = "User Listings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (userProducts.isEmpty()) {
                        Text(
                            text = "No listings found for this user.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            userProducts.forEach { product ->
                                ItemCardItem(
                                    product = product,
                                    onListClick = {
                                        productViewModel.listProduct(product.productId, true) { success: Boolean, _: String ->
                                            if (success) {
                                                // Notify User
                                                val displayImage = if (product.imageUrl.isNotEmpty()) product.imageUrl else product.imageUrls.firstOrNull() ?: ""
                                                val notification = UserNotificationModel(
                                                    type = "MESSAGE",
                                                    requestType = "System",
                                                    title = "Product Listed",
                                                    message = "Your product '${product.name}' is now listed.",
                                                    receiverId = product.ownerId,
                                                    productId = product.productId,
                                                    productName = product.name,
                                                    productImage = displayImage,
                                                    senderName = "Admin",
                                                    senderId = "ADMIN"
                                                )
                                                userNotificationRepo.createNotification(notification) { _, _ -> }

                                                Toast.makeText(context, "Item Listed", Toast.LENGTH_SHORT).show()
                                                productViewModel.getProductsByOwner(userId)
                                            }
                                        }
                                    },
                                    onUnlistClick = {
                                        productViewModel.listProduct(product.productId, false) { success: Boolean, _: String ->
                                            if (success) {
                                                // Notify User
                                                val displayImage = if (product.imageUrl.isNotEmpty()) product.imageUrl else product.imageUrls.firstOrNull() ?: ""
                                                val notification = UserNotificationModel(
                                                    type = "MESSAGE",
                                                    requestType = "System",
                                                    title = "Product Unlisted",
                                                    message = "Your product '${product.name}' has been unlisted by Admin.",
                                                    receiverId = product.ownerId,
                                                    productId = product.productId,
                                                    productName = product.name,
                                                    productImage = displayImage,
                                                    senderName = "Admin",
                                                    senderId = "ADMIN"
                                                )
                                                userNotificationRepo.createNotification(notification) { _, _ -> }

                                                Toast.makeText(context, "Item Unlisted", Toast.LENGTH_SHORT).show()
                                                productViewModel.getProductsByOwner(userId)
                                            }
                                        }
                                    },
                                    onDeleteClick = {
                                        productViewModel.deleteProduct(product.productId) { success: Boolean, _: String ->
                                            if (success) {
                                                Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
                                                productViewModel.getProductsByOwner(userId)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    
                }
            }
        }
    }
}