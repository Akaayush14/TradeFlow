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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.tradeflow.R
import com.example.tradeflow.repository.UserRepoImpl
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
    val userRepo = UserRepoImpl()
    var emailError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val BlueButton = Color(0xFF006CFF)


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(color = Greenish),
                contentAlignment = Alignment.Center
            ) {
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
                    text = "Forgot Password?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Greenish
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))



                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))


                Button(
                    onClick = {
                        if (email.isEmpty() || !email.endsWith("@gmail.com")) {
                            emailError = true
                        } else {
                            emailError = false
                            val userRepo = UserRepoImpl()
                            userRepo.forgetPassword(email) { success, message ->
                                (context as? ComponentActivity)?.runOnUiThread {
                                    Toast.makeText(context, "Password reset link sent successfully", Toast.LENGTH_LONG).show()
                                    if (success) {
                                        // Navigate back to login screen
                                        context.startActivity(Intent(context, LoginActivity::class.java))
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
                ) {
                    Text("Reset Password", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(9.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Back to Login",
                        color = BlueButton,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            context.startActivity(
                                Intent(context, LoginActivity::class.java)
                            )
                        }
                        )
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