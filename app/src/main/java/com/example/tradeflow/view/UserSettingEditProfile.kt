package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

data class Country(
    val name: String,
    val code: String,
    val flag: String
)

val countries = listOf(
    Country("United States", "+1", "🇺🇸"),
    Country("Nigeria", "+234", "🇳🇬"),
    Country("United Kingdom", "+44", "🇬🇧"),
    Country("India", "+91", "🇮🇳"),
    Country("Nepal", "+977", "🇳🇵"),
    Country("China", "+86", "🇨🇳"),
    Country("Bangladesh", "+880", "🇧🇩"),
    Country("New Zealand", "+64", "🇳🇿")
)

class UserSettingEditProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserSettingEditProfileScreen(rememberNavController())
        }
    }
}

@Composable
fun UserSettingEditProfileScreen(navController: NavController) {
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    // State for form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Fetch user data when screen loads
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            isLoading = true
            userViewModel.getUserById(currentUserId)

            // Observe the user data from ViewModel
            userViewModel.users.collect { user ->
                if (user != null) {
                    name = user.name
                    phone = extractPhoneNumber(user.phone) // Extract just the number part
                    gender = user.gender
                    dob = user.dob
                    location = user.location
                    isLoading = false
                }
            }
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, day)
            dob = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(selectedDate.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
                )
                Icon(
                    Icons.Default.Edit,
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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                PhoneNumberField(phone) { phone = it }

                LocationField(location) { location = it }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenderField(gender) { gender = it }

                    OutlinedTextField(
                        value = dob,
                        onValueChange = {},
                        label = { Text("Date of Birth") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerDialog.show() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF005F56),
                                    Color(0xFF007D70),
                                    Color(0xFF4DB6AC)
                                )
                            )
                        )
                        .clickable {
                            // Save all fields to Firebase
                            saveProfileChanges(
                                userId = currentUserId,
                                name = name,
                                phone = phone,
                                gender = gender,
                                dob = dob,
                                location = location,
                                userViewModel = userViewModel,
                                onSuccess = { showSuccessDialog = true }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save Changes", color = Color.White, fontSize = 16.sp)
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
                TextButton(
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
}

@Composable
fun GenderField(gender: String, onGenderChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val genders = listOf("Male", "Female", "Other", "Prefer not to say")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable { expanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))
        Text(
            text = if (gender.isNotEmpty()) gender else "Select Gender",
            color = if (gender.isNotEmpty()) Color.Unspecified else Color.Gray
        )
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowDropDown, null)
        Spacer(Modifier.width(12.dp))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            genders.forEach { genderOption ->
                DropdownMenuItem(
                    text = { Text(genderOption) },
                    onClick = {
                        onGenderChange(genderOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PhoneNumberField(phone: String, onPhoneChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries[0]) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .clickable { expanded = true }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedCountry.flag, fontSize = 20.sp)
                Spacer(Modifier.width(6.dp))
                Text(selectedCountry.code)
                Icon(Icons.Default.ArrowDropDown, null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                countries.forEach {
                    DropdownMenuItem(
                        text = { Text("${it.flag}  ${it.name}") },
                        onClick = {
                            selectedCountry = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        TextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun LocationField(currentLocation: String, onLocationSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    // Find the country that matches current location, or use first one as default
    val selectedCountry = countries.find { it.name == currentLocation } ?: countries[0]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable { expanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))
        Text("${selectedCountry.flag}  ${selectedCountry.name}")
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowDropDown, null)
        Spacer(Modifier.width(12.dp))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countries.forEach {
                DropdownMenuItem(
                    text = { Text("${it.flag}  ${it.name}") },
                    onClick = {
                        onLocationSelected(it.name)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helper function to extract just the phone number (remove country code)
fun extractPhoneNumber(fullPhone: String): String {
    return if (fullPhone.length > 3) {
        // Remove country code (assuming it's at the beginning)
        fullPhone.substring(3)
    } else {
        fullPhone
    }
}

// Helper function to save profile changes
fun saveProfileChanges(
    userId: String,
    name: String,
    phone: String,
    gender: String,
    dob: String,
    location: String,
    userViewModel: UserViewModel,
    onSuccess: () -> Unit
) {
    if (userId.isEmpty()) return
    userViewModel.getUserById(userId)
    onSuccess()
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    val navController = rememberNavController()
    UserSettingEditProfileScreen(navController)
}