package com.example.tradeflow.view

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.tradeflow.repository.UserNotificationRepoImpl
import com.example.tradeflow.ui.components.ThemeWrapper
import com.example.tradeflow.ui.theme.Greenish
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream

class FinalPaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Stripe
        PaymentConfiguration.init(
            context = this,
            publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY
        )

        enableEdgeToEdge()
        setContent {
            ThemeWrapper {
                FinalPaymentScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalPaymentScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val requestId = activity?.intent?.getStringExtra("requestId") ?: ""
    val amount = activity?.intent?.getDoubleExtra("amount", 0.0) ?: 0.0

    var requestModel by remember { mutableStateOf<RequestModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var isPaymentSuccessful by remember { mutableStateOf(false) }
    
    val paymentSheet = rememberPaymentSheet { paymentSheetResult ->
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(context, "Rent Paid Successfully!", Toast.LENGTH_SHORT).show()
                // Update request status to RENT_PAID
                val updates = mapOf(
                    "status" to "RENT_PAID",
                    "isRentPaid" to true
                )
                UserNotificationRepoImpl().updateRequestDetails(requestId, updates) { success, _ ->
                    if (success) {
                        isPaymentSuccessful = true
                    } else {
                        Toast.makeText(context, "Payment successful but failed to update status", Toast.LENGTH_LONG).show()
                    }
                }
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(context, "Payment Canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(context, "Payment Failed: ${paymentSheetResult.error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(requestId) {
        if (requestId.isNotEmpty()) {
            UserNotificationRepoImpl().getRequestById(requestId) { success, msg, request ->
                if (success && request != null) {
                    requestModel = request
                } else {
                    errorMessage = "Failed to load request details"
                }
                isLoading = false
            }
        } else {
            isLoading = false
            errorMessage = "Invalid Request ID"
        }
    }

    if (isPaymentSuccessful && requestModel != null) {
        PaymentSuccessContent(
            request = requestModel!!,
            amount = amount,
            onDownloadBill = {
                requestModel?.let {
                    generateRentBill(context, it, amount)
                }
            },
            onFinish = {
                activity?.finish()
            }
        )
    } else if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Greenish)
        }
    } else if (errorMessage.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(errorMessage, color = Color.Red)
        }
    } else {
        requestModel?.let { request ->
            FinalPaymentContent(
                request = request,
                amount = amount,
                onBackClick = { activity?.finish() },
                onPayClick = { clientSecret ->
                    paymentSheet.presentWithPaymentIntent(
                        clientSecret,
                        PaymentSheet.Configuration("TradeFlow Rent Payment")
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalPaymentContent(
    request: RequestModel,
    amount: Double,
    onBackClick: () -> Unit,
    onPayClick: (String) -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay Rent", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Rent Payment Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Product: ${request.productName}", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rental Period Used: Custom Calculation")
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Rent Due:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Rs $amount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Greenish)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isProcessing = true
                    fetchPaymentIntent(amount) { clientSecret ->
                        isProcessing = false
                        if (clientSecret != null) {
                            onPayClick(clientSecret)
                        } else {
                            Toast.makeText(context, "Failed to initiate payment", Toast.LENGTH_SHORT).show()
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
                    Text("Pay Rs $amount", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Reusing PaymentSuccessContent but customized for Rent
@Composable
fun PaymentSuccessContent(
    request: RequestModel,
    amount: Double,
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
            text = "Rent Paid Successfully!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Amount Paid: Rs $amount",
            fontSize = 18.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please ask owner to return your security deposit.",
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
            Text("Back", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

    // Helper to fetch PaymentIntent (Simulated for now based on UserPointsActivity logic)
    private fun fetchPaymentIntent(amount: Double, onResult: (String?) -> Unit) {
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

private fun generateRentBill(context: android.content.Context, request: RequestModel, amount: Double) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("TradeFlow Rent Receipt", 100f, 80f, paint)
    
    paint.textSize = 14f
    paint.isFakeBoldText = false
    var yPos = 140f
    
    canvas.drawText("Receipt ID: ${System.currentTimeMillis()}", 80f, yPos, paint)
    yPos += 30f
    canvas.drawText("Date: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())}", 80f, yPos, paint)
    yPos += 50f
    
    paint.isFakeBoldText = true
    canvas.drawText("Payment Details", 80f, yPos, paint)
    paint.isFakeBoldText = false
    yPos += 30f
    canvas.drawText("Product: ${request.productName}", 80f, yPos, paint)
    yPos += 30f
    canvas.drawText("Final Rent Amount: Rs $amount", 80f, yPos, paint)
    yPos += 50f
    
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Thank you for using TradeFlow!", 200f, 800f, paint)
    
    pdfDocument.finishPage(page)
    
    val fileName = "Rent_Bill_${System.currentTimeMillis()}.pdf"
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = android.content.ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Toast.makeText(context, "Bill saved to Downloads", Toast.LENGTH_LONG).show()
        }
    } else {
        val file = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        try {
            pdfDocument.writeTo(java.io.FileOutputStream(file))
            Toast.makeText(context, "Bill saved to Downloads", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save bill", Toast.LENGTH_SHORT).show()
        }
    }
    pdfDocument.close()
}
