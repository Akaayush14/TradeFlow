package com.example.tradeflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradeflow.model.UserModel
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.UserViewModel
import com.example.tradeflow.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

import com.example.tradeflow.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInboxScreen(
    onBackClick: () -> Unit = {},
    onChatClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val userViewModel: UserViewModel = viewModel(factory = ViewModelFactory())
    val chatViewModel: ChatViewModel = viewModel(factory = ViewModelFactory())
    val allUsers by userViewModel.allUsers.collectAsState()
    val chatSummaries by chatViewModel.chatSummaries.collectAsState()
    val currentUser = userViewModel.getCurrentUser()

    val inboxRows = remember(searchQuery, allUsers, chatSummaries) {
        val rows = mutableListOf<Pair<UserModel, com.example.tradeflow.model.UserModel.ChatModel>>()
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
        chatViewModel.loadChatSummaries()
    }

    Scaffold(
        topBar = {
            InboxTopAppBar(onBackClick = onBackClick)
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(White)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Greenish
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
                        onClick = { onChatClick(row.first.userId) }
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
                color = White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Greenish,
            titleContentColor = White
        )
    )
}

@Composable
fun InboxItem(user: UserModel, lastMessage: String, lastTime: Long, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(50.dp)
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name.ifEmpty { user.email },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = lastMessage.ifEmpty { "No messages yet" },
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
            )
        }

        // Status indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (!user.isBlocked) Color.Green else Color.Red)
        )
    }
}

private fun summaryText(model: com.example.tradeflow.model.UserModel.ChatModel): String {
    return model.lastMessage
}
