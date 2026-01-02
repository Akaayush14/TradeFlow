package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.repository.UserRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.UserViewModel

class AdminUserMetric : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminUserMetricScreen(
                onBackClick = {
                    val intent = Intent(this, AdminDashExp::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminUserMetricScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val allUsers by userViewModel.allUsers.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }

    val normalUsers = allUsers?.count { it.userId.isNotEmpty() && it.name.isNotEmpty() && !it.isBlocked && !it.isRestricted } ?: 0
    val blockedUsers = allUsers?.count { it.userId.isNotEmpty() && it.name.isNotEmpty() && it.isBlocked } ?: 0
    val restrictedUsers = allUsers?.count { it.userId.isNotEmpty() && it.name.isNotEmpty() && it.isRestricted } ?: 0
    val total = normalUsers + blockedUsers + restrictedUsers

    BackHandler {
        val intent = Intent(context, AdminDashExp::class.java)
        context.startActivity(intent)
        if (context is ComponentActivity) {
            context.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "User Metrics",
                            color = White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "User Status Pie Chart",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (total > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PieChart(
                                normalUsers = normalUsers,
                                blockedUsers = blockedUsers,
                                restrictedUsers = restrictedUsers,
                                total = total,
                                modifier = Modifier.size(120.dp)
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LegendItem(color = Greenish, label = "Normal", count = normalUsers)
                                LegendItem(color = Color.Red, label = "Blocked", count = blockedUsers)
                                LegendItem(color = Color(0xFFFF9800), label = "Restricted", count = restrictedUsers)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No data", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "User Status Bar Chart",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    UserMetricsBarChart(
                        data = listOf(
                            UserMetricsBar("Normal", normalUsers, Greenish),
                            UserMetricsBar("Restricted", restrictedUsers, Color(0xFFFF9800)),
                            UserMetricsBar("Blocked", blockedUsers, Color.Red)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "User Status Scatter Plot",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    UserMetricsScatterPlot(
                        points = (allUsers ?: emptyList()).mapIndexed { index, user ->
                            val x = when {
                                user.isBlocked -> 2f
                                user.isRestricted -> 1f
                                else -> 0f
                            }
                            UserMetricsPoint(x = x, y = index.toFloat())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LegendItem(color = Greenish, label = "Normal", count = normalUsers)
                        LegendItem(color = Color(0xFFFF9800), label = "Restricted", count = restrictedUsers)
                        LegendItem(color = Color.Red, label = "Blocked", count = blockedUsers)
                    }
                }
            }
        }
    }
}

data class UserMetricsBar(val label: String, val value: Int, val color: Color)

@Composable
fun UserMetricsBarChart(data: List<UserMetricsBar>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val maxVal = (data.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)
        val barCount = data.size
        val spacing = 16f
        val barWidth = (size.width - spacing * (barCount + 1)) / barCount
        val heightScale = if (maxVal == 0) 0f else (size.height - 24f) / maxVal
        var x = spacing
        data.forEach { bar ->
            val h = bar.value * heightScale
            drawRect(
                color = bar.color,
                topLeft = Offset(x, size.height - h),
                size = Size(barWidth, h)
            )
            x += barWidth + spacing
        }
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2f
        )
    }
}

data class UserMetricsPoint(val x: Float, val y: Float)

@Composable
fun UserMetricsScatterPlot(points: List<UserMetricsPoint>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val padding = 24f
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val xStep = if (points.isEmpty()) 0f else width / 2f
        val yMax = (points.maxOfOrNull { it.y } ?: 1f).coerceAtLeast(1f)
        val yScale = height / yMax
        drawRect(color = Color.Transparent, topLeft = Offset.Zero, size = size)
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(padding, padding),
            end = Offset(padding, size.height - padding),
            strokeWidth = 2f
        )
        points.forEach { p ->
            val px = padding + p.x * xStep
            val py = size.height - padding - p.y * yScale
            val color = when (p.x.toInt()) {
                2 -> Color.Red
                1 -> Color(0xFFFF9800)
                else -> Greenish
            }
            drawCircle(color = color, radius = 6f, center = Offset(px, py))
        }
    }
}
