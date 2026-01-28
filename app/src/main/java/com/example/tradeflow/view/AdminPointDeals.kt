package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.repository.PointDealRepoImpl
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.PointDealViewModel
import java.text.SimpleDateFormat
import java.util.*

class AdminPointDealsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminPointDealsScreen(
                onBackClick = {
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPointDealsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    //changes for user point deals
    val viewModel = remember { PointDealViewModel(PointDealRepoImpl(), UserRepoImpl()) }
    val allDeals by viewModel.allDeals.observeAsState(initial = null)
    val users by viewModel.users.observeAsState(initial = null)

    var showAddDialog by remember { mutableStateOf(false) }
    var dealToEdit by remember { mutableStateOf<PointDealModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getAllPointDeals()
        viewModel.getAllUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Point Deals", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    dealToEdit = null
                    showAddDialog = true 
                },
                containerColor = Greenish,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, "Add Deal")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (allDeals.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No deals found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allDeals!!) { deal ->
                        AdminDealCard(
                            deal = deal, 
                            onEdit = {
                                dealToEdit = deal
                                showAddDialog = true
                            },
                            onDelete = {
                                viewModel.deletePointDeal(deal.dealId) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) viewModel.getAllPointDeals()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDealDialog(
            users = users ?: emptyList(),
            dealToEdit = dealToEdit,
            onDismiss = { showAddDialog = false },
            onAdd = { deal ->
                viewModel.addPointDeal(deal) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showAddDialog = false
                        viewModel.getAllPointDeals()
                    }
                }
            },
            onUpdate = { deal ->
                viewModel.updatePointDeal(deal) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showAddDialog = false
                        viewModel.getAllPointDeals()
                    }
                }
            },
            onGiftUser = { userId, points, title ->
                viewModel.giftPointsToUser(userId, points, title) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showAddDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun AdminDealCard(deal: PointDealModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deal.offer, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${deal.tier} • ${deal.serviceCategory}", fontSize = 12.sp, color = Color.Gray)
                Text(text = "${deal.pointsRequired} Points", fontSize = 12.sp, color = Greenish, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDealDialog(
    users: List<UserModel>,
    dealToEdit: PointDealModel? = null,
    onDismiss: () -> Unit,
    onAdd: (PointDealModel) -> Unit,
    onUpdate: (PointDealModel) -> Unit,
    onGiftUser: (String, Long, String) -> Unit
) {
    var tier by remember { mutableStateOf(dealToEdit?.tier ?: "Bronze") }
    var dealType by remember { 
        mutableStateOf(
            if (dealToEdit != null) {
                if (dealToEdit.serviceCategory == "Admin Gift" || dealToEdit.rewardPoints > 0) "Gift Free Points" else "Discount Deal"
            } else "Gift Free Points"
        ) 
    }
    var pointsInput by remember { 
        mutableStateOf(
            if (dealToEdit != null) {
                if (dealToEdit.rewardPoints > 0) dealToEdit.rewardPoints.toString() else dealToEdit.pointsRequired.toString()
            } else ""
        ) 
    }
    var offerDescription by remember { mutableStateOf(dealToEdit?.offer ?: "") }
    
    // Default 7 days if creating new
    var validTillMillis by remember { 
        mutableLongStateOf(dealToEdit?.validTill ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) 
    }
    
    var validDate by remember { 
         mutableStateOf(
             if (dealToEdit != null) {
                 SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(dealToEdit.validTill))
             } else ""
         )
    }
    
    // User Selection State
    var targetUserId by remember { mutableStateOf("") }
    var selectedUserName by remember { mutableStateOf("") }
    var userSearchQuery by remember { mutableStateOf("") }
    var showUserDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            validTillMillis = calendar.timeInMillis
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            validDate = sdf.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (dealToEdit != null) "Edit Point Deal" else "Create Point Deal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Deal Type Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deal Type:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Gift Free Points", "Discount Deal").forEach { type ->
                            FilterChip(
                                selected = dealType == type,
                                onClick = { dealType = type },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE0E7FF),
                                    selectedLabelColor = Color(0xFF3F51B5)
                                )
                            )
                        }
                    }
                }

                // User Search Section (Only for Gift Free Points and New Deals)
                // If editing, we assume we are not changing the target user for a direct gift transaction
                if (dealType == "Gift Free Points" && dealToEdit == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("User", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = {
                                userSearchQuery = it
                                showUserDropdown = true
                            },
                            label = { Text("Search by Name or Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (showUserDropdown && userSearchQuery.isNotEmpty()) {
                            val filteredUsers = users.filter {
                                it.email.contains(userSearchQuery, ignoreCase = true) ||
                                it.name.contains(userSearchQuery, ignoreCase = true)
                            }.take(5)
                            if (filteredUsers.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    LazyColumn {
                                        items(filteredUsers) { user ->
                                            Text(
                                                text = "${user.name} (${user.email})",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        targetUserId = user.userId
                                                        selectedUserName = user.name
                                                        userSearchQuery = "${user.name} (${user.email})"
                                                        showUserDropdown = false
                                                    }
                                                    .padding(12.dp)
                                            )
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                        if (targetUserId.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Selected: $selectedUserName", color = Greenish, fontWeight = FontWeight.Bold)
                                TextButton(onClick = {
                                    targetUserId = ""
                                    selectedUserName = ""
                                    userSearchQuery = ""
                                }) {
                                    Text("Clear", color = Color.Red)
                                }
                            }
                        }
                    }
                }

                // User Tier Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User Tier:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Bronze", "Silver", "Gold").forEach { t ->
                            FilterChip(
                                selected = tier == t,
                                onClick = { tier = t },
                                label = { Text(t) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE0E7FF),
                                    selectedLabelColor = Color(0xFF3F51B5)
                                )
                            )
                        }
                    }
                }

                // Points Input Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Points",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = pointsInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) pointsInput = it },
                        placeholder = { Text("0") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                // Offer Description (Only for Discount Deal)
                if (dealType == "Discount Deal") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Offer Description:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        OutlinedTextField(
                            value = offerDescription,
                            onValueChange = { offerDescription = it },
                            placeholder = { Text("e.g. 50% Off on Premium") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }

                // Valid Till Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Valid Till:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    OutlinedTextField(
                        value = validDate.ifEmpty { "Select date" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, "Select Date", tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val points = pointsInput.toLongOrNull() ?: 0L
                    
                    if (points > 0) {
                        val isGift = dealType == "Gift Free Points"
                        
                        // Case 1: Direct Gift to User (New Transaction, not a Deal)
                        if (isGift && targetUserId.isNotEmpty() && dealToEdit == null) {
                            onGiftUser(targetUserId, points, "Admin Gift Points")
                        } 
                        // Case 2: Create/Update Point Deal
                        else {
                            val deal = PointDealModel(
                                dealId = dealToEdit?.dealId ?: "", // Preserve ID if editing
                                title = if (isGift) "$tier Reward" else "$tier Deal",
                                offer = if (isGift) "Claim $points Free Points!" else offerDescription.ifEmpty { "Redeem for $points Points" },
                                tier = tier,
                                serviceCategory = if (isGift) "Admin Gift" else "Discount",
                                pointsRequired = if (isGift) 0L else points,
                                validTill = validTillMillis,
                                isActive = true,
                                discountAmount = 0.0,
                                discountType = "FLAT",
                                rewardPoints = if (isGift) points else 0L,
                                targetUserId = "" // Deals are general usually, specific user targeting done via other means or separate logic
                            )
                            
                            if (dealToEdit != null) {
                                onUpdate(deal)
                            } else {
                                onAdd(deal)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter valid points", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D6F)), // Greenish color from image
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (dealType == "Gift Free Points" && targetUserId.isNotEmpty()) "Give Points" 
                           else if (dealToEdit != null) "Update Deal" 
                           else "Create Deal", 
                    color = White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF3F51B5))
            }
        },
        containerColor = Color(0xFFF5F5F7), // Light background like image
        shape = MaterialTheme.shapes.large
    )
}





