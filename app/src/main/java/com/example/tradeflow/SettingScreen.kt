package com.example.tradeflow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.*



class SettingScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNav()
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "settings") {
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("notifications") {
            NotificationsScreen(navController)
        }
        composable("edit_profile") {
            EditProfileScreen(navController)
        }
        composable("privacy") {
            PrivacySecurityScreen(navController)
        }
        composable("aboutus") {
            AboutUsScreen(navController)

        }


    }
}

// Setting Screen  //

@Composable
fun SettingsScreen(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00897B))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Settings",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )


            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Sidhartha Sah", style = MaterialTheme.typography.titleMedium)
            Text("@sid34gmail.com", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))


        SettingsItem("Notifications") {
            navController.navigate("notifications")
        }
        SettingsItem("Edit Profile") {
            navController.navigate("edit_profile")
        }
        SettingsItem("Language")

        SettingsItem("Privacy & Security") {
            navController.navigate("privacy")
        }
        SettingsItem("About us") {
            navController.navigate("aboutus")
        }
        SettingsItem("Logout") {
            showLogoutDialog = true
        }
    }
    if (showLogoutDialog) {
        IOSStyleLogoutDialog(
            onCancel = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false

                val context = navController.context
                context.startActivity(
                    Intent(context, LoginActivity::class.java)
                )
                (context as ComponentActivity).finish()
            }
        )
    }
}

// Privacy and Security //

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy & Security") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { Section("Account") }
            item { PrivacyItem("Change Password") }
            item { PrivacyItem("Change Email") }
            item { PrivacyItem("Two-Factor Authentication") }

            item { Section("Privacy") }
            item { PrivacyItem("Blocked Users") }
            item { PrivacyItem("Who can see my items") }

            item { Section("Data") }
            item { PrivacyItem("Download Data") }
            item { PrivacyItem("Delete Account") }

        }
    }
}



// Ui components of setting  //

@Composable
fun SettingsItem(title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
    }
    HorizontalDivider()
}

@Composable
fun PrivacyItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
    }
    HorizontalDivider()
}


@Composable
fun Section(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E88E5).copy(alpha = 0.1f)) // subtle highlight
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF1E88E5),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

//Logout Ui//
@Composable
fun IOSStyleLogoutDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Logout",
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Are you sure you want to logout?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.LightGray)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Confirm",
                            color = Color(0xFF7E57C2),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    AppNav()
}
