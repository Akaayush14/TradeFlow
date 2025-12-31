
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF5F8F8), Color(0xFFFCFDFD))
                )
            ),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(CurvedBottomShape())
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF006D64), Color(0xFF009688))
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .offset((-40).dp, (-20).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .offset(260.dp, 30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 1.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007D70)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.house_rent_logo),
                        contentDescription = "TradeFlow Logo",
                        modifier = Modifier.size(120.dp)
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




@Composable
fun SectionTitle(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6C63FF),
            textAlign = TextAlign.Center
        )
    }
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