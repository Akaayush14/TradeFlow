package com.example.tradeflow.view

import android.content.ContentValues
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.tradeflow.BuildConfig
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.model.RequestModel
import com.example.tradeflow.repository.ProductRepoImpl
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.ui.theme.Greenish
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream

class RentalPaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Stripe (Reuse key from UserPointsActivity for consistency)
        PaymentConfiguration.init(
            context = this,
            publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY
        )

        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
                RentalPaymentScreen()
            }
        }
    }
}

// Helper to calculate end date from period string
private fun calculateEndDate(period: String): Long {
    val now = System.currentTimeMillis()
    try {
        val parts = period.trim().split(" ")
        if (parts.isEmpty()) return now + 86400000L // Default 1 day
        
        val value = parts[0].toLongOrNull() ?: return now + 86400000L
        val unit = if (parts.size > 1) parts[1].lowercase() else ""
        
        val millis = when {
            unit.startsWith("day") -> value * 24 * 60 * 60 * 1000L
            unit.startsWith("week") -> value * 7 * 24 * 60 * 60 * 1000L
            unit.startsWith("month") -> value * 30 * 24 * 60 * 60 * 1000L
            unit.startsWith("hour") -> value * 60 * 60 * 1000L
            else -> 86400000L // Default 1 day
        }
        return now + millis
    } catch (e: Exception) {
        return now + 86400000L
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalPaymentScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val requestId = activity?.intent?.getStringExtra("requestId") ?: ""

    var requestModel by remember { mutableStateOf<RequestModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var isPaymentSuccessful by remember { mutableStateOf(false) }
    
    // Stripe Payment Sheet
    val paymentSheet = rememberPaymentSheet { paymentSheetResult ->
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(context, "Deposit Paid Successfully!", Toast.LENGTH_SHORT).show()
                // Update request status to CONFIRMED
                updateRequestStatus(requestId, "CONFIRMED") { success ->
                    if (success) {
                        // Update product status to "Rented" and set End Date
                        requestModel?.let { req ->
                            val endDate = if (req.rentalEndDate > 0) {
                                req.rentalEndDate
                            } else {
                                calculateEndDate(req.rentalPeriod)
                            }
                            
                            ProductRepoImpl().updateProductRentalInfo(req.productId, "Rented", endDate, requestId) { _, _ ->
                                isPaymentSuccessful = true
                            }
                        } ?: run {
                            isPaymentSuccessful = true
                        }
                    } else {
                        Toast.makeText(context, "Payment successful but failed to update status", Toast.LENGTH_LONG).show()
                    }
                }
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(context, "Payment Canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(context, "Payment Failed: ${paymentSheetResult.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(requestId) {
        if (requestId.isNotEmpty()) {
            val repo = UserNotificationRepoImpl() // Reusing existing repo which likely has getRequestById
            repo.getRequestById(requestId) { success, msg, request ->
                isLoading = false
                if (success && request != null) {
                    requestModel = request
                } else {
                    errorMessage = msg
                }
            }
        } else {
            isLoading = false
            errorMessage = "Invalid Request ID"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Payment", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (isPaymentSuccessful && requestModel != null) {
                PaymentSuccessContent(
                    request = requestModel!!, 
                    onDownloadBill = {
                        generateAndDownloadBill(context, requestModel!!)
                    },
                    onFinish = { activity?.finish() }
                )
            } else {
                requestModel?.let { request ->
                    PaymentContent(request = request, onPayClick = { clientSecret ->
                        paymentSheet.presentWithPaymentIntent(
                            clientSecret,
                            PaymentSheet.Configuration("TradeFlow Rental Deposit")
                        )
                    })
                }
            }
        }
    }
}

@Composable
fun PaymentSuccessContent(
    request: RequestModel,
    onDownloadBill: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Greenish,
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Payment Successful!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your security deposit has been received.",
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onDownloadBill,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Download Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentContent(request: RequestModel, onPayClick: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security Deposit Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)), // Light yellow
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFFFD54F))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFA000))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Security Deposit Required",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Text(
                    text = "Why deposit? This refundable amount protects the owner's item. It will be returned to you after the rental period ends and the item is returned in good condition.",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037)
                )

                Text(
                    text = "Deposit Amount:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Greenish
                )
                
                Text(
                    text = "Rs ${request.securityDeposit}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6F00) // Orange/Amber
                )
                
                Text(
                    text = "Refund Policy: Full deposit returned within 24 hours after item return. Partial deduction may apply if item is damaged.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Payment Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5C6BC0)), // Indigo/Blueish
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Payment Summary",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rental Fee (${request.rentalPeriod})", color = Color.White.copy(alpha = 0.9f))
                    Text("Rs ${request.rentalTotalPrice}", color = Color.White)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Security Deposit", color = Color.White.copy(alpha = 0.9f))
                    Text("Rs ${request.securityDeposit}", color = Color.White)
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total to Pay Now", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text("Rs ${request.securityDeposit}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
                
                Text(
                    text = "* Rental fee will be collected upon item handover.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                isProcessing = true
                fetchPaymentIntent(request.securityDeposit) { clientSecret ->
                    isProcessing = false
                    if (clientSecret != null) {
                        onPayClick(clientSecret)
                    } else {
                        Toast.makeText(context, "Failed to initialize payment", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Greenish),
            enabled = !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Pay Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helper to fetch PaymentIntent (Simulated for now based on UserPointsActivity logic)
private fun fetchPaymentIntent(amount: Double, onResult: (String?) -> Unit) {
    // Reuse the test logic from UserPointsActivity
    val TEST_STRIPE_SECRET_KEY = BuildConfig.STRIPE_SECRET_KEY
    val client = OkHttpClient()
    
    val formBody = FormBody.Builder()
        .add("amount", (amount * 100).toLong().toString()) // Amount in cents
        .add("currency", "npr")
        .add("automatic_payment_methods[enabled]", "true")
        .build()

    val request = Request.Builder()
        .url("https://api.stripe.com/v1/payment_intents")
        .addHeader("Authorization", "Bearer $TEST_STRIPE_SECRET_KEY")
        .post(formBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                try {
                    val json = JSONObject(responseBody)
                    val clientSecret = json.optString("client_secret")
                    onResult(clientSecret)
                } catch (e: Exception) {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
        }
    })
}

// Helper to update request status (Quick implementation, ideally use ViewModel)
private fun updateRequestStatus(requestId: String, status: String, onResult: (Boolean) -> Unit) {
    val repo = UserNotificationRepoImpl()
    repo.updateRequestStatus(requestId, status) { success, _ ->
        if (success) {
            onResult(true)
        } else {
            onResult(false)
        }
    }
}

private fun generateAndDownloadBill(context: android.content.Context, request: RequestModel) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    
    // Title
    paint.textSize = 24f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("TradeFlow Rental Receipt", 100f, 80f, paint)
    
    // Request Info
    paint.textSize = 14f
    paint.isFakeBoldText = false
    var yPos = 140f
    
    canvas.drawText("Receipt ID: ${System.currentTimeMillis()}", 80f, yPos, paint)
    yPos += 30f
    canvas.drawText("Date: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())}", 80f, yPos, paint)
    yPos += 50f
    
    // Product Details
    paint.isFakeBoldText = true
    canvas.drawText("Product Details", 80f, yPos, paint)
    paint.isFakeBoldText = false
    yPos += 30f
    canvas.drawText("Product: ${request.productName}", 80f, yPos, paint)
    yPos += 30f
    canvas.drawText("Rental Period: ${request.rentalPeriod}", 80f, yPos, paint)
    yPos += 50f
    
    // Payment Details
    paint.isFakeBoldText = true
    canvas.drawText("Payment Details", 80f, yPos, paint)
    paint.isFakeBoldText = false
    yPos += 30f
    canvas.drawText("Rental Fee: Rs ${request.rentalTotalPrice} (Pending)", 80f, yPos, paint)
    yPos += 30f
    canvas.drawText("Security Deposit: Rs ${request.securityDeposit} (Paid)", 80f, yPos, paint)
    yPos += 40f
    
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("Total Paid Now: Rs ${request.securityDeposit}", 80f, yPos, paint)
    
    yPos += 30f
    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Balance Due: Rs ${request.rentalTotalPrice}", 80f, yPos, paint)
    
    // Footer
    paint.textSize = 12f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Thank you for using TradeFlow!", 200f, 800f, paint)
    
    pdfDocument.finishPage(page)
    
    // Save PDF
    val fileName = "TradeFlow_Receipt_${request.productName}_${System.currentTimeMillis()}.pdf"
    
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    pdfDocument.writeTo(outputStream)
                    outputStream.close()
                    Toast.makeText(context, "Bill downloaded to Downloads", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            val file = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            val outputStream = java.io.FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            Toast.makeText(context, "Bill downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to download bill: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
