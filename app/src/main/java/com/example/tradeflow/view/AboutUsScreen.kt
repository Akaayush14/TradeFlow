
package com.example.tradeflow
import android.os.Bundle
import android.util.Size
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tradeflow.view.CurvedBottomShape
import kotlinx.coroutines.launch

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

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("About Us", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF009688)
                )
            )
        },
        floatingActionButton = {
            if (listState.firstVisibleItemIndex > 0) {
                FloatingActionButton(
                    containerColor = Color(0xFF007D70),
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to Top",
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF5F8F8), Color(0xFFFCFDFD))
                    )
                )
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(CurvedBottomShape())
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF005F56),
                                    Color(0xFF007D70),
                                    Color(0xFF4DB6AC)
                                )
                            )
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .offset((-40).dp, (-30).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .offset(260.dp, 20.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.house_rent_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White)
                                .padding(16.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    "Trade Smarter, Live Better",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { SectionTitle("About Us") }

            item {
                Text(
                    text = "Welcome to TradeFlow! We are revolutionizing the way people exchange goods and services using a powerful credit-based system.",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
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
                    color = Color.DarkGray
                )
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                    onClick = {}
                ) {
                    Text("View Privacy Policy")
                }
            }

            item {
                Text(
                    "© 2024 TradeFlow. All rights reserved.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/* -------- COMPONENTS -------- */
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C63FF)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(description, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAboutUs() {
    AboutUsScreen(rememberNavController())
}