package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.ui.theme.Greenish


class AdminAboutUs : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
                AboutUsScreen(
                    onBackClick = {
                        val intent = Intent(this, AdminSettings::class.java)
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
fun AboutUsScreen(onBackClick: () -> Unit = {}) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
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
                            text = "About Us",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Container
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                clip = true
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant), // Light gray background
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.house_rent_logo),
                            contentDescription = "TradeFlow Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
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
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            item { SectionTitle("About Us") }

            item {
                Text(
                    text = "Welcome to TradeFlow! We are revolutionizing the way people exchange goods and services by creating a seamless platform where users can trade and rent items using our unique credit system.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }

            item {
                Text(
                    text = "Our mission is to build a sustainable sharing economy where everyone benefits. Instead of letting valuable items sit unused, TradeFlow empowers you to turn them into opportunities. Whether you're looking to trade electronics, rent equipment, or exchange services, our platform makes it simple, safe, and rewarding.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }

            item {
                Text(
                    text = "With TradeFlow Credits as our currency, you can participate in a vibrant marketplace without the need for traditional money. Earn credits by renting out your items or providing services, then use those credits to access what you need. It's that simple!",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }

            item { SectionTitle("What We Offer") }

            items(
                listOf(
                    "🔄 Easy Trading" to "Trade items effortlessly with our user-friendly platform",
                    "🏠 Flexible Rentals" to "Rent what you need, when you need it",
                    "💎 Credit System" to "Our unique currency makes transactions smooth and fair",
                    "🔒 Safe & Secure" to "Your transactions and data are protected"
                )
            ) { FeatureCard(it.first, it.second) }

            item { SectionTitle("Our Vision") }

            item {
                Text(
                    text = "We envision a world where resources are shared efficiently, reducing waste and building stronger communities. TradeFlow isn't just an app it's a movement towards conscious consumption and collaborative living.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
            }

            item { SectionTitle("Our Mission") }

            item {
                Text(
                    text = "To create a seamless platform for barter and rental transactions that empowers communities and promotes sustainable consumption.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
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
            containerColor = MaterialTheme.colorScheme.surface // Light gray background
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