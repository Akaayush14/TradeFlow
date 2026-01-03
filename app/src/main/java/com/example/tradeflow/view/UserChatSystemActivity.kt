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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*



class UserChatSystemActivity : ComponentActivity() {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val chatId = listOf(currentUserId, receiverId).sorted().joinToString("_")

    var messageText by remember { mutableStateOf("") }
    var replyMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showOptionsDialog by remember { mutableStateOf<ChatMessage?>(null) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    // 🔄 Listen for real-time updates
    LaunchedEffect(chatId) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    messages.clear()
                    it.documents.forEach { doc ->
                        messages.add(
                            ChatMessage(
                                id = doc.id,
                                text = doc.getString("text") ?: "",
                                isMe = doc.getString("senderId") == currentUserId,
                                time = doc.getLong("timestamp") ?: 0
                            )
                        )
                    }
                }
            }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val msgMap = hashMapOf(
            "senderId" to currentUserId,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "replyTo" to replyMessage?.id // optional for reply feature
        )
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(msgMap)
        replyMessage = null
        messageText = ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with Call / Video
        ChatTopBar(
            onCall = { makePhoneCall(context, "9800000000") },
            onVideoCall = { openVideoCall(context) }
        )

        // Chat messages
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFECE5DD))
        ) {
            LazyColumn(
                state = listState,
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

        // Reply indicator
        replyMessage?.let { msg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Replying to: ${msg.text}", modifier = Modifier.weight(1f))
                TextButton(onClick = { replyMessage = null }) { Text("Cancel") }
            }
        }

        // Input bar
        ChatInputBar(
            text = messageText,
            onTextChange = { messageText = it },
            onSend = { sendMessage(messageText) },
            onAttachClick = {
                // Optional: attachment picker
            }
        )
    }

    // Options dialog (Delete / Reply)
    showOptionsDialog?.let { msg ->
        Dialog(onDismissRequest = { showOptionsDialog = null }) {
            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                TextButton(onClick = {
                    // Delete message in Firestore
                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .document(msg.id)
                        .delete()
                    showOptionsDialog = null
                }) { Text("Delete") }

                TextButton(onClick = {
                    replyMessage = msg
                    showOptionsDialog = null
                }) { Text("Reply") }

                TextButton(onClick = { showOptionsDialog = null }) { Text("Cancel") }
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
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
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

            // 🔽 MORE MENU
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("Clear chat") },
                        onClick = {
                            expanded = false
                            // TODO: clear messages
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Block user") },
                        onClick = {
                            expanded = false
                            // TODO: block user
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Report") },
                        onClick = {
                            expanded = false
                            // TODO: report user
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF075E54)
        )
    )
}


@Composable
fun MessageBubble(
    message: ChatMessage,
    onLongPress: () -> Unit,
    onReply: () -> Unit
) {
    val bubbleColor = if (message.isMe) Color(0xFF25D366) else Color.White
    val textColor = if (message.isMe) Color.White else Color.Black
    val alignment = if (message.isMe) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .combinedClickable(
                    onClick = { /* do nothing */ },
                    onLongClick = onLongPress
                )
                .widthIn(max = 250.dp)
        ) {
            message.replyTo?.let {
                Text(
                    text = "Reply: ${it.text}",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTime(message.time),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit // New callback for plus button
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Plus button for attachments
        IconButton(
            onClick = onAttachClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF075E54), CircleShape)
        ) {
            Text("+", color = Color.White, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Message input field
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Message", color = Color.Gray) },
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(24.dp)), // light gray background
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )


        Spacer(modifier = Modifier.width(8.dp))

        // Send button
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



fun makePhoneCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$number") }
    context.startActivity(intent)
}

fun openVideoCall(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://meet.google.com") }
    context.startActivity(intent)
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

