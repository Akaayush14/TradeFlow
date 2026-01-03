package com.example.tradeflow.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R


@Composable
fun ChatBotScreen() {

    var messageText by remember { mutableStateOf("") }

    // Empty list initially
    val messages = remember { mutableStateListOf<ChatMessage>() }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {

        ChatBotTopBar()

        Box(modifier = Modifier.weight(1f)) {
            // White background, no image

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                // Iterate in reverse order (Newest first) so they appear at the bottom
                // and stack upwards.
                for (i in messages.indices.reversed()) {
                    val message = messages[i]
                    val date = formatDate(message.time)

                    // Check previous (older) message for date change
                    val prevMessage = if (i > 0) messages[i - 1] else null
                    val prevDate = prevMessage?.let { formatDate(it.time) }

                    item {
                        MessageBubble(message)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // If date changed relative to the older message, or if this is the oldest message,
                    // show the date header above this message (which means emitted AFTER in reverseLayout)
                    if (prevDate != date) {
                        item { DateHeader(date) }
                    }
                }
            }
        }

        ChatBotInputBar(
            text = messageText,
            onTextChange = { messageText = it },
            onSend = {
                if (messageText.isNotBlank()) {
                    messages.add(
                        ChatMessage(
                            messageText,
                            true,
                            System.currentTimeMillis()
                        )
                    )
                    messageText = ""
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotTopBar(
    onBack: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .height(56.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF009688) // Teal color from image
        ),

        // Back Icon
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },

        // Title with Logo
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.house_rent_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Trade Flow",
                    color = Color.White,
                    fontSize = 18.sp,
                    maxLines = 1
                )
            }
        }
    )
}


@Composable
fun ChatBotInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Plus Icon
        IconButton(onClick = { }) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = Color(0xFF2979FF), // Blue color
                modifier = Modifier.size(28.dp)
            )
        }

        // Input Field
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Type a message...", color = Color.Gray) },
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5), // Light gray background
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF2979FF), CircleShape) // Blue color
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatBotPreview() {
    MaterialTheme {
        ChatBotScreen()
    }
}