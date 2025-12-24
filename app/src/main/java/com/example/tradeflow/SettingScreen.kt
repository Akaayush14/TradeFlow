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
import androidx.compose.runtime.*
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

/* ---------------- NAVIGATION ---------------- */

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "settings") {
        composable("settings") { SettingsScreen(navController) }
        composable("notifications") { NotificationsScreen(navController) }
        composable("edit_profile") { EditProfileScreen(navController) }
        composable("privacy") { PrivacySecurityScreen(navController) }
        composable("aboutus") { AboutUsScreen(navController) }
    }
}

// Setting Screen   //

@Composable
fun SettingsScreen(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00897B))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text( text = "Settings", color = Color.White, style = MaterialTheme.typography.titleLarge )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            Box{
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

        SettingsItem("Notifications") { navController.navigate("notifications") }
        SettingsItem("Edit Profile") { navController.navigate("edit_profile") }

        SettingsItemWithValue(
            title = "Language",
            value = selectedLanguage
        ) {
            showLanguageDialog = true
        }

        SettingsItem("Privacy & Security") { navController.navigate("privacy") }
        SettingsItem("About us") { navController.navigate("aboutus") }

        SettingsItem("Logout") { showLogoutDialog = true }
    }

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

// Privacy Ui //

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy & Security") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
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

// Setting Items //

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
fun SettingsItemWithValue(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Text(value, color = Color.Gray)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
    }
    HorizontalDivider()
}

// Language Dialog  //

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

// Making Other Ui //

@Composable
fun PrivacyItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp)
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
            .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(title, color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
    }
}

// add logout Dialog  //

@Composable
fun IOSStyleLogoutDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Are you sure you want to logout?", color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Row(Modifier.height(48.dp)) {
                    Box(Modifier.weight(1f).clickable { onCancel() }, Alignment.Center) {
                        Text("Cancel")
                    }
                    Box(Modifier.width(1.dp).background(Color.LightGray))
                    Box(Modifier.weight(1f).clickable { onConfirm() }, Alignment.Center) {
                        Text("Confirm", color = Color(0xFF7E57C2))
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
