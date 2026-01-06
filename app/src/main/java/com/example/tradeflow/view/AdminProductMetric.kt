package com.example.tradeflow.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.R
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel

class AdminProductMetric : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminProductMetricScreen(
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
fun AdminProductMetricScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val productViewModel = remember { ProductViewModel(ProductRepoImpl()) }
    val allProducts by productViewModel.allProducts.collectAsState()

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    val listed = allProducts?.count { it.isListed } ?: 0
    val unlisted = allProducts?.count { !it.isListed } ?: 0

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
                            text = "Product Metrics",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Listing Status Pie Chart",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (listed + unlisted > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductPieChartSegments(
                                segments = listOf(listed, unlisted),
                                colors = listOf(DarkGreen, Color.Red),
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(top = 12.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LegendItem(color = DarkGreen, label = "Listed", count = listed)
                                LegendItem(color = Color.Red, label = "Unlisted", count = unlisted)
                            }
                        }
                    } else {
                        Text(text = "No data", fontSize = 14.sp, color = Color.Gray)
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
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Listing Status Bar Chart",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    ProductMetricsBarChart(
                        data = listOf(
                            ProductMetricsBar("Listed", listed, DarkGreen),
                            ProductMetricsBar("Unlisted", unlisted, Color.Red)
                        ),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(color = DarkGreen, label = "Listed", count = listed)
                        LegendItem(color = Color.Red, label = "Unlisted", count = unlisted)
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
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Listing Status Scatter Plot",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    ProductMetricsScatterPlot(
                        points = (allProducts ?: emptyList()).mapIndexed { index, p ->
                            val x = if (p.isListed) 1f else 0f
                            ProductMetricsPoint(x = x, y = index.toFloat(), label = (index + 1).toString())
                        },
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(color = DarkGreen, label = "Listed", count = listed)
                        LegendItem(color = Color.Red, label = "Unlisted", count = unlisted)
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
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Listing Status Line Graph",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    ProductMetricsLineGraph(
                        data = listOf(
                            ProductMetricsBar("Listed", listed, DarkGreen),
                            ProductMetricsBar("Unlisted", unlisted, Color.Red)
                        ),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(color = DarkGreen, label = "Listed", count = listed)
                        LegendItem(color = Color.Red, label = "Unlisted", count = unlisted)
                    }
                }
            }
        }
    }
}

data class ProductMetricsBar(val label: String, val value: Int, val color: Color)

@Composable
fun ProductMetricsBarChart(data: List<ProductMetricsBar>, modifier: Modifier = Modifier) {
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

data class ProductMetricsPoint(val x: Float, val y: Float, val label: String)

@Composable
fun ProductMetricsScatterPlot(points: List<ProductMetricsPoint>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val padding = 24f
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val xStep = width
        val yMax = (points.maxOfOrNull { it.y } ?: 1f).coerceAtLeast(1f)
        val yScale = height / yMax
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
            val px = padding + p.x * (xStep / 1f)
            val py = size.height - padding - p.y * yScale
            val color = if (p.x >= 1f) DarkGreen else Color.Red
            drawCircle(color = color, radius = 6f, center = Offset(px, py))
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    setColor(android.graphics.Color.BLACK)
                    textSize = 24f
                }
                canvas.nativeCanvas.drawText(p.label, px + 8f, py - 8f, paint)
            }
        }
    }
}

@Composable
fun ProductMetricsLineGraph(data: List<ProductMetricsBar>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val padding = 40f
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val maxVal = (data.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)
        val yScale = if (maxVal == 0) 0f else height / maxVal
        val steps = (data.size - 1).coerceAtLeast(1)
        val xStep = if (data.isEmpty()) 0f else width / steps

        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 3.5f
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(padding, padding),
            end = Offset(padding, size.height - padding),
            strokeWidth = 3.5f
        )

        var prev: Offset? = null
        data.forEachIndexed { i, item ->
            val x = if (data.size == 1) padding + width / 2f else padding + i * xStep
            val y = size.height - padding - item.value * yScale
            val point = Offset(x, y)
            prev?.let { p ->
                drawLine(color = Color(0xFF1976D2), start = p, end = point, strokeWidth = 8f)
            }
            prev = point
            drawCircle(color = item.color, radius = 10f, center = point)

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    setColor(android.graphics.Color.BLACK)
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(item.value.toString(), x, y - 16f, paint)
            }
        }
    }
}
