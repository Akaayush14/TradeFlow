package com.example.tradeflow.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import com.example.tradeflow.R
import com.example.tradeflow.model.PointDealModel
import com.example.tradeflow.repository.PointDealRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.PointDealViewModel
import java.text.SimpleDateFormat
import java.util.*

class AdminPointDeals : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AdminPointDealsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPointDealsScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val pointDealViewModel = remember { PointDealViewModel(PointDealRepoImpl()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDeal by remember { mutableStateOf<PointDealModel?>(null) }

    LaunchedEffect(Unit) {
        pointDealViewModel.getAllPointDeals()
    }

    val allDeals by pointDealViewModel.allDeals.observeAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Point Deals", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish
                ),
                actions = {
                    IconButton(onClick = {
                        editingDeal = null
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, "Add Deal", tint = White)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allDeals ?: emptyList()) { deal ->
                AdminPointDealCard(
                    deal = deal,
                    onEdit = {
                        editingDeal = deal
                        showAddDialog = true
                    },
                    onDelete = {
                        pointDealViewModel.deletePointDeal(deal.dealId) { success, message ->
                            if (success) {
                                pointDealViewModel.getAllPointDeals()
                            }
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddEditPointDealDialog(
                deal = editingDeal,
                onDismiss = {
                    showAddDialog = false
                    editingDeal = null
                },
                onSave = { deal ->
                    if (editingDeal != null) {
                        pointDealViewModel.updatePointDeal(deal) { success, message ->
                            if (success) {
                                pointDealViewModel.getAllPointDeals()
                                showAddDialog = false
                                editingDeal = null
                            }
                        }
                    } else {
                        pointDealViewModel.addPointDeal(deal) { success, message ->
                            if (success) {
                                pointDealViewModel.getAllPointDeals()
                                showAddDialog = false
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AdminPointDealCard(
    deal: PointDealModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val validTillDate = dateFormat.format(Date(deal.validTill))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${deal.tier} DEAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deal.offer,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Category: ${deal.serviceCategory}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Points Required: ${deal.pointsRequired}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Valid till: $validTillDate",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Status: ${if (deal.isActive) "Active" else "Inactive"}",
                        fontSize = 12.sp,
                        color = if (deal.isActive) Greenish else Color.Red
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit", tint = Greenish)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPointDealDialog(
    deal: PointDealModel?,
    onDismiss: () -> Unit,
    onSave: (PointDealModel) -> Unit
) {
    var title by remember { mutableStateOf(deal?.title ?: "") }
    var offer by remember { mutableStateOf(deal?.offer ?: "") }
    var tier by remember { mutableStateOf(deal?.tier ?: "Bronze") }
    var serviceCategory by remember { mutableStateOf(deal?.serviceCategory ?: "") }
    var pointsRequired by remember { mutableStateOf(deal?.pointsRequired?.toString() ?: "") }
    var discountAmount by remember { mutableStateOf(deal?.discountAmount?.toString() ?: "") }
    var discountType by remember { mutableStateOf(deal?.discountType ?: "FLAT") }
    var validTillDays by remember { mutableStateOf("30") }
    var isActive by remember { mutableStateOf(deal?.isActive ?: true) }

    val tiers = listOf("Bronze", "Silver", "Gold")
    val discountTypes = listOf("FLAT", "UPTO")
    var tierExpanded by remember { mutableStateOf(false) }
    var discountTypeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (deal == null) "Add Point Deal" else "Edit Point Deal", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = offer,
                    onValueChange = { offer = it },
                    label = { Text("Offer (e.g., FLAT $15 OFF!! or FLAT Rs.15 OFF!!)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter offer text with $ or Rs. amount") }
                )

                // Tier Dropdown
                ExposedDropdownMenuBox(
                    expanded = tierExpanded,
                    onExpandedChange = { tierExpanded = !tierExpanded }
                ) {
                    OutlinedTextField(
                        value = tier,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tier") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tierExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = tierExpanded,
                        onDismissRequest = { tierExpanded = false }
                    ) {
                        tiers.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    tier = t
                                    tierExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = serviceCategory,
                    onValueChange = { serviceCategory = it },
                    label = { Text("Service Category (e.g., BIKE, FOOD, CAR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pointsRequired,
                    onValueChange = { pointsRequired = it },
                    label = { Text("Points Required") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Discount Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = discountTypeExpanded,
                    onExpandedChange = { discountTypeExpanded = !discountTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = discountType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Discount Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = discountTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = discountTypeExpanded,
                        onDismissRequest = { discountTypeExpanded = false }
                    ) {
                        discountTypes.forEach { dt ->
                            DropdownMenuItem(
                                text = { Text(dt) },
                                onClick = {
                                    discountType = dt
                                    discountTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = discountAmount,
                    onValueChange = { discountAmount = it },
                    label = { Text("Discount Amount (in Dollars $)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = { Text("e.g., 15.0 (will show as $15)") },
                    supportingText = {
                        Text(
                            "Note: Since your items use dollar pricing, discounts are in dollars ($)",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                )

                OutlinedTextField(
                    value = validTillDays,
                    onValueChange = { validTillDays = it },
                    label = { Text("Valid Till (days from now)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text("Active")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Auto-generate offer text if not provided, using dollar format
                    val finalOffer = if (offer.isBlank() && discountAmount.isNotBlank()) {
                        val amount = discountAmount.toDoubleOrNull() ?: 0.0
                        if (discountType == "FLAT") {
                            "FLAT $${String.format("%.0f", amount)} OFF!!"
                        } else {
                            "UPTO $${String.format("%.0f", amount)} OFF!!"
                        }
                    } else {
                        offer
                    }

                    val dealModel = PointDealModel(
                        dealId = deal?.dealId ?: "",
                        title = title.ifBlank { "${tier} Deal" },
                        offer = finalOffer,
                        tier = tier,
                        serviceCategory = serviceCategory,
                        pointsRequired = pointsRequired.toLongOrNull() ?: 0L,
                        validTill = System.currentTimeMillis() + (validTillDays.toLongOrNull() ?: 30L) * 24 * 60 * 60 * 1000,
                        isActive = isActive,
                        discountAmount = discountAmount.toDoubleOrNull() ?: 0.0,
                        discountType = discountType,
                        createdAt = deal?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(dealModel)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Greenish)
            ) {
                Text("Save", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}