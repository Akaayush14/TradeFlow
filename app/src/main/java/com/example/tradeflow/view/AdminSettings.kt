package com.example.tradeflow.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.theme.ThemeManager
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.repository.AdminRepoImpl
import com.example.tradeflow.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
                AdminSettingsScreen(
                    onBackClick = {
                        val intent = Intent(this, AdminDashExp::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminSettingsScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Get current user
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val userEmailFromAuth = currentUser?.email ?: ""

    // Initialize ViewModel for ADMIN data - Use AdminViewModel
    val adminViewModel = remember { AdminViewModel(AdminRepoImpl()) }

    // Get theme from ThemeManager
    val currentThemeMode by remember { ThemeManager.themeMode }
    var isLoading by remember { mutableStateOf(false) }

    // State for admin data
    var adminData by remember { mutableStateOf<com.example.tradeflow.model.AdminModel?>(null) }
    
    // Developer Tools State
    var showRevertDialog by remember { mutableStateOf(false) }
    var isReverting by remember { mutableStateOf(false) }
    val userNotificationViewModel = remember {
        com.example.tradeflow.viewmodel.UserNotificationViewModel(
            com.example.tradeflow.repository.UserNotificationRepoImpl(),
            com.example.tradeflow.repository.ProductRepoImpl()
        )
    }

    // Load admin data using AdminViewModel
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            adminViewModel.getAdminById(userId)
        }
    }

    // Observe admin data from ViewModel
    val adminState by adminViewModel.admin.collectAsState()

    // Update local adminData when ViewModel state changes
    LaunchedEffect(adminState) {
        adminData = adminState
        if (adminData != null) {
            println("✅ AdminSettings - Admin data loaded from ViewModel!")
            println("   Name: ${adminData?.name}")
            println("   Email: ${adminData?.email}")
            println("   Image URL: ${adminData?.imageUrl}")
        }
    }

    // Determine what to display - Use admin data
    val displayName = remember(adminData) {
        adminData?.name?.ifEmpty {
            currentUser?.displayName?.ifEmpty { "Administrator" }
        } ?: currentUser?.displayName?.ifEmpty { "Administrator" } ?: "Administrator"
    }

    val displayEmail = remember(adminData) {
        adminData?.email?.ifEmpty {
            userEmailFromAuth.ifEmpty { "No email" }
        } ?: userEmailFromAuth.ifEmpty { "No email" }
    }

    val profileImageUrl = remember(adminData) {
        adminData?.imageUrl?.ifEmpty { "" } ?: ""
    }

    println("📱 AdminSettings Display Values:")
    println("   Display Name: '$displayName'")
    println("   Display Email: '$displayEmail'")
    println("   Profile URL: '$profileImageUrl'")
    println("   User ID: '$userId'")
    println("   AdminData exists: ${adminData != null}")

    // Handle back button press
    BackHandler {
        onBackClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Profile Header
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            ) {
                // Bubbles
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-30).dp, y = 30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 300.dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 250.dp, y = (-40).dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f))
                )
            }

            // Profile content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 130.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.placeholderimage),
                            error = painterResource(R.drawable.house_rent_logo)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_profile),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Display admin username
                Text(
                    text = displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Display admin email
                Text(
                    text = displayEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Settings Items in LazyColumn
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                AdminSettingsItem("Edit Profile", R.drawable.profile_filled) {
                    val intent = Intent(context, EditAdminProfile::class.java)
                    context.startActivity(intent)
                }
            }
            item {
                AdminThemeSettingsItem(
                    currentThemeMode = currentThemeMode,
                    isLoading = isLoading,
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                AdminSettingsItemWithValue("Language", selectedLanguage, R.drawable.language) {
                    showLanguageDialog = true
                }
            }
            item {
                AdminSettingsItem("Privacy & Security", R.drawable.privacy) {
                    val intent = Intent(context, AdminPrivacyPolicy::class.java)
                    context.startActivity(intent)
                }
            }
            item {
                AdminSettingsItem("About us", R.drawable.aboutus) {
                    val intent = Intent(context, AdminAboutUs::class.java)
                    context.startActivity(intent)
                }
            }
            item {
                AdminSettingsItem("Terms and Conditions", R.drawable.ic_termsandcondition) {
                    val intent = Intent(context, AdminTermsAndCondition::class.java)
                    context.startActivity(intent)
                }
            }
            item {
                AdminSettingsItem("Logout", R.drawable.signout) {
                    showLogoutDialog = true
                }
            }

            // Developer Section (Bottom)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Developer Tools",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                AdminSettingsItem("Revert Last Trade", R.drawable.ic_settings) {
                    showRevertDialog = true
                }
            }
        }
    }

    // Revert Trade Dialog
    if (showRevertDialog) {
        AlertDialog(
            onDismissRequest = { showRevertDialog = false },
            title = { Text("Revert Last Trade?") },
            text = { 
                Text(
                    "This will CLEAN REVERT the last COMPLETED trade:\n" +
                    "• DELETE Request (Trace removed)\n" +
                    "• Make items AVAILABLE again\n" +
                    "• Undo Points Transfer & Delete History\n" +
                    "• Delete related Notifications\n\n" +
                    "It will look like the trade never happened.",
                    fontSize = 14.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        isReverting = true
                        userNotificationViewModel.revertLastCompletedTrade { success, message, request ->
                            isReverting = false
                            showRevertDialog = false
                            if (success) {
                                android.widget.Toast.makeText(context, "Reverted: ${request?.productName}", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                android.widget.Toast.makeText(context, "Failed: $message", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isReverting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isReverting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Revert Now")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AdminThemeAwareLogoutDialog(
            onCancel = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                // Clear theme cache on logout
                ThemeManager.clear()
                // Sign out from Firebase
                FirebaseAuth.getInstance().signOut()
                // Clear any local preferences
                val sharedPreferences = context.getSharedPreferences("TradeFlowPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                // Navigate to login screen
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                // Finish all activities
                if (context is ComponentActivity) {
                    context.finishAffinity()
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AdminLanguageDialog(
            selectedLanguage = selectedLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { lang ->
                selectedLanguage = lang
                showLanguageDialog = false
            }
        )
    }

    if (showThemeDialog) {
        AdminThemeSelectionDialog(
            currentThemeMode = currentThemeMode,
            isLoading = isLoading,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { themeMode ->
                ThemeManager.setThemeMode(themeMode)
                isLoading = true
                showThemeDialog = false
                if (userId.isNotEmpty()) {
                    ThemeManager.saveTheme(userId, themeMode) { success, message ->
                        isLoading = false
                    }
                } else {
                    isLoading = false
                }
            }
        )
    }
}

@Composable
fun AdminThemeAwareLogoutDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Logout",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Do you really want to log out?",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Yes",
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "No",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun AdminThemeSettingsItem(
    currentThemeMode: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val themeDisplayName = when (currentThemeMode) {
        "light" -> "Light"
        "dark" -> "Dark"
        else -> "System Default"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = "Theme",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Theme",
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = themeDisplayName,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        thickness = 0.5.dp
    )
}

@Composable
fun AdminThemeSelectionDialog(
    currentThemeMode: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        "Light" to "light",
        "Dark" to "dark",
        "System Default" to "system"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Theme",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                themes.forEach { (displayName, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = !isLoading,
                                onClick = { onThemeSelected(value) }
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconRes = when (value) {
                            "light" -> R.drawable.ic_sun
                            "dark" -> R.drawable.ic_moon
                            else -> R.drawable.ic_settings
                        }

                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = displayName,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = displayName,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Checkmark if selected
                        if (value == currentThemeMode) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Divider except for last item
                    if (displayName != themes.last().first) {
                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 0.5.dp
                        )
                    }
                }

                // Loading indicator
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSettingsItem(title: String, iconRes: Int? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        thickness = 0.5.dp
    )
}

@Composable
fun AdminSettingsItemWithValue(title: String, value: String, iconRes: Int? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        thickness = 0.5.dp
    )
}

@Composable
fun AdminLanguageDialog(
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("English", "Nepali", "Hindi", "Chinese")
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Language",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
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
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (lang == selectedLanguage) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (lang != languages.last()) {
                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}