package com.example.tradeflow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish


class AdminAboutUs : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AboutUsScreen(onBackClick: () -> Unit = {}) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
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
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Title
            Text(
                text = "TradeFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Greenish
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Trade Smarter, Live Better",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // About Us Section
            SectionTitle(text = "About Us")

            ParagraphText(
                text = "Welcome to TradeFlow! We are revolutionizing the way people exchange goods and services by creating a seamless platform where users can trade and rent items using our unique credit system."
            )

            ParagraphText(
                text = "Our mission is to build a sustainable sharing economy where everyone benefits. Instead of letting valuable items sit unused, TradeFlow empowers you to turn them into opportunities. Whether you're looking to trade electronics, rent equipment, or exchange services, our platform makes it simple, safe, and rewarding."
            )

            ParagraphText(
                text = "With TradeFlow Credits as our currency, you can participate in a vibrant marketplace without the need for traditional money. Earn credits by renting out your items or providing services, then use those credits to access what you need. It's that simple!",
                bottomPadding = 32.dp
            )

            // What We Offer Section
            SectionTitle(text = "What We Offer")

            FeatureCard(
                emoji = "🔄",
                title = "Easy Trading",
                description = "Trade items effortlessly with our user-friendly platform"
            )

            FeatureCard(
                emoji = "🏠",
                title = "Flexible Rentals",
                description = "Rent what you need, when you need it"
            )

            FeatureCard(
                emoji = "💎",
                title = "Credit System",
                description = "Our unique currency makes transactions smooth and fair"
            )

            FeatureCard(
                emoji = "🔒",
                title = "Safe & Secure",
                description = "Your transactions and data are protected",
                bottomPadding = 32.dp
            )

            // Our Vision Section
            SectionTitle(text = "Our Vision")

            ParagraphText(
                text = "We envision a world where resources are shared efficiently, reducing waste and building stronger communities. TradeFlow isn't just an app—it's a movement towards conscious consumption and collaborative living.",
                bottomPadding = 32.dp
            )

            // Footer
            Text(
                text = "© 2024 TradeFlow. All rights reserved.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Greenish,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

@Composable
fun ParagraphText(
    text: String,
    bottomPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = Color(0xFF333333),
        lineHeight = 24.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
    )
}

@Composable
fun FeatureCard(
    emoji: String,
    title: String,
    description: String,
    bottomPadding: androidx.compose.ui.unit.Dp = 12.dp
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(
                text = emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Greenish,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}