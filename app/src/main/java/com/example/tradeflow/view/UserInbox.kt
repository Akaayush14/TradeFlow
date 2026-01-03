package com.example.tradeflow.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.tradeflow.R
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import kotlinx.coroutines.launch

// Data model for messages
data class MessagePreview(
    val id: Int,
    val senderName: String,
    val lastMessage: String,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(onBackClick: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    val messages = remember { getMockMessages() }

    Scaffold(
        topBar = {
            InboxTopAppBar(onBackClick = onBackClick)
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(White)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Greenish
                ),
                shape = RoundedCornerShape(50.dp)
            )


            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(messages) { message ->
                    MessageItem(message = message, onClick = {  })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxTopAppBar(onBackClick: () -> Unit = {}) {
    // Use CenterAlignedTopAppBar for a centered title
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Inbox",
                color = White,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_new_24),
                    contentDescription = "Back",
                    tint = White
                )
            }
        },
        // Use the appropriate TopAppBarDefaults colors function
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Greenish,
            titleContentColor = White,
            navigationIconContentColor = White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(message: MessagePreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE))
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier.align(Alignment.Center),
                tint = Color(0xFF0288D1)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.senderName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = message.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
            )
        }

        if (message.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            // The blue circle badge exactly as in the image
            Badge(
                containerColor = Color(0xFF3B82F6)
            ) {
                Text(
                    text = message.unreadCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}


fun getMockMessages(): List<MessagePreview> {
    return listOf(
        MessagePreview(1, "Haley James", "Stand up for what you believe in", 9),
        MessagePreview(2, "Nathan Scott", "One day you're seventeen and planning for someday. And then quietly and without...", 0),
        MessagePreview(3, "Brooke Davis", "I am who I am. No excuses.", 2),
        MessagePreview(4, "Jamie Scott", "Some people are a little different. I think that's cool.", 0),
        MessagePreview(5, "Marvin McFadden", "Last night in the NBA the Charlotte Bobcats quietly made a move that most sports fans...", 0),
        MessagePreview(6, "Antwon Taylor", "Meet me at the Rivercourt", 0),
    )
}

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
                    Brush.Companion.verticalGradient(
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
                            Brush.Companion.verticalGradient(
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
                                .size(140.dp)
                                .clip(CircleShape)
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
                    textAlign = TextAlign.Companion.Center,
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
                    textAlign = TextAlign.Companion.Justify
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
                    textAlign = TextAlign.Companion.Center,
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