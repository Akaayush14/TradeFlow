package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.UserViewModel

class AdminAdmin : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminAdminScreen()
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminAdminScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 for Admin, 1 for Register Admin

    BackHandler {
        val intent = Intent(context, AdminDashExp::class.java)
        context.startActivity(intent)
        if (context is ComponentActivity) {
            context.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, AdminDashExp::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
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
                            text = "Admin",
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
        ) {
            // Tab Row for Admin and Register Admin
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Gray,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DarkGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Admin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 0) Color.Black else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Register Admin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 1) Color.Black else Color.Gray
                        )
                    }
                )
            }

            // Content based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> AdminListContent()
                    1 -> RegisterAdminContent()
                }
            }
        }
    }
}

@Composable
fun AdminListContent() {
    // TODO: Implement the following:
    // 1. Create UserViewModel instance with UserRepoImpl()
    // 2. Collect allUsers state from viewModel
    // 3. Create state for showDeleteDialog (UserModel?)
    // 4. Use LaunchedEffect to call getAllUser() when composable loads
    // 5. Filter allUsers to show only admins (where user.isAdmin == true)
    //    Note: You'll need to add isAdmin field to UserModel if not present
    // 6. Display empty state if no admins found
    // 7. Display LazyColumn with AdminCard for each admin
    // 8. Pass onDeleteClick handler to show delete confirmation dialog
    // 9. Implement delete confirmation AlertDialog
    // 10. In delete dialog, call userViewModel.deleteUser() with admin.userId
    // 11. Refresh admin list after successful deletion
    // 12. Show appropriate Toast messages for success/failure

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Admin List - TO DO",
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}

@Composable
fun RegisterAdminContent() {
    // TODO: Implement the following:
    // 1. Create UserViewModel instance with UserRepoImpl()
    // 2. Create state variables for: name, email, phone, password, confirmPassword
    // 3. Create form with OutlinedTextField for each field:
    //    - Name (regular text)
    //    - Email (regular text)
    //    - Phone (regular text)
    //    - Password (use PasswordVisualTransformation)
    //    - Confirm Password (use PasswordVisualTransformation)
    // 4. Add Register Admin button
    // 5. Implement validation in button onClick:
    //    - Check if name is not blank
    //    - Check if email is not blank (bonus: validate email format)
    //    - Check if phone is not blank
    //    - Check if password is not blank
    //    - Check if password and confirmPassword match
    //    - Check if password is at least 6 characters
    // 6. If validation passes:
    //    - Call userViewModel.register(email, password, phone)
    //    - On success, create UserModel with:
    //      * userId from registration response
    //      * name, email, phone from form
    //      * isAdmin = true (You'll need to add this field to UserModel)
    //    - Call userViewModel.addUserToDatabase(userId, userModel)
    //    - On success, show success toast and clear all form fields
    //    - On failure, show error toast with message
    // 7. Show appropriate Toast messages for validation errors and success/failure
    // 8. Center the form vertically and horizontally on screen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register New Admin - TO DO",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen
        )
    }
}

@Composable
fun AdminCard(
    admin: UserModel,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = admin.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = admin.email,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = admin.phone,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Delete",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}