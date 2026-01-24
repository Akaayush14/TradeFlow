package com.example.tradeflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White

// Data model for messages
data class MessagePreview(
    val id: Int,
    val senderName: String,
    val lastMessage: String,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInboxScreen(onBackClick: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    val messages = remember { getMockMessages() }

    Scaffold(
        topBar = {
            InboxTopAppBar(onBackClick = onBackClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
    // Use TradeFlowTopBar for consistent styling
    TradeFlowTopBar(
        title = {
            Text(
                "Inbox",
                color = White
            )
        },
        onBackClick = onBackClick
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