package com.example.tradeflow.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradeflow.R
import com.example.tradeflow.model.ChatMessage
import com.example.tradeflow.viewmodel.ChatUiState
import com.example.tradeflow.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receiverId = intent.getStringExtra("receiverId") ?: ""
        val receiverName = intent.getStringExtra("receiverName") ?: "User"
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setContent {
            MaterialTheme {
                val viewModel: ChatViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return ChatViewModel(applicationContext) as T
                        }
                    }
                )
                ChatScreen(senderId, receiverId, receiverName, onBack = { finish() }, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    senderId: String,
    receiverId: String,
    receiverName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var messageText by remember { mutableStateOf("") }

    // Selection Mode State
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(senderId, receiverId) {
        if (senderId.isNotEmpty() && receiverId.isNotEmpty()) {
            viewModel.loadMessages(senderId, receiverId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = if (receiverId == "chat_bot") R.drawable.chatbot else R.drawable.house_rent_logo),
                            contentDescription = "Receiver Image",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = receiverName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (senderId.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You must be logged in to chat.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when (val state = uiState) {
                        is ChatUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        is ChatUiState.Error -> {
                            Text(
                                text = state.message,
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is ChatUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                reverseLayout = true
                            ) {
                                if (isTyping) {
                                    item {
                                        TypingIndicator()
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                items(state.messages) { message ->
                                    val isSelected = selectedMessages.contains(message.id)
                                    ChatBubble(
                                        message = message,
                                        isMe = senderId == message.senderId,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedMessages.add(message.id)
                                            }
                                        },
                                        onClick = {
                                            if (isSelectionMode) {
                                                if (isSelected) {
                                                    selectedMessages.remove(message.id)
                                                    if (selectedMessages.isEmpty()) {
                                                        isSelectionMode = false
                                                    }
                                                } else {
                                                    selectedMessages.add(message.id)
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                // Input Area or Selection Toolbar
                if (isSelectionMode) {
                    SelectionToolbar(
                        selectedCount = selectedMessages.size,
                        onDelete = {
                            // Delete selected messages
                            val roomId = if (senderId < receiverId) "${senderId}_${receiverId}" else "${receiverId}_${senderId}"
                            selectedMessages.forEach { msgId ->
                                viewModel.deleteMessage(msgId, roomId)
                            }
                            selectedMessages.clear()
                            isSelectionMode = false
                        },
                        onCancel = {
                            selectedMessages.clear()
                            isSelectionMode = false
                        },
                        onSelectAll = {
                            val currentState = uiState
                            if (currentState is ChatUiState.Success) {
                                if (selectedMessages.size == currentState.messages.size) {
                                    selectedMessages.clear() // Deselect all if already all selected
                                } else {
                                    selectedMessages.clear()
                                    selectedMessages.addAll(currentState.messages.map { it.id })
                                }
                            }
                        }
                    )
                } else {
                    MessageInput(
                        messageText = messageText,
                        onMessageChange = { messageText = it },
                        onSend = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(senderId, receiverId, messageText)
                                messageText = ""
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionToolbar(
    selectedCount: Int,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cross button (Left)
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Cancel Selection", tint = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Selected Count (Left, next to cross)
        Text(
            text = "$selectedCount selected",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Spacer to push Select All to center
        Spacer(modifier = Modifier.weight(1f))

        // Select All (Center-ish)
        TextButton(onClick = onSelectAll) {
            Text("Select All", color = MaterialTheme.colorScheme.primary)
        }

        // Spacer to push Delete to right
        Spacer(modifier = Modifier.weight(1f))

        // Delete button (Right)
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun MessageInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = { Text("Type a message...") }
        )

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isMe: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer // Highlight color when selected
    } else if (isMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 0.dp)
    }
    val alignment = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            if (!isMe) {
                Image(
                    painter = painterResource(id = R.drawable.chatbot),
                    contentDescription = "Bot Avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box {
                Surface(
                    color = backgroundColor,
                    shape = shape,
                    shadowElevation = 2.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                        .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.message,
                            color = contentColor,
                            fontSize = 16.sp
                        )
                        Text(
                            text = formatChatTime(message.timestamp),
                            color = contentColor.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                // Checkmark Overlay for selected items
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }
        }
    }
}

fun formatChatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun TypingIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 0.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Typing...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}