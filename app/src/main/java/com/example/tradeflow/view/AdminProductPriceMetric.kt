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
import com.example.tradeflow.ui.theme.Greenish
import com.example.tradeflow.ui.theme.White
import com.example.tradeflow.viewmodel.ProductViewModel

class AdminProductPriceMetric : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminProductPriceMetricScreen(
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
fun AdminProductPriceMetricScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val vm = remember { ProductViewModel(ProductRepoImpl()) }
    val allProducts by vm.allProducts.collectAsState()

    LaunchedEffect(Unit) { vm.getAllProduct() }

    val lt100 = allProducts?.count { it.price < 100.0 } ?: 0
    val r100_499 = allProducts?.count { it.price >= 100.0 && it.price <= 499.0 } ?: 0
    val r500_999 = allProducts?.count { it.price >= 500.0 && it.price <= 999.0 } ?: 0
    val r1000_1499 = allProducts?.count { it.price >= 1000.0 && it.price <= 1499.0 } ?: 0
    val r1500_2000 = allProducts?.count { it.price >= 1500.0 && it.price <= 2000.0 } ?: 0
    val gt2000 = allProducts?.count { it.price > 2000.0 } ?: 0

    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFFC107),
        Color(0xFF03A9F4),
        Color(0xFF9C27B0),
        Color(0xFFFF5722),
        Color(0xFF607D8B)
    )

    BackHandler {
        val intent = Intent(context, AdminDashExp::class.java)
        context.startActivity(intent)
        if (context is ComponentActivity) context.finish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Product Price Metrics", color = White, style = MaterialTheme.typography.titleLarge)
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
                modifier = Modifier.fillMaxWidth().height(240.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Price Range Pie Chart", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    val total = lt100 + r100_499 + r500_999 + r1000_1499 + r1500_2000 + gt2000
                    if (total > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            ProductPieChartSegments(
                                segments = listOf(lt100, r100_499, r500_999, r1000_1499, r1500_2000, gt2000),
                                colors = colors,
                                modifier = Modifier.size(100.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LegendItem(color = colors[0], label = "<100", count = lt100)
                                LegendItem(color = colors[1], label = "100-499", count = r100_499)
                                LegendItem(color = colors[2], label = "500-999", count = r500_999)
                                LegendItem(color = colors[3], label = "1000-1499", count = r1000_1499)
                                LegendItem(color = colors[4], label = "1500-2000", count = r1500_2000)
                                LegendItem(color = colors[5], label = ">2000", count = gt2000)
                            }
                        }
                    } else {
                        Text(text = "No data", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Price Range Bar Chart", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    PriceMetricsBarChart(
                        data = listOf(
                            PriceMetricsBar("<100", lt100, colors[0]),
                            PriceMetricsBar("100-499", r100_499, colors[1]),
                            PriceMetricsBar("500-999", r500_999, colors[2]),
                            PriceMetricsBar("1000-1499", r1000_1499, colors[3]),
                            PriceMetricsBar("1500-2000", r1500_2000, colors[4]),
                            PriceMetricsBar(">2000", gt2000, colors[5])
                        ),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(color = colors[0], label = "<100", count = lt100)
                        LegendItem(color = colors[1], label = "100-499", count = r100_499)
                        LegendItem(color = colors[2], label = "500-999", count = r500_999)
                        LegendItem(color = colors[3], label = "1000-1499", count = r1000_1499)
                        LegendItem(color = colors[4], label = "1500-2000", count = r1500_2000)
                        LegendItem(color = colors[5], label = ">2000", count = gt2000)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Price Range Scatter Plot", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    PriceMetricsScatterPlot(
                        points = listOf(
                            PriceMetricsPoint(0f, lt100.toFloat(), label = "<100"),
                            PriceMetricsPoint(1f, r100_499.toFloat(), label = "100-499"),
                            PriceMetricsPoint(2f, r500_999.toFloat(), label = "500-999"),
                            PriceMetricsPoint(3f, r1000_1499.toFloat(), label = "1000-1499"),
                            PriceMetricsPoint(4f, r1500_2000.toFloat(), label = "1500-2000"),
                            PriceMetricsPoint(5f, gt2000.toFloat(), label = ">2000")
                        ),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(color = colors[0], label = "<100", count = lt100)
                        LegendItem(color = colors[1], label = "100-499", count = r100_499)
                        LegendItem(color = colors[2], label = "500-999", count = r500_999)
                        LegendItem(color = colors[3], label = "1000-1499", count = r1000_1499)
                        LegendItem(color = colors[4], label = "1500-2000", count = r1500_2000)
                        LegendItem(color = colors[5], label = ">2000", count = gt2000)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Price Range Line Graph", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    PriceMetricsLineGraph(
                        data = listOf(
                            PriceMetricsBar("<100", lt100, colors[0]),
                            PriceMetricsBar("100-499", r100_499, colors[1]),
                            PriceMetricsBar("500-999", r500_999, colors[2]),
                            PriceMetricsBar("1000-1499", r1000_1499, colors[3]),
                            PriceMetricsBar("1500-2000", r1500_2000, colors[4]),
                            PriceMetricsBar(">2000", gt2000, colors[5])
                        ),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(color = colors[0], label = "<100", count = lt100)
                        LegendItem(color = colors[1], label = "100-499", count = r100_499)
                        LegendItem(color = colors[2], label = "500-999", count = r500_999)
                        LegendItem(color = colors[3], label = "1000-1499", count = r1000_1499)
                        LegendItem(color = colors[4], label = "1500-2000", count = r1500_2000)
                        LegendItem(color = colors[5], label = ">2000", count = gt2000)
                    }
                }
            }
        }
    }
}

