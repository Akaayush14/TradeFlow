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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.viewmodel.UserViewModel

class AdminDashUser : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminUserScreen(
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
fun AdminUserScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(1) } // History tab selected
    var selectedTab by remember { mutableStateOf(0) } // 0 for None, 1 for Restricted, 2 for Blocked

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
                            text = "User",
                            color = DarkGreen,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        },

        bottomBar = {
            NavigationBar(containerColor = Greenish) {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = {
                        selectedIndex = 0
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
                    onClick = { selectedIndex = 1 }, // Already on User
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_user),
                            contentDescription = "User",
                            tint = Color.White
                        )
                    },
                    label = { Text("User", color = Color.White) }
                )
                NavigationBarItem(
                    selected = selectedIndex == 3,
                    onClick = {
                        val intent = Intent(context, AdminDashItem::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_items),
                            contentDescription = "Items",
                            tint = Color.White
                        )
                    },
                    label = { Text("Items", color = Color.White) }
                )

                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = {
                        selectedIndex = 2
                        val intent = Intent(context, AdminProfile::class.java)
                        context.startActivity(intent)
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_profile),
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    },
                    label = { Text("Profile", color = Color.White) }
                )



            }

        }


    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tab Row for None, Restricted, and Blocked
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
                            "None",
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
                            "Restricted",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 1) Color.Black else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Blocked",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 2) Color.Black else Color.Gray
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
                    0 -> NoneContent()
                    1 -> RestrictedContent()
                    2 -> BlockedContent()
                }
            }
        }
    }
}

@Composable
fun NoneContent() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val allUsers by userViewModel.allUsers.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<UserModel?>(null) }
    var showBlockDialog by remember { mutableStateOf<UserModel?>(null) }
    var showRestrictDialog by remember { mutableStateOf<UserModel?>(null) }
    
    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }
    
    // Filter users with no restrictions and no blocks (normal users)
    val noneUsers = allUsers?.filter { !it.isBlocked && !it.isRestricted } ?: emptyList()
    
    if (noneUsers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No users",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = noneUsers,
                key = { it.userId }
            ) { user ->
                UserCardUser(
                    user = user,
                    onDeleteClick = { showDeleteDialog = user },
                    onBlockClick = { showBlockDialog = user },
                    onRestrictClick = { showRestrictDialog = user }
                )
            }
        }
    }
    
    // Dialogs
    UserDialogs(
        showDeleteDialog = showDeleteDialog,
        showBlockDialog = showBlockDialog,
        showRestrictDialog = showRestrictDialog,
        onDismissDelete = { showDeleteDialog = null },
        onDismissBlock = { showBlockDialog = null },
        onDismissRestrict = { showRestrictDialog = null },
        userViewModel = userViewModel
    )
}

@Composable
fun RestrictedContent() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val allUsers by userViewModel.allUsers.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<UserModel?>(null) }
    var showBlockDialog by remember { mutableStateOf<UserModel?>(null) }
    var showRestrictDialog by remember { mutableStateOf<UserModel?>(null) }
    
    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }
    
    // Filter restricted users
    val restrictedUsers = allUsers?.filter { it.isRestricted } ?: emptyList()
    
    if (restrictedUsers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No restricted users",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = restrictedUsers,
                key = { it.userId }
            ) { user ->
                UserCardUser(
                    user = user,
                    onDeleteClick = { showDeleteDialog = user },
                    onBlockClick = { showBlockDialog = user },
                    onRestrictClick = { showRestrictDialog = user }
                )
            }
        }
    }
    
    // Dialogs
    UserDialogs(
        showDeleteDialog = showDeleteDialog,
        showBlockDialog = showBlockDialog,
        showRestrictDialog = showRestrictDialog,
        onDismissDelete = { showDeleteDialog = null },
        onDismissBlock = { showBlockDialog = null },
        onDismissRestrict = { showRestrictDialog = null },
        userViewModel = userViewModel
    )
}

