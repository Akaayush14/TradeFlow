package com.example.tradeflow

import android.content.Intent
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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.ui.theme.TealBlue
import com.example.tradeflow.ui.theme.White

// Data model for listings, now includes an image resource ID and type
enum class ListingType { BARTER, RENTAL }

data class ListingItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: String,
    // Use Int to store the drawable resource ID (e.g., R.drawable.tshirt_image)
    val imageResId: Int,
    val type: ListingType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val listings = remember { getMockListings() }
    var selectedTab by remember { mutableStateOf(ListingType.BARTER) }

    val barterListings = remember(listings) { listings.filter { it.type == ListingType.BARTER } }
    val rentalListings = remember(listings) { listings.filter { it.type == ListingType.RENTAL } }

    Scaffold(
        topBar = { ProfileTopAppBar() },
        containerColor = White
    ) { innerPadding ->
        // Single scrollable column so header + listings scroll together
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                ProfileHeaderSection(
                    barterCount = barterListings.size,
                    rentalCount = rentalListings.size,
                    completedCount = 12 // mock value for now
                )

                // Tabs for Barter / Rental listings
                TabRow(
                    selectedTabIndex = if (selectedTab == ListingType.BARTER) 0 else 1,
                    containerColor = White
                ) {
                    Tab(
                        selected = selectedTab == ListingType.BARTER,
                        onClick = { selectedTab = ListingType.BARTER },
                        text = { Text("Barter", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == ListingType.RENTAL,
                        onClick = { selectedTab = ListingType.RENTAL },
                        text = { Text("Rental", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                // Listing section title
                Text(
                    text = if (selectedTab == ListingType.BARTER) "My Barter Listings" else "My Rental Listings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // LazyColumn handles efficient vertical scrolling of the list items
            val data = if (selectedTab == ListingType.BARTER) barterListings else rentalListings
            items(data) { item ->
                ListingItemCard(item = item, onClick = { /* Handle item click */ })
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


@Composable
fun ProfileHeaderSection(
    barterCount: Int,
    rentalCount: Int,
    completedCount: Int
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center),
                    tint = Color(0xFF0288D1)
                )
            }

            // Pencil icon over avatar (bottom-right)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(TealBlue)
                    .align(Alignment.BottomEnd)
                    .clickable {
                        // TODO: replace SettingsActivity with your actual settings activity class name if different
                        // context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Edit Profile / Settings",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Lucas Scott", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
        Text("@lucasscott3", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(label = "Barter items", value = barterCount.toString(), modifier = Modifier.weight(1f))
            ProfileStat(label = "Rental items", value = rentalCount.toString(), modifier = Modifier.weight(1f))
            ProfileStat(label = "Completed", value = completedCount.toString(), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
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
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (item.type == ListingType.BARTER) Color(0xFFE0F2F1) else Color(0xFFEDE7F6)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.type == ListingType.BARTER) "Barter" else "Rental",
                            fontSize = 11.sp,
                            color = if (item.type == ListingType.BARTER) TealBlue else Color(0xFF5E35B1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.description, fontSize = 12.sp, color = Color.Gray)
                }
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
        ListingItem(1, "Amazing T-shirt", "Trade via credits", "€ 12.00", R.drawable.tshirt, ListingType.BARTER),
        ListingItem(2, "Fabulous Pants", "Trade via credits", "€ 15.00", R.drawable.pant, ListingType.BARTER),
        ListingItem(3, "Spectacular Dress", "Trade via credits", "€ 20.00", R.drawable.dress, ListingType.BARTER),
        ListingItem(4, "Cool Sneakers", "Rent per day", "€ 8.00 / day", R.drawable.sneakers, ListingType.RENTAL),
        ListingItem(5, "Stylish Jacket", "Rent per weekend", "€ 25.00 / weekend", R.drawable.jacket, ListingType.RENTAL),
        ListingItem(6, "Summer Hat", "Rent per day", "€ 5.00 / day", R.drawable.hat, ListingType.RENTAL),
    )
}
