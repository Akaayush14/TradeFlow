package com.example.tradeflow.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import com.example.tradeflow.repository.PointDealRepoImpl
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
    val viewModel = remember { PointDealViewModel(PointDealRepoImpl()) }
    val allDeals by viewModel.allDeals.observeAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getAllPointDeals()
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
                onClick = { showAddDialog = true },
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
                        AdminDealCard(deal = deal, onDelete = {
                            viewModel.deletePointDeal(deal.dealId) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) viewModel.getAllPointDeals()
                            }
                        })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDealDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { deal ->
                viewModel.addPointDeal(deal) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showAddDialog = false
                        viewModel.getAllPointDeals()
                    }
                }
            }
        )
    }
}

@Composable
fun AdminDealCard(deal: PointDealModel, onDelete: () -> Unit) {
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
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDealDialog(onDismiss: () -> Unit, onAdd: (PointDealModel) -> Unit) {
    var offer by remember { mutableStateOf("") }
    var tier by remember { mutableStateOf("Bronze") }
    var category by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf("") }  // ADD THIS
    var discountType by remember { mutableStateOf("FLAT") }  // ADD THIS
    var validDate by remember { mutableStateOf("") }
    var validTillMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) } // Default 7 days

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            validTillMillis = calendar.timeInMillis
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            validDate = sdf.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Point Deal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = offer,
                    onValueChange = { offer = it },
                    label = { Text("Offer (e.g. FLAT Rs.15 OFF!!)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Tier Dropdown (Simplified as RadioRow for now)
                Text("Tier:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Bronze", "Silver", "Gold").forEach { t ->
                        FilterChip(
                            selected = tier == t,
                            onClick = { tier = t },
                            label = { Text(t) }
                        )
                    }
                }

                // Category Dropdown
                Text("Category:")

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Enter category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = points,
                    onValueChange = { if (it.all { char -> char.isDigit() }) points = it },
                    label = { Text("Points Required") },
                    modifier = Modifier.fillMaxWidth()
                )

                // ADD DISCOUNT FIELDS HERE - BETWEEN POINTS AND VALID TILL
                OutlinedTextField(
                    value = discountAmount,
                    onValueChange = { discountAmount = it },
                    label = { Text("Discount Amount") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Discount Type:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("FLAT", "UPTO").forEach { type ->
                        FilterChip(
                            selected = discountType == type,
                            onClick = { discountType = type },
                            label = { Text(type) }
                        )
                    }
                }

                OutlinedTextField(
                    value = validDate,
                    onValueChange = {},
                    label = { Text("Valid Till") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (offer.isNotEmpty() && points.isNotEmpty()) {
                        val deal = PointDealModel(
                            // REMOVE: dealId = UUID.randomUUID().toString(),
                            title = "${tier} DEAL",
                            offer = offer,
                            tier = tier,
                            serviceCategory = category,
                            pointsRequired = points.toLongOrNull() ?: 0L,
                            validTill = validTillMillis,
                            isActive = true,
                            discountAmount = discountAmount.toDoubleOrNull() ?: 0.0,  // ADD THIS
                            discountType = discountType  // ADD THIS
                        )
                        onAdd(deal)
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Greenish)
            ) {
                Text("Add", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}





