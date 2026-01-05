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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
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

            LocationField { location = it }

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
                    label = { Text("DOB") },
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
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text("Save Changes", color = Color.White, fontSize = 16.sp)
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
fun LocationField(onLocationSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries[0]) }

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
                        selectedCountry = it
                        onLocationSelected(it.name)
                        expanded = false
                    }
                )
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