package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserSettingEditProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserSettingEditProfileScreen(rememberNavController())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingEditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val coroutineScope = rememberCoroutineScope()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    val userData by userViewModel.users.collectAsState()

    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") } // Just the number part
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

    val calendar = Calendar.getInstance()

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

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    LaunchedEffect(userData) {
        userData?.let { user ->
            // Set name
            name = user.name

            // Parse phone number
            if (user.phone.isNotEmpty()) {
                val (country, number) = PhoneParser.parseFullPhone(user.phone)
                selectedCountry = country
                phoneNumber = number
            }
        }
    }

    fun validateForm(): Boolean {
        var isValid = true

        if (name.isBlank()) {
            isValid = false
        }

        if (phoneNumber.isBlank() || !PhoneParser.isValidPhoneNumber(phoneNumber)) {
            isValid = false
        }

        return isValid
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

        // Phone
        val fullPhone = PhoneParser.combinePhone(selectedCountry, phoneNumber)
        if (userData?.phone != fullPhone) {
            updates["phone"] = fullPhone
        }

        // Location (NEW)
        if (userData?.location != location) {
            updates["location"] = location.trim()
        }

        // Gender (NEW)
        if (userData?.gender != gender) {
            updates["gender"] = gender
        }

        // Date of Birth (NEW)
        if (userData?.dob != dob) {
            updates["dob"] = dob
        }

        if (updates.isNotEmpty()) {
            userViewModel.updateUserProfile(userId, updates) { success, message ->
                isLoading = false

                if (success) {
                    // Refresh user data to get updated values
                    userViewModel.getUserById(userId)
                    showSuccessDialog = true
                } else {
                    errorMessage = message
                    showErrorDialog = true
                }
            }
        } else {
            isLoading = false
            showSuccessDialog = true // No changes needed
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF005F56),
                            Color(0xFF007D70),
                            Color(0xFF4DB6AC)
                        )
                    )
                )
        ) {
            IconButton(
                onClick = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        activity?.finish()
                    }
                },
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "Edit Profile",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-60).dp),
            contentAlignment = Alignment.Center
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.house_rent_logo),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Icon(
                    Icons.Filled.Edit,
                    null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E88E5))
                        .padding(6.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country Code Picker Button
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

                Spacer(modifier = Modifier.width(8.dp))

                // Phone Number Input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text(text = "Enter phone number") },
                    enabled = !isLoading
                )
            }

            // Location Field
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your location") },
                enabled = !isLoading
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Gender Field - Reduced width
                Column(modifier = Modifier.weight(0.8f)) {
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
                            placeholder = { Text("Gender", color = Color.Gray) },
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
                            enabled = false
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

                // Date of Birth Field - Increased width
                Column(modifier = Modifier.weight(1.2f)) {
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
                        placeholder = { Text("24 December 1999", color = Color.Gray) },
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
                        enabled = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF005F56))
                    .clickable(enabled = !isLoading) {
                        saveProfile()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Text("Saving...", color = Color.White, fontSize = 16.sp)
                } else {
                    Text("Save Changes", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }

    // Country Dialog
    if (showCountryDialog) {
        Dialog(onDismissRequest = { showCountryDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Country Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.height(300.dp)) {
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

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text("Profile updated successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK")
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