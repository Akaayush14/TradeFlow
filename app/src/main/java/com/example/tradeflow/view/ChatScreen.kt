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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tradeflow.R
import com.example.tradeflow.model.MessageModel
import com.example.tradeflow.model.UserModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ChatSystemViewModel
import com.example.tradeflow.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.tradeflow.viewmodel.ViewModelFactory
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String,
    onBackClick: () -> Unit = {}
) {
    val chatViewModel: ChatSystemViewModel = viewModel(factory = ViewModelFactory())
    val userViewModel: UserViewModel = viewModel(factory = ViewModelFactory())

    val messages by chatViewModel.messages.collectAsState()
    val receiverUser by userViewModel.users.collectAsState()
    val context = LocalContext.current

    var messageText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<MessageModel?>(null) }

    LaunchedEffect(Unit) {
        chatViewModel.getMessages(receiverId)
    }

    LaunchedEffect(receiverId) {
        userViewModel.getUserById(receiverId) { _, _, _ -> }
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

    if (showDeleteDialog && messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Message") },
            text = { Text("Are you sure you want to delete this message?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatViewModel.deleteMessage(messageToDelete!!) { success, errorMsg ->
                            if (success) {
                                Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            showDeleteDialog = false
                            messageToDelete = null
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                        isOwnMessage = message.senderId == userViewModel.getCurrentUser()?.uid,
                        onLongClick = {
                            messageToDelete = message
                            showDeleteDialog = true
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            if (messageText.isEmpty()) {
                                Text("Type a message...", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        val text = messageText.trim()
                        if (text.isNotEmpty()) {
                            chatViewModel.sendTextMessage(receiverId, text) { success, errorMsg ->
                                if (success) {
                                    messageText = ""
                                } else {
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
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
                    if (user != null && user.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.placeholderimage),
                            error = painterResource(R.drawable.placeholderimage)
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.align(Alignment.Center),
                            tint = Color(0xFF0288D1)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user?.name ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = if (user?.isBlocked == true) "Blocked" else if (user?.isOnline == true) "Online" else "Offline",
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
    isOwnMessage: Boolean,
    onLongClick: () -> Unit
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
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongClick() })
                }
                .padding(8.dp)
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