data class PriceMetricsBar(val label: String, val value: Int, val color: Color)

@Composable
fun PriceMetricsBarChart(data: List<PriceMetricsBar>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val maxVal = (data.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)
        val barCount = data.size
        val spacing = 12f
        val barWidth = (size.width - spacing * (barCount + 1)) / barCount
        val heightScale = if (maxVal == 0) 0f else (size.height - 24f) / maxVal
        var x = spacing
        data.forEach { bar ->
            val h = bar.value * heightScale
            drawRect(color = bar.color, topLeft = Offset(x, size.height - h), size = Size(barWidth, h))
            x += barWidth + spacing
        }
        drawLine(color = Color.Gray.copy(alpha = 0.4f), start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 2f)
        drawLine(color = Color.Gray.copy(alpha = 0.4f), start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 2f)
    }
}

data class PriceMetricsPoint(val x: Float, val y: Float, val label: String)

@Composable
fun PriceMetricsScatterPlot(points: List<PriceMetricsPoint>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val padding = 24f
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val xStep = if (points.isEmpty()) 0f else width / (points.size - 1).coerceAtLeast(1)
        val yMax = (points.maxOfOrNull { it.y } ?: 1f).coerceAtLeast(1f)
        val yScale = height / yMax
        drawLine(color = Color.Gray.copy(alpha = 0.4f), start = Offset(padding, size.height - padding), end = Offset(size.width - padding, size.height - padding), strokeWidth = 2f)
        drawLine(color = Color.Gray.copy(alpha = 0.4f), start = Offset(padding, padding), end = Offset(padding, size.height - padding), strokeWidth = 2f)
        points.forEachIndexed { i, p ->
            val px = padding + i * xStep
            val py = size.height - padding - p.y * yScale
            val color = when (p.label) {
                "<100" -> Color(0xFF4CAF50)
                "100-499" -> Color(0xFFFFC107)
                "500-999" -> Color(0xFF03A9F4)
                "1000-1499" -> Color(0xFF9C27B0)
                "1500-2000" -> Color(0xFFFF5722)
                else -> Color(0xFF607D8B)
            }
            drawCircle(color = color, radius = 8f, center = Offset(px, py))
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    setColor(android.graphics.Color.BLACK)
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(p.label, px, py - 12f, paint)
            }
        }
    }
}

@Composable
fun PriceMetricsLineGraph(data: List<PriceMetricsBar>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val padding = 40f
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val maxVal = (data.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)
        val yScale = if (maxVal == 0) 0f else height / maxVal
        val steps = (data.size - 1).coerceAtLeast(1)
        val xStep = width / steps
        drawLine(color = Color.Gray.copy(alpha = 0.5f), start = Offset(padding, size.height - padding), end = Offset(size.width - padding, size.height - padding), strokeWidth = 3.5f)
        drawLine(color = Color.Gray.copy(alpha = 0.5f), start = Offset(padding, padding), end = Offset(padding, size.height - padding), strokeWidth = 3.5f)
        var prev: Offset? = null
        data.forEachIndexed { i, item ->
            val x = padding + i * xStep
            val y = size.height - padding - item.value * yScale
            val point = Offset(x, y)
            prev?.let { p -> drawLine(color = Color(0xFF1976D2), start = p, end = point, strokeWidth = 8f) }
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

