package com.example.tradeflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items

class AboutUsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            AboutUsScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(navController: NavController) {

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF5F8F8),
            Color(0xFFFCFDFD)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("About Us") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF009688)
                )
            )
        },
        floatingActionButton = {
            if (scrollState.value > 300) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    },
                    containerColor = Color(0xFF007D70)

                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll Up",
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007D70)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.house_rent_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
            }


            item {
                Text(
                    text = "Trade Smarter, Live Better",
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                SectionTitle("About Us")
            }

            item {
                Text(
                    text = "Welcome to TradeFlow! ...",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }

            item {
                SectionTitle("What We Offer")
            }

            items(
                listOf(
                    "🔁 Easy Trading" to "Trade items effortlessly with our user-friendly platform",
                    "🏠 Flexible Rentals" to "Rent what you need, when you need it",
                    "💎 Credit System" to "Our unique currency makes transactions smooth and fair",
                    "🔒 Safe & Secure" to "Your transactions and data are protected"
                )
            ) { (title, desc) ->
                FeatureCard(title, desc)
            }

            item {
                SectionTitle("Our Vision")
            }

            item {
                Text(
                    text = "We envision a world where resources are shared efficiently...",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }

            item {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                ) {
                    Text("View Privacy Policy")
                }
            }

            item {
                Text(
                    text = "© 2024 TradeFlow. All rights reserved.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6C63FF)

    )

}

@Composable
fun FeatureCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C63FF)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAboutUs() {
    AboutUsScreen(rememberNavController())
}
