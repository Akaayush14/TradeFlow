package com.example.tradeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.ui.theme.TealBlue
import com.example.tradeflow.ui.theme.White

// Data model for listings, now includes an image resource ID
data class ListingItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: String,
    // Use Int to store the drawable resource ID (e.g., R.drawable.tshirt_image)
    val imageResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val listings = remember { getMockListings() }

    Scaffold(
        topBar = { ProfileTopAppBar() },
        containerColor = White
    ) { innerPadding ->
        Column( // The Column provides the overall structure below the Top Bar
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProfileHeaderSection()

            Text(
                text = "Listings",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // LazyColumn handles efficient vertical scrolling of the list items
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(listings) { item ->
                    ListingItemCard(item = item, onClick = { /* Handle item click */ })
                }
            }
        }
    }
}

// ... (ProfileTopAppBar function code remains the same as before) ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text("Profile", color = White, fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            IconButton(onClick = { /* Handle back press */ }) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_new_24),

                    contentDescription = "Back",
                    tint = White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = TealBlue,
            titleContentColor = White,
            navigationIconContentColor = White
        )
    )
}


// ... (ProfileHeaderSection function code remains the same as before) ...
@Composable
fun ProfileHeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Warning",
            color = Color(0xFFFF9800),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD))
                    .align(Alignment.Center)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.size(80.dp).align(Alignment.Center),
                    tint = Color(0xFF0288D1)
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(TealBlue)
                    .align(Alignment.BottomEnd)
                    .clickable { /* Handle edit avatar click */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Create, contentDescription = "Edit Profile", tint = White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Lucas Scott", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
        Text("@lucasscott3", fontSize = 14.sp, color = Color.Gray)
    }
}


@Composable
fun ListingItemCard(item: ListingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Image Display Area ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                // Use the drawable resource ID provided in the data model
                Image(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = "Listing Image for ${item.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // --------------------------

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                Text(item.description, fontSize = 14.sp, color = Color.Gray)
            }

            Text(
                text = item.price,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
        // Divider line
        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.LightGray))
    }
}


fun getMockListings(): List<ListingItem> {
    // Note: R.drawable.* references will only work if you add the corresponding files
    // to your project's res/drawable folder.
    return listOf(
        ListingItem(1, "Amazing T-shirt", "Trade via Credits", "€ 12.00", R.drawable.tshirt),
        ListingItem(2, "Fabulous Pants", "Trade via Credits", "€ 15.00", R.drawable.pant),
        ListingItem(3, "Spectacular Dress", "Trade via Credits", "€ 20.00", R.drawable.dress),
        ListingItem(4, "Cool Sneakers", "Trade via Credits", "€ 45.00", R.drawable.sneakers), // Re-using image
        ListingItem(5, "Stylish Jacket", "Trade via Credits", "€ 60.00", R.drawable.jacket), // Re-using image
        ListingItem(6, "Summer Hat", "Trade via Credits", "€ 8.00", R.drawable.hat), // Re-using image
    )
}
