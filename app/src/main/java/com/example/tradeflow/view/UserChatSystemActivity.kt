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
import com.example.tradeflow.ui.theme.Greenish
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
    val replyTo: ChatMessage? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {

    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var replyMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showOptions by remember { mutableStateOf<ChatMessage?>(null) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    fun sendMessage() {
        if (messageText.isBlank()) return
        messages.add(
            0,
            ChatMessage(
                text = messageText,
                isMe = true,
                replyTo = replyMessage
            )
        )
        messageText = ""
        replyMessage = null
    }

    Column(modifier = Modifier.fillMaxSize()) {

        ChatTopBar(
            onCall = { makePhoneCall(context, "9800000000") },
            onVideoCall = { openVideoCall(context) }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                reverseLayout = true,
                state = listState,
                modifier = Modifier.padding(8.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        message = msg,
                        onLongPress = { showOptions = msg }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        replyMessage?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(6.dp)
            ) {
                Text("Replying: ${it.text}", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { replyMessage = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        ChatInputBar(
            text = messageText,
            onTextChange = { messageText = it },
            onSend = { sendMessage() }
        )
    }

    showOptions?.let { msg ->
        Dialog(onDismissRequest = { showOptions = null }) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                TextButton(onClick = {
                    messages.remove(msg)
                    showOptions = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }

                TextButton(onClick = {
                    replyMessage = msg
                    showOptions = null
                }) { Text("Reply", color = MaterialTheme.colorScheme.primary) }

                TextButton(onClick = { showOptions = null }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(onCall: () -> Unit, onVideoCall: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text("TradeFlow", color = MaterialTheme.colorScheme.onPrimary)
                Text("online", color = MaterialTheme.colorScheme.onPrimary.copy(0.7f), fontSize = 12.sp)
            }
        },
        actions = {
            IconButton(onClick = onCall) {
                Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onVideoCall) {
                Icon(Icons.Default.Videocam, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Clear chat") }, onClick = { expanded = false })
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun MessageBubble(message: ChatMessage, onLongPress: () -> Unit) {
    val bg = if (message.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val txt = if (message.isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val align = if (message.isMe) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = align
    ) {
        Column(
            modifier = Modifier
                .background(bg, RoundedCornerShape(16.dp))
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(10.dp)
                .widthIn(max = 250.dp)
        ) {
            message.replyTo?.let {
                Text("Reply: ${it.text}", fontSize = 12.sp, color = txt.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(message.text, color = txt)
            Text(
                formatTime(message.time),
                fontSize = 10.sp,
                color = txt.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
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
            placeholder = { Text("Message") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Text("➤", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

fun makePhoneCall(context: Context, number: String) {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
}

fun openVideoCall(context: Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.google.com")))
}

fun formatTime(time: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(time))

@Preview(showBackground = true)
@Composable
fun PreviewChat() {
    MaterialTheme {
        ChatScreen()
    }
}
