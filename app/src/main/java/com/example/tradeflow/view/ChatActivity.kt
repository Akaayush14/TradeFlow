package com.example.tradeflow.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    var messageText by remember { mutableStateOf("") }

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
                            painter = painterResource(id = if (receiverId == "chat_bot") R.drawable.traded else R.drawable.house_rent_logo),
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
                    containerColor = Color(0xFF006400)
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
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
                    color = Color.Red,
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
                                color = Color.Red
                            )
                        }
                        is ChatUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                reverseLayout = true
                            ) {
                                items(state.messages) { message ->
                                    ChatBubble(message, senderId == message.senderId)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }

                // Input Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = { Text("Type a message...") }
                    )
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(senderId, receiverId, messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF006400), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    val backgroundColor = if (isMe) Color(0xFF006400) else Color.White
    val contentColor = if (isMe) Color.White else Color.Black
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 0.dp)
    }
    val alignment = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = backgroundColor,
            shape = shape,
            shadowElevation = 2.dp
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
    }
}

fun formatChatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}