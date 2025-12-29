package com.example.tradeflow.view
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import java.text.SimpleDateFormat
import java.util.*

class ChatSystemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChatScreen()
            }
        }
    }
}
data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val time: Long
)
@Composable
fun ChatScreen() {

    var messageText by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf(
            ChatMessage("Hey its just a theory!", false, System.currentTimeMillis() - 86400000),
            ChatMessage("Thor vs Thanos fight 🔥", false, System.currentTimeMillis()),
            ChatMessage("Disney will end it peacefully 😄", true, System.currentTimeMillis())
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        ChatTopBar()

        Box(modifier = Modifier.weight(1f)) {

            // 🔹 Background
            Image(
                painter = painterResource(R.drawable.house_rent_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                reverseLayout = true
            ) {
                var lastDate = ""

                messages.forEach { message ->
                    val date = formatDate(message.time)

                    if (date != lastDate) {
                        item { DateHeader(date) }
                        lastDate = date
                    }

                    item {
                        MessageBubble(message)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        ChatInputBar(
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
fun ChatTopBar(
    onBack: () -> Unit = {},
    onCall: () -> Unit = {},
    onVideoCall: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.height(56.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF075E54)
        ),

        // 🔹 BACK ICON
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },

        // 🔹 TITLE (IMAGE + NAME + STATUS)
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.house_rent_logo),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Avengers Endgame",
                        color = Color.White,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "online",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        },

        // 🔹 ACTION ICONS
        actions = {
            IconButton(onClick = onCall) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.White
                )
            }
            IconButton(onClick = onVideoCall) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Video Call",
                    tint = Color.White
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        }
    )
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier
                .background(Color(0x66000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

/* -------------------- MESSAGE BUBBLE -------------------- */

@Composable
fun MessageBubble(message: ChatMessage) {

    val gradient = Brush.verticalGradient(
        colors = if (message.isMe)
            listOf(
                Color(0xFF005F56),
                Color(0xFF007D70),
                Color(0xFF4DB6AC)
            )
        else listOf(Color.White, Color.White)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(16.dp))
                .padding(10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                message.text,
                color = if (message.isMe) Color.White else Color.Black,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatTime(message.time),
                fontSize = 10.sp,
                color = if (message.isMe) Color.White.copy(0.8f) else Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/* -------------------- INPUT BAR -------------------- */

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = { /* open attachment bottom sheet */ }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }

        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Message") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF25D366), CircleShape)
        ) {
            Icon(Icons.Default.Send, null, tint = Color.White)
        }
    }
}

/* -------------------- DATE & TIME -------------------- */

fun formatTime(time: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(time))

fun formatDate(time: Long): String {
    val today = SimpleDateFormat("yyyyMMdd").format(Date())
    val msgDate = SimpleDateFormat("yyyyMMdd").format(Date(time))

    return when {
        today == msgDate -> "Today"
        today.toInt() - msgDate.toInt() == 1 -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy").format(Date(time))
    }
}

@Preview(showBackground = true)
@Composable
fun ChatActivity() {
    MaterialTheme {
        ChatScreen()
    }
}