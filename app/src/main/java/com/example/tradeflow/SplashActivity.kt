package com.example.tradeflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.tradeflow.ui.theme.TradeFlowTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {

    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(Unit) {
        delay(2000)
        val intent = Intent(context, LoginUI::class.java)
    }

    Scaffold { padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ){

        }
    }
}

@Preview
@Composable
fun SplashBodyPreview() {
    SplashBody()
}