package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.RegisterActivity
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val BlueButton = Color(0xFF006CFF)
    val Teal = Color(0xFF00897B)

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    fun handleLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter email and password"
            return
        }

        isLoading = true
        errorMessage = ""

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""

                    if (userId.isNotEmpty()) {
                        // Check if user is an admin
                        val adminsRef = database.getReference("Admins").child(userId)

                        adminsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // Check if admin is blocked
                                    val isBlocked = snapshot.child("isBlocked").getValue(Boolean::class.java) ?: false
                                    if (isBlocked) {
                                        auth.signOut()
                                        errorMessage = "Your account has been blocked. Please contact support."
                                    } else {
                                        // User exists in Admins collection → Admin Dashboard
                                        val intent = Intent(context, AdminDashExp::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        (context as? ComponentActivity)?.finish()
                                    }
                                } else {
                                    // User not in Admins collection, check Users collection
                                    val usersRef = database.getReference("Users").child(userId)
                                    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            if (userSnapshot.exists()) {
                                                // Check if user is blocked
                                                val isBlocked = userSnapshot.child("isBlocked").getValue(Boolean::class.java) ?: false
                                                if (isBlocked) {
                                                    auth.signOut()
                                                    errorMessage = "Your account has been blocked. Please contact support."
                                                } else {
                                                    // Regular User Dashboard
                                                    val intent = Intent(context, UserDashboard::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    context.startActivity(intent)
                                                    (context as? ComponentActivity)?.finish()
                                                }
                                            } else {
                                                // User Auth exists but no DB record found (Edge case)
                                                // Proceed to User Dashboard or show error? 
                                                // Defaulting to UserDashboard to handle profile creation if needed
                                                val intent = Intent(context, UserDashboard::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(intent)
                                                (context as? ComponentActivity)?.finish()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            errorMessage = "Database error: ${error.message}"
                                            auth.signOut()
                                        }
                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // If we can't check, default to user dashboard
                                errorMessage = "Network error: ${error.message}"
                                auth.signOut()
                            }
                        })
                    }
                } else {
                    errorMessage = "Login failed: ${task.exception?.message}"
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Greenish),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.house_rent_logo),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp),
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 25.dp)
        ) {
            Text(
                text = "Welcome!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Teal
            )

            Spacer(modifier = Modifier.height(20.dp))

            // EMAIL FIELD
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            // PASSWORD FIELD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            painter = painterResource(
                                id = if (showPassword) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                            ),
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // FORGOT PASSWORD
            Text(
                text = "Forgot password?",
                color = BlueButton,
                modifier = Modifier.clickable {
                    val intent = Intent(context, ForgetPasswordActivity::class.java)
                    intent.putExtra("email", email)
                    context.startActivity(intent)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ERROR MESSAGE
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LOGIN BUTTON
            Button(
                onClick = { handleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueButton),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = "Login", fontSize = 17.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // REGISTER LINK
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Not a member? ", color = Color.Gray)
                Text(
                    text = "Register now",
                    color = BlueButton,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, RegisterActivity::class.java)
                        context.startActivity(intent)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewLogin() {
    LoginScreen()
}
