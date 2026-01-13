

package com.example.tradeflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradeflow.model.MessageModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ChatViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



import com.example.tradeflow.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String,
    onBackClick: () -> Unit = {}
) {
    val chatViewModel: ChatViewModel = viewModel(factory = ViewModelFactory())
    val userViewModel: UserViewModel = viewModel(factory = ViewModelFactory())

    val messages by chatViewModel.messages.collectAsState()
    val receiverUser by userViewModel.users.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        chatViewModel.getMessages(receiverId)
    }

    LaunchedEffect(receiverId) {
        userViewModel.getUserById(receiverId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                lazyListState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.removeListeners()
            chatViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                user = receiverUser,
                onBackClick = onBackClick
            )
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(messages) { message ->
                    MessageItem(
                        message = message,
                        isOwnMessage = message.senderId == userViewModel.getCurrentUser()?.uid
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50.dp),
                    singleLine = false,
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            chatViewModel.sendTextMessage(receiverId, messageText) { success, _ ->
                                if (success) messageText = ""
                            }
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (messageText.isNotBlank()) Greenish else Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    user: UserModel?,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user?.name ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = if (user?.isBlocked == true) "Blocked" else "Online",
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Greenish,
            titleContentColor = White
        )
    )
}

@Composable
fun MessageItem(
    message: MessageModel,
    isOwnMessage: Boolean
) {
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start
    val backgroundColor = if (isOwnMessage) Greenish else Color(0xFFE0E0E0)
    val textColor = if (isOwnMessage) White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp
            )
        }

        Text(
            text = formatMessageTime(message.timestamp),
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
