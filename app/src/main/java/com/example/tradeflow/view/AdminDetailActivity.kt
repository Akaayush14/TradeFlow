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
import com.example.tradeflow.repository.AdminRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.viewmodel.AdminViewModel
import java.util.Calendar

import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete

class AdminDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminDetailScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDetailScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val adminId = activity?.intent?.getStringExtra("adminId") ?: ""

    val viewModel = remember { AdminViewModel(AdminRepoImpl()) }
    val admin by viewModel.admin.collectAsState()

    // Editing states
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedDob by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }

    LaunchedEffect(adminId) {
        if (adminId.isNotEmpty()) {
            viewModel.getAdminById(adminId)
        }
    }

    LaunchedEffect(admin) {
        admin?.let {
            editedName = it.name
            editedPhone = it.phone
            editedDob = it.dateOfBirth
            editedGender = it.gender
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
                title = { Text("Admin Details", color = White) },
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
                                "dateOfBirth" to editedDob,
                                "gender" to editedGender
                            )
                            viewModel.updateAdmin(adminId, updates) { success, message ->
                                if (success) {
                                    Toast.makeText(context, "Admin Updated Successfully", Toast.LENGTH_SHORT).show()
                                    isEditing = false
                                    // Refresh admin data
                                    viewModel.getAdminById(adminId)
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
        if (admin == null && adminId.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Greenish)
            }
        } else if (admin == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Admin not found")
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
                // Admin Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Greenish, CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (admin!!.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = admin!!.imageUrl,
                            contentDescription = "Admin Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
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
                        value = admin!!.email,
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
                    DetailItem(label = "Name", value = admin!!.name)
                    DetailItem(label = "Email", value = admin!!.email)
                    DetailItem(label = "Phone", value = admin!!.phone)
                    DetailItem(label = "Date of Birth", value = admin!!.dateOfBirth)
                    DetailItem(label = "Gender", value = admin!!.gender.ifEmpty { "Not Specified" })
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailItem(label = "Status", value = if (admin!!.isBlocked) "Blocked" else "Active", color = if (admin!!.isBlocked) Color.Red else Greenish)
                    DetailItem(label = "Restrictions", value = if (admin!!.isRestricted) "Restricted" else "None", color = if (admin!!.isRestricted) Color(0xFFFFA500) else Greenish)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, color: Color = Color.Black) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
