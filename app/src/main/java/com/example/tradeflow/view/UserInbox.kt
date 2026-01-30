package com.example.tradeflow.view

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.tradeflow.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.UserViewModel
import com.example.tradeflow.viewmodel.ChatSystemViewModel
import kotlinx.coroutines.launch

import com.example.tradeflow.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInboxScreen(
    onBackClick: () -> Unit = {},
    onChatClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val userViewModel: UserViewModel = viewModel(factory = ViewModelFactory())
    val chatSystemViewModel: ChatSystemViewModel = viewModel(factory = ViewModelFactory())
    val allUsers by userViewModel.allUsers.collectAsState()
    val chatSummaries by chatSystemViewModel.chatSummaries.collectAsState()
    val currentUser = userViewModel.getCurrentUser()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserModel?>(null) }

    val inboxRows = remember(searchQuery, allUsers, chatSummaries) {
        val rows = mutableListOf<Pair<UserModel, com.example.tradeflow.model.ChatModel>>()
        val usersMap = allUsers?.associateBy { it.userId } ?: emptyMap()
        chatSummaries.forEach { summary ->
            val partnerId = summary.participants.firstOrNull { it != currentUser?.uid } ?: ""
            val user = usersMap[partnerId]
            if (user != null) {
                rows.add(user to summary)
            }
        }
        val filtered = if (searchQuery.isEmpty()) {
            rows
        } else {
            rows.filter {
                it.first.name.contains(searchQuery, ignoreCase = true) ||
                        it.first.email.contains(searchQuery, ignoreCase = true) ||
                        summaryText(it.second).contains(searchQuery, ignoreCase = true)
            }
        }
        filtered.sortedByDescending { it.second.lastMessageTime }
    }

    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
        chatSystemViewModel.loadChatSummaries()
    }

    if (showDeleteDialog && userToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Conversation") },
            text = { Text("Are you sure you want to delete the conversation with ${userToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToDelete?.let { user ->
                            chatSystemViewModel.deleteChat(user.userId) { success, _ ->
                                if (success) {
                                    showDeleteDialog = false
                                    userToDelete = null
                                }
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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
            InboxTopAppBar(onBackClick = onBackClick)
        },
        containerColor = MaterialTheme.colorScheme.background,
        // ADD THE FLOATING ACTION BUTTON HERE
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Open Chat Bot
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("receiverId", "chat_bot")
                    intent.putExtra("receiverName", "TradeFlow Assistant")
                    context.startActivity(intent)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chatbot),
                    contentDescription = "Chat Bot Assistant",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(50.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(inboxRows) { row ->
                    InboxItem(
                        user = row.first,
                        lastMessage = summaryText(row.second),
                        lastTime = row.second.lastMessageTime,
                        onClick = { onChatClick(row.first.userId) },
                        onLongClick = {
                            userToDelete = row.first
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxTopAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Messages",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InboxItem(
    user: UserModel,
    lastMessage: String,
    lastTime: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            if (user.profileImageUrl.isNotEmpty()) {
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name.ifEmpty { user.email },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = lastMessage.ifEmpty { "No messages yet" },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        // Status indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (user.isOnline) Color.Green else Color.Red)
        )
    }
}

private fun summaryText(model: com.example.tradeflow.model.ChatModel): String {
    return model.lastMessage
}