package com.example.tradeflow.view

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.viewmodel.ProductViewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.font.FontWeight
import com.example.tradeflow.R

// Add this annotation to use experimental Material 3 APIs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen() {
    val context = LocalContext.current
    val activity = context as Activity

    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val productViewModel: ProductViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    
    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    val allProducts by productViewModel.allProducts.observeAsState(initial = emptyList())

    val filteredProducts = allProducts?.filter { product ->
        val matchesTab = when (selectedTab) {
            "Rent" -> product.type == "Rent"
            "Trade" -> product.type == "Barter"
            else -> true
        }
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch && !product.isDeleted
    } ?: emptyList()

    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search items...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = White
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = White
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                            disabledContainerColor = Color.White.copy(alpha = 0.15f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = White,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        textStyle = TextStyle(
                            color = White,
                            fontSize = 16.sp
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true)
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(activity, SettingScreenActivity::class.java)
                        activity.startActivity(intent)
                    }) {
                        // Use Icons.Default.Menu or your custom resource
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                }
            )
        }

    ) { paddingValues ->
        // The main content uses the padding provided by the Scaffold (for the top bar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply the padding here
                .background(color = White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf("All","Rent","Trade")
                tabs.forEach { tabName ->
                    val isSelected = selectedTab == tabName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(if (isSelected) White else Color(0xFFF0F0F0))
                            .clickable { selectedTab = tabName },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            color = if (isSelected) Color.Black else Color.Gray
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(filteredProducts) { product ->
                    ExploreItemCard(
                        product = product,
                        onClick = {
                            val intent = Intent(context, ItemDetailsActivity::class.java)
                            intent.putExtra("productId", product.productId)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ExploreItemCard(product: ProductModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder Image
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
             Icon(
                imageVector = Icons.Default.Search, // Placeholder icon
                contentDescription = null,
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = product.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$${product.price}", fontSize = 16.sp, color = Greenish, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                Text(text = product.status, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
