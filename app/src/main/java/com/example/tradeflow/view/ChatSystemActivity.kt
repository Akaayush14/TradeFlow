package com.example.tradeflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tradeflow.R
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
    var isSeen: Boolean = false,
    var replyTo: ChatMessage? = null
)

@Composable
fun ChatScreen() {
    var messageText by remember { mutableStateOf("") }
    var replyMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var showOptionsDialog by remember { mutableStateOf<Pair<ChatMessage, Boolean>?>(null) }
    var forwardMessage by remember { mutableStateOf<ChatMessage?>(null) }

    fun sendMessage(text: String) {
        if (text.isNotBlank()) {
            messages.add(ChatMessage(text = text, isMe = true, replyTo = replyMessage))
            replyMessage = null
            // Simulate auto-reply
            messages.add(ChatMessage(text = "Auto reply ${Random.nextInt(100)}", isMe = false))
        }
    }

    fun deleteMessage(message: ChatMessage, deleteForEveryone: Boolean) {
        messages.remove(message)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChatTopBar()

        Box(modifier = Modifier.weight(1f)) {
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
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        onLongPress = { showOptionsDialog = Pair(message, false) },
                        onReply = { replyMessage = message },
                        onForward = { forwardMessage = message }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        // Reply preview
        replyMessage?.let { reply ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Replying to: ${reply.text}",
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { replyMessage = null }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Cancel"
                    )
                }
            }
        }

        // Forward preview
        forwardMessage?.let { fwd ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0F7FA))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Forwarding: ${fwd.text}",
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    sendMessage(fwd.text)
                    forwardMessage = null
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_send_24),
                        contentDescription = "Send Forward"
                    )
                }
                IconButton(onClick = { forwardMessage = null }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Cancel"
                    )
                }
            }
        }

        ChatInputBar(
            text = messageText,
            onTextChange = { messageText = it },
            onSend = { sendMessage(messageText) }
        )
    }

    // Options Dialog
    showOptionsDialog?.let { (msg, _) ->
        Dialog(onDismissRequest = { showOptionsDialog = null }) {
            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                TextButton(onClick = { deleteMessage(msg, false); showOptionsDialog = null }) {
                    Text("Delete for me")
                }
                TextButton(onClick = { deleteMessage(msg, true); showOptionsDialog = null }) {
                    Text("Delete for everyone")
                }
                TextButton(onClick = { replyMessage = msg; showOptionsDialog = null }) {
                    Text("Reply")
                }
                TextButton(onClick = { forwardMessage = msg; showOptionsDialog = null }) {
                    Text("Forward")
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
    onBack: () -> Unit = {},
    onCall: () -> Unit = {},
    onVideoCall: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier.height(56.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF075E54)),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_back_ios_new_24),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.house_rent_logo),
                    contentDescription = "Profile",
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Avengers Endgame", color = Color.White, fontSize = 16.sp)
                    Text(text = "online", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        },
        actions = {
            Row {
                IconButton(onClick = onCall) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_call_24),
                        contentDescription = "Call",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onVideoCall) {
                    Icon(
                        painter = painterResource(R.drawable.videocall),
                        contentDescription = "Video Call",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_more_horiz_24),
                        contentDescription = "More",
                        tint = Color.White
                    )
                }
            }
        }
    )
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onLongPress: () -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (message.isMe) Color(0xFF25D366) else Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(10.dp)
                .widthIn(max = 280.dp)
                .combinedClickable(onClick = { }, onLongClick = onLongPress)
        ) {
            Column {
                message.replyTo?.let { reply ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Text("Replying to: ${reply.text}", fontSize = 12.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(text = message.text, color = if (message.isMe) Color.White else Color.Black, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatTime(message.time), fontSize = 10.sp, color = if (message.isMe) Color.White.copy(0.8f) else Color.Gray)
                }
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
        IconButton(onClick = { /* handle attachment */ }) {
            Icon(
                painter = painterResource(R.drawable.additem),
                contentDescription = "Add",
                tint = Color(0xFF075E54)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = "Message",
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )


        Spacer(modifier = Modifier.width(6.dp))

        if (text.isNotBlank()) {
            IconButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp).background(Color(0xFF25D366), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_send_24),
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        } else {
            IconButton(
                onClick = { /* record voice */ },
                modifier = Modifier.size(48.dp).background(Color(0xFF075E54), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_send_24),
                    contentDescription = "Mic",
                    tint = Color.White
                )
            }
        }
    }
}

fun formatTime(time: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(time))

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    MaterialTheme {
        ChatScreen()
    }
}
