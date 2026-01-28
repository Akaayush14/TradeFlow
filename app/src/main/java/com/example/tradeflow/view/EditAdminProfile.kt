package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri as AndroidUri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.countries
import com.example.tradeflow.repository.AdminRepoImpl
import com.example.tradeflow.theme.ThemeManager
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class EditAdminProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
                EditAdminProfileScreen(
                    onBackClick = {
                        val intent = Intent(this, AdminSettings::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAdminProfileScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel = remember { AdminViewModel(AdminRepoImpl()) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val admin by viewModel.admin.collectAsState()

    // Form fields
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    val genderOptions = listOf("Male", "Female", "Other")
    var showGenderMenu by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries.first { it.name == "Nepal" }) }
    var showCountryDialog by remember { mutableStateOf(false) }

    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Image state
    var profileImageUrl by remember { mutableStateOf("") }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: AndroidUri? ->
        if (uri != null) {
            Log.d("ADMIN_PROFILE_IMAGE", "Image selected: $uri")
            isUploadingImage = true

            viewModel.uploadImage(context, uri) { cloudinaryUrl ->
                isUploadingImage = false

                if (cloudinaryUrl != null) {
                    Log.d("ADMIN_PROFILE_IMAGE", "Cloudinary upload successful: $cloudinaryUrl")

                    val updates = mapOf("imageUrl" to cloudinaryUrl)
                    viewModel.updateAdmin(userId, updates) { success, message ->
                        if (success) {
                            profileImageUrl = cloudinaryUrl
                            viewModel.getAdminById(userId)
                            Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Uploaded but failed to save: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("ADMIN_PROFILE_IMAGE", "Cloudinary upload failed")
                    Toast.makeText(context, "Failed to upload photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Load admin data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.getAdminById(userId)
        }
    }

    // Update form fields when admin data changes
    LaunchedEffect(admin) {
        admin?.let { adminData ->
            name = adminData.name

            // Parse phone number
            if (adminData.phone.isNotEmpty()) {
                val phone = adminData.phone
                if (phone.startsWith("+")) {
                    for (country in countries) {
                        if (phone.startsWith(country.code)) {
                            selectedCountry = country
                            phoneNumber = phone.substring(country.code.length)
                            break
                        }
                    }
                } else {
                    selectedCountry = countries.first { it.name == "Nepal" }
                    phoneNumber = phone
                }
            } else {
                selectedCountry = countries.first { it.name == "Nepal" }
                phoneNumber = ""
            }

            location = adminData.location ?: ""
            gender = adminData.gender ?: ""
            dob = adminData.dateOfBirth ?: ""
            profileImageUrl = adminData.imageUrl ?: ""
            Log.d("ADMIN_PROFILE", "Loaded profile image URL: $profileImageUrl")
        }
    }

    // Handle back button
    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(y = 60.dp)
                ) {
                    // Profile image container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(
                                width = 5.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .clickable {
                                if (!isUploadingImage) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploadingImage) {
                            // Show loading indicator while uploading
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (profileImageUrl.isNotEmpty()) {
                            // Show Cloudinary image
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.placeholderimage),
                                error = painterResource(R.drawable.ic_profile)
                            )
                        } else {
                            // Show default/placeholder
                            Icon(
                                painter = painterResource(R.drawable.ic_profile),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Edit icon overlay
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .align(Alignment.BottomEnd)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .clickable {
                                if (!isUploadingImage) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // More spacing between profile picture and form
            Spacer(modifier = Modifier.height(70.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Column {
                    Text(
                        text = "Phone Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = !isLoading) {
                                    showCountryDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${selectedCountry.flag} ${selectedCountry.code}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Phone Number Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("Enter phone number") },
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your location") },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "Gender",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isLoading) {
                                        showGenderMenu = true
                                    },
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        "Select Gender",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                                        contentDescription = "Select Gender",
                                        tint = if (isLoading)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ),
                                enabled = false,
                                shape = RoundedCornerShape(12.dp)
                            )

                            DropdownMenu(
                                expanded = showGenderMenu,
                                onDismissRequest = { showGenderMenu = false }
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                option,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            gender = option
                                            showGenderMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Date of Birth Field
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Date of Birth",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = dob,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isLoading) {
                                    val calendar = Calendar.getInstance()
                                    val year = calendar.get(Calendar.YEAR)
                                    val month = calendar.get(Calendar.MONTH)
                                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                                    DatePickerDialog(
                                        context,
                                        { _, selectedYear, selectedMonth, selectedDay ->
                                            val monthNames = arrayOf(
                                                "January", "February", "March", "April", "May", "June",
                                                "July", "August", "September", "October", "November", "December"
                                            )
                                            dob = "$selectedDay ${monthNames[selectedMonth]} $selectedYear"
                                        },
                                        year,
                                        month,
                                        day
                                    ).apply {
                                        datePicker.maxDate = System.currentTimeMillis()
                                    }.show()
                                },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "DD Month YYYY",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                                    contentDescription = "Select Date",
                                    tint = if (isLoading)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Save Button with more top spacing
                Button(
                    onClick = {
                        // Validation
                        when {
                            name.isBlank() -> {
                                errorMessage = "Name is required"
                                showErrorDialog = true
                            }
                            phoneNumber.isBlank() -> {
                                errorMessage = "Phone number is required"
                                showErrorDialog = true
                            }
                            !phoneNumber.all { it.isDigit() } -> {
                                errorMessage = "Phone number should contain only digits"
                                showErrorDialog = true
                            }
                            phoneNumber.length < 7 -> {
                                errorMessage = "Phone number must be at least 7 digits"
                                showErrorDialog = true
                            }
                            else -> {
                                // Save logic
                                isLoading = true
                                val updates = mutableMapOf<String, Any>()

                                // Name
                                if (admin?.name != name) {
                                    updates["name"] = name.trim()
                                }

                                // Phone
                                val newFullPhone = "${selectedCountry.code}${phoneNumber.trim()}"
                                if (admin?.phone != newFullPhone) {
                                    updates["phone"] = newFullPhone
                                }

                                // Location
                                if (admin?.location != location) {
                                    updates["location"] = location.trim()
                                }

                                // Gender
                                if (admin?.gender != gender) {
                                    updates["gender"] = gender
                                }

                                // Date of Birth
                                if (admin?.dateOfBirth != dob) {
                                    updates["dateOfBirth"] = dob
                                }

                                // Profile Image
                                if (profileImageUrl.isNotEmpty() && admin?.imageUrl != profileImageUrl) {
                                    updates["imageUrl"] = profileImageUrl
                                }

                                if (updates.isNotEmpty()) {
                                    viewModel.updateAdmin(userId, updates) { success, message ->
                                        isLoading = false
                                        if (success) {
                                            showSuccessDialog = true
                                            viewModel.getAdminById(userId)
                                        } else {
                                            errorMessage = message
                                            showErrorDialog = true
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    showSuccessDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Text(
                            "Saving...",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 17.sp
                        )
                    } else {
                        Text(
                            "Save Changes",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Country Dialog
    if (showCountryDialog) {
        Dialog(onDismissRequest = { showCountryDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Country Code",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(countries) { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCountry = country
                                        showCountryDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(country.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    country.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    country.code,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    "Success",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    "Profile updated successfully!",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
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
            }
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
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditAdminProfilePreview() {
    MaterialTheme {
        EditAdminProfileScreen()
    }
}