@Composable
fun BlockedContent() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val allUsers by userViewModel.allUsers.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<UserModel?>(null) }
    var showBlockDialog by remember { mutableStateOf<UserModel?>(null) }
    var showRestrictDialog by remember { mutableStateOf<UserModel?>(null) }
    
    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }
    
    // Filter blocked users
    val blockedUsers = allUsers?.filter { it.isBlocked } ?: emptyList()
    
    if (blockedUsers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No blocked users",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = blockedUsers,
                key = { it.userId }
            ) { user ->
                UserCardUser(
                    user = user,
                    onDeleteClick = { showDeleteDialog = user },
                    onBlockClick = { showBlockDialog = user },
                    onRestrictClick = { showRestrictDialog = user }
                )
            }
        }
    }
    
    // Dialogs
    UserDialogs(
        showDeleteDialog = showDeleteDialog,
        showBlockDialog = showBlockDialog,
        showRestrictDialog = showRestrictDialog,
        onDismissDelete = { showDeleteDialog = null },
        onDismissBlock = { showBlockDialog = null },
        onDismissRestrict = { showRestrictDialog = null },
        userViewModel = userViewModel
    )
}

@Composable
fun UserCardUser(
    user: UserModel,
    onDeleteClick: () -> Unit,
    onBlockClick: () -> Unit,
    onRestrictClick: () -> Unit
) {
    // Determine card color: blocked = light red, restricted = light orange, normal = white
    val cardColor = when {
        user.isBlocked -> Color(0xFFFFEBEE) // Light red
        user.isRestricted -> Color(0xFFFFF3E0) // Light orange
        else -> Color.White
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Name
            Text(
                text = user.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Email
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = Color.Gray
            )
            // Status labels
            if (user.isBlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BLOCKED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
            if (user.isRestricted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RESTRICTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800) // Orange
                )
            }
            // Buttons at the bottom
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Block/Unblock button
                Button(
                    onClick = onBlockClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) DarkGreen else Color.Red
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (user.isBlocked) "Unblock" else "Block",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                // Restrict/Unrestrict button
                Button(
                    onClick = onRestrictClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRestricted) DarkGreen else Color(0xFFFF9800) // Orange
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (user.isRestricted) "Unrestrict" else "Restrict",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                // Delete button
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UserDialogs(
    showDeleteDialog: UserModel?,
    showBlockDialog: UserModel?,
    showRestrictDialog: UserModel?,
    onDismissDelete: () -> Unit,
    onDismissBlock: () -> Unit,
    onDismissRestrict: () -> Unit,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${user.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.deleteUser(user.userId) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                userViewModel.getAllUser()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            onDismissDelete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    
    // Block/Unblock Dialog
    showBlockDialog?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissBlock,
            title = { Text(if (user.isBlocked) "Unblock User" else "Block User") },
            text = { 
                Text("Are you sure you want to ${if (user.isBlocked) "unblock" else "block"} ${user.name}?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.blockUser(user.userId, !user.isBlocked) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                userViewModel.getAllUser()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            onDismissBlock()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) DarkGreen else Color.Red
                    )
                ) {
                    Text(if (user.isBlocked) "Unblock" else "Block", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissBlock,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    
    // Restrict/Unrestrict Dialog
    showRestrictDialog?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissRestrict,
            title = { Text(if (user.isRestricted) "Unrestrict User" else "Restrict User") },
            text = { 
                Text("Are you sure you want to ${if (user.isRestricted) "unrestrict" else "restrict"} ${user.name}?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.restrictUser(user.userId, !user.isRestricted) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                userViewModel.getAllUser()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            onDismissRestrict()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRestricted) DarkGreen else Color(0xFFFF9800) // Orange
                    )
                ) {
                    Text(if (user.isRestricted) "Unrestrict" else "Restrict", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissRestrict,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}