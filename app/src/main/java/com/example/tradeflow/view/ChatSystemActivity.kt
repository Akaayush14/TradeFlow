package com.example.tradeflow

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

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
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isMe: Boolean,
    val time: Long = System.currentTimeMillis(),
    var replyTo: ChatMessage? = null
)

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var replyMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var showOptionsDialog by remember { mutableStateOf<ChatMessage?>(null) }

    fun sendMessage(text: String) {
        if (text.isNotBlank()) {
            messages.add(ChatMessage(text = text, isMe = true, replyTo = replyMessage))
            replyMessage = null
            messages.add(ChatMessage(text = "Auto reply ${Random.nextInt(100)}", isMe = false))
            messageText = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        ChatTopBar(
            onCall = { makePhoneCall(context, "9800000000") },
            onVideoCall = { openVideoCall(context) }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFECE5DD))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        onLongPress = { showOptionsDialog = message },
                        onReply = { replyMessage = message }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        replyMessage?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Replying to: ${it.text}", modifier = Modifier.weight(1f))
                TextButton(onClick = { replyMessage = null }) {
                    Text("Cancel")
                }
            }
        }

        ChatInputBar(
            text = messageText,
            onTextChange = { messageText = it },
            onSend = { sendMessage(messageText) }
        )
    }

    showOptionsDialog?.let { msg ->
        Dialog(onDismissRequest = { showOptionsDialog = null }) {
            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                TextButton(onClick = {
                    messages.remove(msg)
                    showOptionsDialog = null
                }) {
                    Text("Delete")
                }
                TextButton(onClick = {
                    replyMessage = msg
                    showOptionsDialog = null
                }) {
                    Text("Reply")
                }
                TextButton(onClick = { showOptionsDialog = null }) {
                    Text("Cancel")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    onCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF075E54)),
        title = {
            Column {
                Text("TradeFlow", color = Color.White, fontSize = 16.sp)
                Text("online", color = Color.White.copy(0.7f), fontSize = 12.sp)
            }
        },
        actions = {
            IconButton(onClick = onCall) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
            }
            IconButton(onClick = onVideoCall) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }
    )
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onLongPress: () -> Unit,
    onReply: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (message.isMe) Color(0xFF25D366) else Color.White,
                    RoundedCornerShape(16.dp)
                )
                .padding(10.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                )
                .widthIn(max = 280.dp)
        ) {
            Column {
                message.replyTo?.let {
                    Text(
                        "Reply: ${it.text}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.text,
                    color = if (message.isMe) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formatTime(message.time),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message") },
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF25D366), CircleShape)
        ) {
            Text("➤", color = Color.White, fontSize = 18.sp)
        }
    }
}

/* ---------------- API FUNCTIONS ---------------- */

fun makePhoneCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$number")
    }
    context.startActivity(intent)
}

fun openVideoCall(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://meet.google.com")
    }
    context.startActivity(intent)
}

fun formatTime(time: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(time))
}


@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    MaterialTheme {
        ChatScreen()
    }
}
