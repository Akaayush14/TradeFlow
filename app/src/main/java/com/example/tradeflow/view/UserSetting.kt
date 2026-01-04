package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.tradeflow.R

class UserSetting : ComponentActivity() {
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
        composable("settings") { SettingsScreen(navController) }
        composable("notifications") { NotificationsScreen(navController) }
        composable("edit_profile") { EditProfileScreen(navController) }
        composable("privacy") { PrivacySecurityScreen(navController) }
//        composable("aboutus") { AboutUsScreen(navController) }
    }
}


class CurvedBottomShape: Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            lineTo(0f, size.height - 80)
            quadraticBezierTo(
                size.width / 2,
                size.height + 40,
                size.width,
                size.height - 80
            )
            lineTo(size.width, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/* ---------------- SETTINGS SCREEN ---------------- */
@Composable
fun SettingsScreen(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Profile Header
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(CurvedBottomShape())
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
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .offset(x = (-40).dp, y = (-30).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 260.dp, y = 20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 130.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    Image(
                        painter = painterResource(R.drawable.house_rent_logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Sidhartha Sah", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("@sid34gmail.com", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Settings Items
        SettingsItem("Notifications", R.drawable.notification_filled) {
            navController.navigate("notifications")
        }
        SettingsItem("Edit Profile", R.drawable.profile_filled) {
            navController.navigate("edit_profile")
        }
        SettingsItemWithValue("Language", selectedLanguage, R.drawable.language) {
            showLanguageDialog = true
        }
        SettingsItem("Privacy & Security", R.drawable.privacy) {
            navController.navigate("privacy")
        }
        SettingsItem("About us", R.drawable.aboutus) {
            navController.navigate("aboutus")
        }
        SettingsItem("Logout", R.drawable.signout) {
            showLogoutDialog = true
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        IOSStyleLogoutDialog(
            onCancel = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                val context = navController.context
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as ComponentActivity).finish()
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            selectedLanguage = selectedLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = {
                selectedLanguage = it
                showLanguageDialog = false
            }
        )
    }
}

/* ---------------- SETTINGS ITEM ---------------- */
@Composable
fun SettingsItem(title: String, iconRes: Int? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(title, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
    Divider()
}

@Composable
fun SettingsItemWithValue(title: String, value: String, iconRes: Int? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(title, modifier = Modifier.weight(1f))
        Text(value, color = Color.Gray)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
    Divider()
}

/* ---------------- PRIVACY & SECURITY SCREEN ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy & Security") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item { Section("Account") }
            item { PrivacyItem("Change Password") }
            item { PrivacyItem("Change Email") }
            item { PrivacyItem("Two-Factor Authentication") }
            item { Section("Privacy") }
            item { PrivacyItem("Blocked Users") }
            item { PrivacyItem("Who can see my items") }
        }
    }
}

@Composable
fun PrivacyItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp)
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
    Divider()
}

@Composable
fun Section(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(title, color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
    }
}

/* ---------------- LOGOUT DIALOG ---------------- */
@Composable
fun IOSStyleLogoutDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(R.drawable.house_rent_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Comeback Soon!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are You Sure You Want to Logout?",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .background(Color.LightGray)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Yes, Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/* ---------------- LANGUAGE DIALOG ---------------- */
@Composable
fun LanguageDialog(
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("English", "Nepali", "Hindi", "Chinese")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Language", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                languages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(lang) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(lang, modifier = Modifier.weight(1f))
                        if (lang == selectedLanguage) {
                            Text("✓", color = Color(0xFF1E88E5))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MaterialTheme {
        AppNav()
    }
}

