package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.repository.AdminRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.AdminViewModel
import java.util.*

class EditAdminProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EditAdminProfileScreen(
                onBackClick = {
                    val intent = Intent(this, AdminProfile::class.java)
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
fun EditAdminProfileScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val adminViewModel = remember { AdminViewModel(AdminRepoImpl()) }
    val admin by adminViewModel.admin.collectAsState()

    // Form states
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Gender") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    var showGenderMenu by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingNavigation by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch current admin data
    LaunchedEffect(Unit) {
        val currentUser = adminViewModel.getCurrentUser()
        currentUser?.let {
            adminViewModel.getAdminById(it.uid)
        }
    }

    // Update form when admin data is loaded
    LaunchedEffect(admin) {
        admin?.let {
            name = it.name
            dateOfBirth = it.dateOfBirth
            phone = it.phone
            email = it.email
            gender = it.gender
            imageUrl = it.imageUrl
        }
    }

    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Check if any field has been modified
    val hasChanges = (admin != null) && (
            name != admin!!.name ||
            dateOfBirth != admin!!.dateOfBirth ||
            phone != admin!!.phone ||
            gender != admin!!.gender ||
            imageUri != null
            )

    // Handle back button press
    BackHandler {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            val intent = Intent(context, AdminProfile::class.java)
            context.startActivity(intent)
            if (context is ComponentActivity) {
                context.finish()
            }
        }
    }

    // Discard Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                showDiscardDialog = false
                pendingNavigation = null
            },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Discard Changes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Do you want to discard changes?",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        val intent = Intent(context, AdminProfile::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                        pendingNavigation = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Yes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        pendingNavigation = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "No",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            pendingNavigation = "back"
                            showDiscardDialog = true
                        } else {
                            val intent = Intent(context, AdminProfile::class.java)
                            context.startActivity(intent)
                            if (context is ComponentActivity) {
                                context.finish()
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit Profile",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Greenish)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Image Picker
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { launcher.launch("image/*") }
                        .border(2.dp, Greenish, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Admin Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Current Admin Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Select Image",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Text(
                    text = "Tap to change photo",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                // Name Field
                Column {
                    Text(
                        text = "Name",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Name...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            cursorColor = DarkGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Email Field (ReadOnly)
                Column {
                    Text(
                        text = "Email",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    )
                }

                // Phone Field
                Column {
                    Text(
                        text = "Phone",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Phone...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            cursorColor = DarkGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Date of Birth Field
                Column {
                    Text(
                        text = "Date of Birth",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable {
                                val calendar = Calendar.getInstance()
                                val year = calendar.get(Calendar.YEAR)
                                val month = calendar.get(Calendar.MONTH)
                                val day = calendar.get(Calendar.DAY_OF_MONTH)

                                DatePickerDialog(
                                    context,
                                    { _, selectedYear, selectedMonth, selectedDay ->
                                        dateOfBirth = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                                    },
                                    year,
                                    month,
                                    day
                                ).apply {
                                    datePicker.maxDate = System.currentTimeMillis()
                                }.show()
                            },
                        readOnly = true,
                        placeholder = { Text("dd/mm/yyyy", color = Color.Gray) },
                        trailingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = DarkGreen
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    )
                }

                // Gender Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Gender",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { showGenderMenu = true },
                            readOnly = true,
                            trailingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                contentDescription = "Select Gender",
                                tint = DarkGreen
                            )
                        },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                                disabledTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false
                        )

                        DropdownMenu(
                            expanded = showGenderMenu,
                            onDismissRequest = { showGenderMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            listOf("Male", "Female", "Other").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        showGenderMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Person,
                                            contentDescription = "Selected",
                                            tint = DarkGreen
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Button
                Button(
                    onClick = {
                        if (name.isEmpty() || dateOfBirth.isEmpty() || phone.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Please fill all fields",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            isLoading = true
                            val adminId = adminViewModel.getCurrentUser()?.uid ?: return@Button
                            
                            val updateData = mutableMapOf<String, Any>(
                                "name" to name,
                                "dateOfBirth" to dateOfBirth,
                                "phone" to phone,
                                "gender" to gender
                            )

                            if (imageUri != null) {
                                adminViewModel.uploadImage(context, imageUri!!) { url ->
                                    if (url != null) {
                                        updateData["imageUrl"] = url
                                        adminViewModel.updateAdmin(adminId, updateData) { success, msg ->
                                            isLoading = false
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                val intent = Intent(context, AdminProfile::class.java)
                                                context.startActivity(intent)
                                                if (context is ComponentActivity) {
                                                    context.finish()
                                                }
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                adminViewModel.updateAdmin(adminId, updateData) { success, msg ->
                                    isLoading = false
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        val intent = Intent(context, AdminProfile::class.java)
                                        context.startActivity(intent)
                                        if (context is ComponentActivity) {
                                            context.finish()
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Greenish
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Confirm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}