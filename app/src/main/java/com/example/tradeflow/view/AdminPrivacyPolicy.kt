package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.repository.AdminRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.AdminViewModel

class AdminPrivacyPolicy : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminPrivacyPolicyScreen(
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
fun AdminPrivacyPolicyScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel = remember { AdminViewModel(AdminRepoImpl()) }

    // State for Change Password Dialog
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                            text = "Privacy & Security",
                            color = Color.White,
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
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            // Account Security Section
            AdminSection("Account Security")

            // Change Password Item
            AdminPrivacyItem("Change Password") {
                showChangePasswordDialog = true
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                errorMessage = ""
            }
        }
    }

    // Change Password Dialog - Exact same as UserSettingPrivacyScreen
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = {
                Text(
                    "Change Password",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color(0xFF333333)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Password Field
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            errorMessage = ""
                        },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = if (showCurrentPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Greenish,
                            focusedLabelColor = Greenish,
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { showCurrentPassword = !showCurrentPassword },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (showCurrentPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showCurrentPassword) "Hide password"
                                    else "Show password",
                                    modifier = Modifier.size(20.dp),
                                    tint = Greenish
                                )
                            }
                        }
                    )

                    // New Password Field
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = ""
                        },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = if (showNewPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Greenish,
                            focusedLabelColor = Greenish,
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { showNewPassword = !showNewPassword },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (showNewPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showNewPassword) "Hide password"
                                    else "Show password",
                                    modifier = Modifier.size(20.dp),
                                    tint = Greenish
                                )
                            }
                        }
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Greenish,
                            focusedLabelColor = Greenish,
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { showConfirmPassword = !showConfirmPassword },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showConfirmPassword) "Hide password"
                                    else "Show password",
                                    modifier = Modifier.size(20.dp),
                                    tint = Greenish
                                )
                            }
                        }
                    )

                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showChangePasswordDialog = false },
                        enabled = !isLoading
                    ) {
                        Text("Cancel", color = Greenish)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (currentPassword.isEmpty()) {
                                errorMessage = "Please enter current password"
                                return@Button
                            }

                            if (newPassword.isEmpty()) {
                                errorMessage = "Please enter new password"
                                return@Button
                            }

                            if (newPassword.length < 6) {
                                errorMessage = "New password must be at least 6 characters"
                                return@Button
                            }

                            if (confirmPassword.isEmpty()) {
                                errorMessage = "Please confirm new password"
                                return@Button
                            }

                            if (newPassword != confirmPassword) {
                                errorMessage = "Passwords don't match"
                                return@Button
                            }

                            if (newPassword == currentPassword) {
                                errorMessage = "New password must be different from current password"
                                return@Button
                            }

                            // Call change password
                            isLoading = true
                            viewModel.changePassword(currentPassword, newPassword) { success, message ->
                                isLoading = false

                                if (success) {
                                    Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                    showChangePasswordDialog = false
                                } else {
                                    errorMessage = message
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Greenish
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Change Password", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun AdminSection(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Greenish.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = Greenish,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun AdminPrivacyItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )
        // Using built-in Material icon instead of custom drawable
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = Color(0xFFE0E0E0),
        thickness = 0.5.dp
    )
}