package com.example.tradeflow.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.tradeflow.ui.theme.TradeFlowTheme
import com.example.tradeflow.util.ThemeManager
import com.example.tradeflow.utils.ThemeManager


class AdminSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize theme
        ThemeManager.init(this)

        setContent {
            TradeFlowTheme(darkTheme = ThemeManager.isDarkMode) {
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
    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }

    // Handle back button press - navigate to AdminDashExp
    BackHandler {
        val intent = Intent(context, AdminDashExp::class.java)
        context.startActivity(intent)
        if (context is ComponentActivity) {
            context.finish()
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
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
                    onClick = {
                        // Clear user session/preferences
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

                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Yes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "No",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Greenish,
                    titleContentColor = if (isDarkMode) MaterialTheme.colorScheme.onSurface else DarkGreen,
                    navigationIconContentColor = if (isDarkMode) MaterialTheme.colorScheme.onSurface else DarkGreen
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = if (isDarkMode) MaterialTheme.colorScheme.onSurface else Color.White
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
                            text = "Settings",
                            color = if (isDarkMode) MaterialTheme.colorScheme.onSurface else White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            SettingsContent(
                isDarkMode = isDarkMode,
                onDarkModeToggle = { enabled ->
                    ThemeManager.setDarkMode(context, enabled)
                    isDarkMode = enabled
                },
                onLogoutClick = { showLogoutDialog = true }
            )
        }
    }
}

@Composable
fun SettingsContent(
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 220.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Dark Mode Toggle
            SettingsMenuItemWithSwitch(
                title = "Dark Mode",
                iconRes = R.drawable.ic_termsandcondition, // Replace with dark mode icon if available
                isChecked = isDarkMode,
                onCheckedChange = onDarkModeToggle
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            SettingsMenuItem(
                title = "Notifications",
                iconRes = R.drawable.notification,
                onClick = {
                    val intent = Intent(context, AdminNotification::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            SettingsMenuItem(
                title = "Edit Profile",
                iconRes = R.drawable.ic_profile,
                onClick = {
                    val intent = Intent(context, EditAdminProfile::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            SettingsMenuItem(
                title = "About us",
                iconRes = R.drawable.ic_termsandcondition,
                onClick = {
                    val intent = Intent(context, AdminAboutUs::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            SettingsMenuItem(
                title = "Privacy & Security",
                iconRes = R.drawable.ic_termsandcondition,
                onClick = {
                    val intent = Intent(context, AdminPrivacyPolicy::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            SettingsMenuItem(
                title = "Terms and Conditions",
                iconRes = R.drawable.ic_termsandcondition,
                onClick = {
                    val intent = Intent(context, AdminTermsAndCondition::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            SettingsMenuItem(
                title = "Logout",
                iconRes = R.drawable.signout,
                onClick = onLogoutClick,
                showArrow = false
            )
        }
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    iconRes: Int? = null,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (showArrow) {
            // No trailing arrow per spec
        }
    }
    @Composable
    fun SettingsMenuItemWithSwitch(
        title: String,
        iconRes: Int? = null,
        isChecked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                iconRes?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = title,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Greenish,
                    checkedTrackColor = Greenish.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }

}
