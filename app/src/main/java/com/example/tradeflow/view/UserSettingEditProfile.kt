package com.example.tradeflow.view

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeflow.R
import com.example.tradeflow.countries
import com.example.tradeflow.viewmodel.UserViewModel
import com.example.tradeflow.repository.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingEditProfileScreen(navController: NavController) {
    val context = LocalContext.current

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    val userData by userViewModel.users.collectAsState()

    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    val genderOptions = listOf("Male", "Female")
    var showGenderMenu by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries.first { it.name == "Nepal" }) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    LaunchedEffect(userData) {
        userData?.let { user ->
            name = user.name
            if (user.phone.isNotEmpty()) {
                val (country, number) = PhoneParser.parseFullPhone(user.phone)
                selectedCountry = country
                phoneNumber = number
            } else {
                selectedCountry = countries.first { it.name == "Nepal" }
                phoneNumber = ""
            }
            location = user.location ?: ""
            gender = user.gender ?: ""
            dob = user.dob ?: ""
        }
    }

    fun calculateAge(dob: String): Int {
        if (dob.isEmpty()) return 0

        try {
            val parts = dob.split(" ")
            if (parts.size != 3) return 0

            val day = parts[0].toInt()
            val monthStr = parts[1]
            val year = parts[2].toInt()

            val monthMap = mapOf(
                "January" to 0, "February" to 1, "March" to 2, "April" to 3,
                "May" to 4, "June" to 5, "July" to 6, "August" to 7,
                "September" to 8, "October" to 9, "November" to 10, "December" to 11
            )

            val month = monthMap[monthStr] ?: 0

            val birthCalendar = Calendar.getInstance().apply {
                set(year, month, day)
            }

            val today = Calendar.getInstance()
            var calculatedAge = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                calculatedAge--
            }

            return calculatedAge
        } catch (e: Exception) {
            return 0
        }
    }

    fun validateForm(): Boolean {
        return when {
            name.isBlank() -> false
            phoneNumber.isBlank() -> false
            !phoneNumber.all { it.isDigit() } -> false
            phoneNumber.length < 7 -> false
            else -> true
        }
    }

    fun saveProfile() {
        if (!validateForm()) {
            errorMessage = "Please fill all required fields correctly"
            showErrorDialog = true
            return
        }

        isLoading = true

        val updates = mutableMapOf<String, Any>()

        // Name
        if (userData?.name != name) {
            updates["name"] = name.trim()
        }

        // Phone - Update ONLY if FULL phone number changed
        val newFullPhone = "${selectedCountry.code}${phoneNumber.trim()}"
        if (userData?.phone != newFullPhone) {
            updates["phone"] = newFullPhone
        }

        // Location
        if (userData?.location != location) {
            updates["location"] = location.trim()
        }

        // Gender
        if (userData?.gender != gender) {
            updates["gender"] = gender
        }

        // Date of Birth
        if (userData?.dob != dob) {
            updates["dob"] = dob
        }

        if (updates.isNotEmpty()) {
            userViewModel.updateUserProfile(userId, updates) { success, message ->
                isLoading = false
                if (success) {
                    userViewModel.getUserById(userId)  // Refresh
                    showSuccessDialog = true
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF005F56)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF005F56),
                                Color(0xFF007D70),
                                Color(0xFF4DB6AC)
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
                    // Profile image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(
                                width = 5.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.house_rent_logo),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E88E5))
                            .align(Alignment.BottomEnd)
                            .border(
                                width = 3.dp, // Thicker border
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            null,
                            tint = Color.White,
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
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Text(
                        text = "Phone Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
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
                                    color = Color(0xFFF5F5F5),
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
                                fontWeight = FontWeight.Medium
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
                            enabled = !isLoading
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
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gender",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
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
                                placeholder = { Text("Select Gender", color = Color.Gray) },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                                        contentDescription = "Select Gender",
                                        tint = if (isLoading) Color.Gray else Color(0xFF1E88E5)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1E88E5),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    disabledTextColor = Color.Black,
                                    disabledPlaceholderColor = Color.Gray
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
                                        text = { Text(option) },
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
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp)) // Increased from 4dp
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
                            placeholder = { Text("DD Month YYYY", color = Color.Gray) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                                    contentDescription = "Select Date",
                                    tint = if (isLoading) Color.Gray else Color(0xFF1E88E5)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E88E5),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                                disabledTextColor = Color.Black,
                                disabledPlaceholderColor = Color.Gray
                            ),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Age display with more spacing
                if (dob.isNotEmpty()) {
                    val age = calculateAge(dob)
                    if (age > 0) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp)) // Added spacing
                            Text(
                                text = "Age: $age years",
                                fontSize = 15.sp, // Slightly larger
                                color = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp)) // Increased from 24dp

                // Save Button with more top spacing
                Button(
                    onClick = { saveProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp), // Slightly taller
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF005F56)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Text("Saving...", color = Color.White, fontSize = 17.sp) // Slightly larger
                    } else {
                        Text("Save Changes", color = Color.White, fontSize = 17.sp) // Slightly larger
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Increased from 20dp
            }
        }
    }

    // Dialogs (outside Scaffold)

    // Country Dialog
    if (showCountryDialog) {
        Dialog(onDismissRequest = { showCountryDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Country Code",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
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
                                    fontSize = 16.sp
                                )
                                Text(country.code, fontSize = 16.sp)
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
                    color = Color(0xFF005F56)
                )
            },
            text = { Text("Profile updated successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF005F56)
                    )
                ) {
                    Text("OK", color = Color.White)
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
                    color = Color.Red
                )
            },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF005F56)
                    )
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    val navController = rememberNavController()
    UserSettingEditProfileScreen(navController)
}