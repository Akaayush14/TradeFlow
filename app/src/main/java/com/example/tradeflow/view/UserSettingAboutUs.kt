package com.example.tradeflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeflow.ui.components.ThemeWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingAboutUsScreen(navController: NavController) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "About Us",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Option 1: If your logo has a proper background and fills the space
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                clip = true
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.house_rent_logo),
                            contentDescription = "TradeFlow Logo",
                            modifier = Modifier.fillMaxSize(), // Fills the entire container
                            contentScale = ContentScale.Fit // Keeps aspect ratio but fits within bounds
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "TradeFlow",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Trade Smarter, Live Better",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item { SectionTitle("About Us") }

            item {
                Text(
                    text = "Welcome to TradeFlow! We are revolutionizing the way people exchange goods and services using a powerful credit-based system.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }

            item { SectionTitle("What We Offer") }

            items(
                listOf(
                    "🔁 Easy Trading" to "Trade items effortlessly",
                    "🏠 Flexible Rentals" to "Rent when you need",
                    "💎 Credit System" to "Fair & simple currency",
                    "🔒 Secure" to "Safe transactions"
                )
            ) { FeatureCard(it.first, it.second) }

            item { SectionTitle("Our Vision") }

            item {
                Text(
                    "A world where resources are shared efficiently and sustainably.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }

            item { SectionTitle("Our Mission") }

            item {
                Text(
                    "To create a seamless platform for barter and rental transactions that empowers communities and promotes sustainable consumption.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }

            item {
                Text(
                    "© 2024 TradeFlow. All rights reserved.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun FeatureCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji/Icon
            Text(
                text = title.takeWhile { !it.isWhitespace() },
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column {
                Text(
                    text = title.dropWhile { !it.isWhitespace() }.trim(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAboutUs() {
    ThemeWrapper {
        UserSettingAboutUsScreen(rememberNavController())
    }
}