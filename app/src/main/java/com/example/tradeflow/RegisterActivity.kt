package com.example.tradeflow

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
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
import com.example.tradeflow.ui.theme.TradeFlowTheme
import com.example.tradeflow.ui.theme.Blue
import com.example.tradeflow.ui.theme.Green
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.PurpleGrey80
import kotlinx.coroutines.launch


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
    var terms by remember { mutableStateOf(false) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val BlueButton = Color(0xFF006CFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(color = Greenish),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.house_rent_logo),
                contentDescription = null,
                modifier = Modifier.size(300.dp)
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
                color = Green
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
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
            ) {
                Text("Login", fontSize = 17.sp, color = Color.White)
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Green)
        )
    }
}
@Preview
@Composable
fun RegisterView() {
    RegisterBody()

}