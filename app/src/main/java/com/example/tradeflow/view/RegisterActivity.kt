package com.example.tradeflow

import android.app.Activity
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.viewmodel.UserViewModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.TradeFlowTheme
import com.example.tradeflow.ui.theme.Blue
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.PurpleGrey80
import kotlinx.coroutines.launch

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

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegisterBody()
        }
    }
}

@Composable
fun RegisterBody() {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf(false) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var context = LocalContext.current
    val activity = context as Activity

    var selectedCountry by remember {
        mutableStateOf(
            countries.first { it.name == "Nepal" }
        )
    }
    var showCountryDialog by remember { mutableStateOf(false) }

    val BlueButton = Color(0xFF006CFF)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(color = Greenish),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.house_rent_logo),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sign Up!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Greenish
            )

            Spacer(modifier = Modifier.height(20.dp))


            Text("Name", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("Enter a name") }

            )

            Spacer(modifier = Modifier.height(14.dp))

            // Phone Number Field
            Text("Phone Number", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))

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
                    onValueChange = {phone = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text(text = "Enter phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Email Address", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("name@email.com") }
            )

            Spacer(modifier = Modifier.height(14.dp))


            Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("Create a password") },
                visualTransformation =
                    if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            painter = painterResource(
                                if (showPassword)
                                    R.drawable.baseline_visibility_24
                                else
                                    R.drawable.baseline_visibility_off_24
                            ),
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))


            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("Confirm password") },
                visualTransformation =
                    if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            painter = painterResource(
                                if (showConfirmPassword)
                                    R.drawable.baseline_visibility_24
                                else
                                    R.drawable.baseline_visibility_off_24
                            ),
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))


            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = terms,
                    onCheckedChange = { terms = it }
                )
                Text(
                    buildAnnotatedString {
                        append("I've read and agree with the ")
                        withStyle(SpanStyle(color = BlueButton)) {
                            append("Terms and Conditions")
                        }
                        append(" and the ")
                        withStyle(SpanStyle(color = BlueButton)) {
                            append("Privacy Policy.")
                        }
                    },
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            Button(
                onClick = {
                    // Validation
                    if (!terms) {
                        Toast.makeText(context, "Please agree to terms and conditions", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (phone.length < 7) {
                        Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (password.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val fullPhone = selectedCountry.code + phone

                    // Register user
                    userViewModel.register(email, password, fullPhone) { success, message, userId ->
                        if (success) {
                            // Create user model
                            val model = UserModel(
                                userId = userId,
                                name = name,
                                email = email,
                                phone = fullPhone,
                            )

                            // Add to database
                            userViewModel.addUserToDatabase(userId, model) { dbSuccess, dbMessage ->
                                if (dbSuccess) {
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    activity.finish()
                                } else {
                                    Toast.makeText(context, "Database error: $dbMessage", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Registration failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
            ) {
                Text("Register", fontSize = 17.sp, color = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Greenish)
        )
        if (showCountryDialog) {
            Dialog(onDismissRequest = { showCountryDialog = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Select Country",
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
}
@Preview
@Composable
fun RegisterView() {
    RegisterBody()
}