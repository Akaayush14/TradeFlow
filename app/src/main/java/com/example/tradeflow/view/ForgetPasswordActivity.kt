package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.tradeflow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.tradeflow.ui.theme.Greenish

class ForgetPasswordActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgotBody()
        }
    }
}

@Composable
fun ForgotBody() {
    val auth = FirebaseAuth.getInstance()
    var emailError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val BlueButton = Color(0xFF006CFF)

    LaunchedEffect(Unit) {
        val passedEmail = (context as? ComponentActivity)?.intent?.getStringExtra("email")
        if (!passedEmail.isNullOrEmpty()) {
            email = passedEmail
        }
    }

    fun checkUserType(email: String, onResult: (isAdmin: Boolean?) -> Unit) {
        val database = FirebaseDatabase.getInstance()

        // First check Admins collection
        database.getReference("Admins")
            .orderByChild("email").equalTo(email)
            .get().addOnSuccessListener { adminSnapshot ->
                if (adminSnapshot.exists()) {
                    onResult(true)
                } else {
                    database.getReference("Users")
                        .orderByChild("email").equalTo(email)
                        .get().addOnSuccessListener { userSnapshot ->
                            if (userSnapshot.exists()) {
                                onResult(false)
                            } else {
                                onResult(null)
                            }
                        }
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(color = Greenish),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Image(
                    painter = painterResource(R.drawable.house_rent_logo),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Reset Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Greenish
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter your email to receive password reset link",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = emailError
                )

                if (emailError) {
                    Text(
                        text = "Please enter a valid email address",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isEmpty() || !email.contains("@")) {
                            emailError = true
                            return@Button
                        }

                        emailError = false
                        isLoading = true

                        checkUserType(email.trim()) { userType ->
                            if (userType == null) {
                                // Email not found in database
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Email not found in system",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // Email exists, send reset link
                                auth.sendPasswordResetEmail(email.trim())
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            val userTypeText = if (userType) "Admin" else "User"
                                            Toast.makeText(
                                                context,
                                                "Password reset link sent to $email ($userTypeText account)",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            // Navigate back to login screen
                                            context.startActivity(
                                                Intent(context, LoginActivity::class.java)
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Error: ${task.exception?.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButton),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Send Reset Link", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewForgot() {
    ForgotBody()
}