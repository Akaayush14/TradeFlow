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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation.compose.NavHost
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.UserSettingAboutUsScreen
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class UserSetting: ComponentActivity() {
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
        composable("settings") { UserSettingsScreen(navController) }
        composable("edit_profile") { UserSettingEditProfileScreen(navController) }
        composable("privacy") { UserSettingPrivacyScreen(navController) }
        composable("aboutus") { UserSettingAboutUsScreen(navController) }
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
fun UserSettingsScreen(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Get current user
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val userEmailFromAuth = currentUser?.email ?: ""

    // Initialize ViewModel
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val userData by userViewModel.users.collectAsState()

    // Load user data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId) { success, message, user ->
                if (!success) {
                    println("Failed to load user data: $message")
                }
            }
        }
    }

    // Debug logging for profile image URL
    LaunchedEffect(userData) {
        println("SettingsScreen - User data loaded: ${userData?.name}")
        println("SettingsScreen - Profile image URL: ${userData?.profileImageUrl}")
        println("SettingsScreen - Has profileImageUrl: ${userData?.profileImageUrl?.isNotEmpty()}")
    }

    // Determine what to display
    val displayName = remember(userData) {
        userData?.name?.ifEmpty {
            currentUser?.displayName ?: "User"
        } ?: "Loading..."
    }

    val displayEmail = remember(userData) {
        userData?.email?.ifEmpty {
            userEmailFromAuth
        } ?: "Loading..."
    }

    val profileImageUrl = remember(userData) {
        userData?.profileImageUrl ?: ""
    }

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
                // Decorative circles in background
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-30).dp, y = 30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 300.dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 250.dp, y = (-40).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 130.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile picture - UPDATED to use AsyncImage
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(
                            width = 4.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                ) {
                    if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.placeholderimage),
                            error = painterResource(R.drawable.house_rent_logo)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.house_rent_logo),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Display ACTUAL user name
                Text(
                    text = displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Display ACTUAL user email
                Text(
                    text = displayEmail,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Settings Items
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                SettingsItem("Edit Profile", R.drawable.profile_filled) {
                    navController.navigate("edit_profile")
                }
            }
            item {
                SettingsItemWithValue("Language", selectedLanguage, R.drawable.language) {
                    showLanguageDialog = true
                }
            }
            item {
                SettingsItem("Privacy & Security", R.drawable.privacy) {
                    navController.navigate("privacy")
                }
            }
            item {
                SettingsItem("About us", R.drawable.aboutus) {
                    navController.navigate("aboutus")
                }
            }
            item {
                SettingsItem("Logout", R.drawable.signout) {
                    showLogoutDialog = true
                }
            }
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
            .padding(vertical = 12.dp, horizontal = 16.dp),
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
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = Color.LightGray.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}

@Composable
fun SettingsItemWithValue(title: String, value: String, iconRes: Int? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
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
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Text(
            text = value,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = Color.LightGray.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}

/* ---------------- PRIVACY & SECURITY SCREEN ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingPrivacyScreen(navController: NavController) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Section("Account")

            // Only keeping Change Password as per your advice
            PrivacyItem("Change Password") {
                currentUser?.email?.let { email ->
                    userViewModel.forgetPassword(email) { success, message ->
                        dialogMessage = if (success) {
                            "A password reset link has been sent to your email: $email"
                        } else {
                            "Error: $message"
                        }
                        showDialog = true
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Security") },
            text = { Text(dialogMessage) }
        )
    }
}

@Composable
fun PrivacyItem(title: String, onClick: () -> Unit) {
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
            fontSize = 16.sp
        )
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = Color.LightGray.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}

@Composable
fun Section(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF1E88E5),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
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
            shape = RoundedCornerShape(20.dp),
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
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
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
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Language",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                languages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(lang) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp
                        )
                        if (lang == selectedLanguage) {
                            // FIXED: Using Icons.Default.CheckCircle instead of drawable resource
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (lang != languages.last()) {
                        Divider(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            thickness = 0.5.dp
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
    MaterialTheme {
        AppNav()
    }
}