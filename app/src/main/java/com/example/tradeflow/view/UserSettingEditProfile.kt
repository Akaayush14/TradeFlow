package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeflow.R
import java.text.SimpleDateFormat
import java.util.*

// Data class for country codes (similar to RegisterActivity)
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
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") } // Changed from country to location
    var dob by remember { mutableStateOf("") }
    var selectedCountry by remember {
        mutableStateOf(
            countries.first { it.name == "Nepal" } // Default to Nepal
        )
    }
    var showCountryDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? ComponentActivity
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
                    // Check if we can pop back in navigation, otherwise finish activity
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
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country Code Picker Button (similar to RegisterActivity)
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(56.dp)
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
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
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text(text = "Enter phone number") }
                )
            }

            // Location Field (replacing country selection)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your location") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.weight(1f)
                )

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
                        // Save changes logic here
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Save Changes", color = Color.White, fontSize = 16.sp)
            }
        }
    }

    // Country Dialog (similar to RegisterActivity)
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
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    val navController = rememberNavController()
    UserSettingEditProfileScreen(navController)
}