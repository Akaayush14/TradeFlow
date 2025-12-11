package com.example.tradeflow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Green

class AdminSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminSettingsScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(-1) } // No tab selected for Settings

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                            .padding(end = 48.dp), // Compensate for back button width
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Settings",
                            color = DarkGreen,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Green) {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = {
                        val intent = Intent(context, AdminDashExp::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_explore),
                            contentDescription = "Explore",
                            tint = Color.White
                        )
                    },
                    label = { Text("Explore", color = Color.White) }
                )

                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = {
                        val intent = Intent(context, AdminHistory::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_history),
                            contentDescription = "History",
                            tint = Color.White
                        )
                    },
                    label = { Text("History", color = Color.White) }
                )

                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = {
                        val intent = Intent(context, AdminProfile::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.profile),
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    },
                    label = { Text("Profile", color = Color.White) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SettingsContent()
        }
    }
}

@Composable
fun SettingsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings Screen Content")
    }
}
