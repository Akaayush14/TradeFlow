package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.AdminModel
import com.example.tradeflow.repository.AdminRepoImpl
import com.example.tradeflow.theme.ThemeManager
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
                AdminProfileScreen(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current

    // Admin view model for fetching profile details
    val adminViewModel = remember { AdminViewModel(AdminRepoImpl()) }
    val admin by adminViewModel.admin.collectAsState()
    val currentUser = adminViewModel.getCurrentUser()

    // Load admin data
    LaunchedEffect(Unit) {
        currentUser?.let {
            adminViewModel.getAdminById(it.uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, EditAdminProfile::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (admin != null) {
                ProfileContent(admin!!)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Greenish)
                }
            }
        }
    }
}

@Composable
fun ProfileContent(admin: AdminModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Picture only (no label below)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 3.dp,
                        color = Greenish,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (admin.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = admin.imageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholderimage),
                        error = painterResource(R.drawable.ic_profile)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(60.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Greenish)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personal Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Personal Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Name
                ProfileDetailItem(
                    icon = Icons.Default.Person,
                    label = "Name",
                    value = admin.name.ifEmpty { "Not set" }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Email
                ProfileDetailItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = admin.email.ifEmpty { "Not set" }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Phone
                ProfileDetailItem(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = admin.phone.ifEmpty { "Not set" }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Date of Birth
                ProfileDetailItem(
                    icon = Icons.Default.DateRange,
                    label = "Date of Birth",
                    value = admin.dateOfBirth.ifEmpty { "Not set" }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Gender
                ProfileDetailItem(
                    icon = Icons.Default.Person,
                    label = "Gender",
                    value = admin.gender.ifEmpty { "Not set" }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Location
                ProfileDetailItem(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    value = admin.location.ifEmpty { "Not set" }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Greenish,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }
    }
}