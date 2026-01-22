package com.example.tradeflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSavedItemsScreen(
    savedProducts: List<ProductModel>,
    onBackClick: () -> Unit,
    onProductClick: (ProductModel) -> Unit,
    onUnsaveClick: (ProductModel) -> Unit
) {
    Scaffold(
        topBar = {
            TradeFlowTopBar(
                title = {
                    Text(
                        text = "Saved Items",
                        color = White
                    )
                },
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (savedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved items yet",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedProducts) { product ->
                    ExploreItemCard(
                        product = product,
                        isSaved = true,
                        onFavoriteClick = { onUnsaveClick(product) },
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}
