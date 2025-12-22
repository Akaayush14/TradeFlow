package com.example.tradeflow

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Green
import java.util.*

class EditAdminProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditAdminProfileScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form states
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Gender") }
    var age by remember { mutableStateOf(0) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var showGenderMenu by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Check if any field has been modified
    val hasChanges = name.isNotEmpty() || dateOfBirth.isNotEmpty() ||
            location.isNotEmpty() || gender != "Gender" || agreedToTerms

    // Handle back button press
    BackHandler {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            val intent = Intent(context, AdminSettings::class.java)
            context.startActivity(intent)
            if (context is ComponentActivity) {
                context.finish()
            }
        }
    }

    // Discard Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
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
                        val intent = Intent(context, AdminSettings::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
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
                    onClick = { showDiscardDialog = false },
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

    // Calculate age from date of birth
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

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showDiscardDialog = true
                        } else {
                            val intent = Intent(context, AdminSettings::class.java)
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
                            color = DarkGreen,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

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
                                    val monthNames = arrayOf(
                                        "January", "February", "March", "April", "May", "June",
                                        "July", "August", "September", "October", "November", "December"
                                    )
                                    dateOfBirth = "$selectedDay ${monthNames[selectedMonth]} $selectedYear"
                                    age = calculateAge(dateOfBirth)
                                },
                                year,
                                month,
                                day
                            ).apply {
                                datePicker.maxDate = System.currentTimeMillis()
                            }.show()
                        },
                    readOnly = true,
                    placeholder = { Text("24 December 199", color = Color.Gray) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = android.R.drawable.arrow_down_float),
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

            // Location and Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Location Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Location",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Location...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            cursorColor = DarkGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Gender Field
                Column(modifier = Modifier.weight(1f)) {
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
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.checkbox_on_background),
                                    contentDescription = "Selected",
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
                            onDismissRequest = { showGenderMenu = false }
                        ) {
                            listOf("Male", "Female").forEach { option ->
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
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Confirm Button
            Button(
                onClick = {
                    if (!agreedToTerms) {
                        Toast.makeText(
                            context,
                            "Please agree to the Terms and Conditions and Privacy Policy",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (name.isEmpty() || dateOfBirth.isEmpty() || location.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Please fill all fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // TODO: Save profile data to database
                        Toast.makeText(
                            context,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(context, AdminSettings::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
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

            // Terms and Conditions Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreedToTerms = !agreedToTerms },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = DarkGreen,
                        uncheckedColor = Color.Gray
                    )
                )

                val annotatedText = buildAnnotatedString {
                    append("I've read and agree with the ")

                    pushStringAnnotation(tag = "terms", annotation = "terms")
                    withStyle(style = SpanStyle(color = Color(0xFF007AFF), fontWeight = FontWeight.Normal)) {
                        append("Terms and Conditions")
                    }
                    pop()

                    append(" and the ")

                    pushStringAnnotation(tag = "privacy", annotation = "privacy")
                    withStyle(style = SpanStyle(color = Color(0xFF007AFF), fontWeight = FontWeight.Normal)) {
                        append("Privacy Policy")
                    }
                    pop()

                    append(".")
                }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = Color.Gray
                    ),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                            .firstOrNull()?.let {
                                // Navigate to Terms and Conditions
                                Toast.makeText(context, "Terms and Conditions clicked", Toast.LENGTH_SHORT).show()
                            }

                        annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                            .firstOrNull()?.let {
                                val intent = Intent(context, AdminPrivacyPolicy::class.java)
                                context.startActivity(intent)
                            }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}