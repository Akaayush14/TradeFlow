package com.example.tradeflow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradeflow.ui.theme.DarkGreen
import com.example.tradeflow.ui.theme.Greenish

class AdminTermsAndCondition : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminTermsAndConditionScreen(
                onBackClick = {
                    val intent = Intent(this, AdminSettings::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminTermsAndConditionScreen(onBackClick: () -> Unit = {}) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Greenish,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                            text = "Terms and Conditions",
                            color = Color.White,
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
                .background(Color(0xFFF5F5F5))
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "TradeFlow Terms and Conditions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Greenish,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Last updated: December 2024",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Introduction
            TermsSection(
                title = "1. Agreement to Terms",
                content = "By accessing and using TradeFlow, you agree to be bound by these Terms and Conditions. If you disagree with any part of these terms, you may not access the service. These terms apply to all users, including administrators, traders, and visitors."
            )

            // Account Terms
            TermsSection(
                title = "2. Account Terms",
                items = listOf(
                    "You must be at least 18 years of age to use this service",
                    "You must provide accurate and complete information during registration",
                    "You are responsible for maintaining the security of your account",
                    "You are responsible for all activities that occur under your account",
                    "You must not use the service for any illegal or unauthorized purpose",
                    "You must not violate any laws in your jurisdiction",
                    "One person or legal entity may maintain only one account"
                )
            )

            // Trading Terms
            TermsSection(
                title = "3. Trading and Transactions",
                items = listOf(
                    "All trades are binding agreements between users",
                    "TradeFlow acts as a facilitator and is not a party to transactions",
                    "Users are responsible for verifying the quality and condition of items",
                    "TradeFlow does not guarantee the value or authenticity of items",
                    "Disputes must be resolved between trading parties",
                    "Users must honor all trade commitments made on the platform",
                    "Fraudulent trading activity will result in immediate account termination"
                )
            )

            // Credit System
            TermsSection(
                title = "4. Credit System",
                items = listOf(
                    "Credits are virtual tokens used to facilitate trades on the platform",
                    "Credits have no real-world monetary value",
                    "Credits cannot be exchanged for cash or transferred off-platform",
                    "TradeFlow reserves the right to adjust credit values to maintain platform balance",
                    "Manipulation of the credit system is strictly prohibited",
                    "Credits may expire after prolonged account inactivity",
                    "TradeFlow may revoke credits obtained through fraudulent means"
                )
            )

            // Prohibited Activities
            TermsCard(
                title = "5. Prohibited Activities",
                items = listOf(
                    "Posting illegal, stolen, or counterfeit items",
                    "Engaging in fraud or deceptive practices",
                    "Harassing, threatening, or abusing other users",
                    "Creating multiple accounts to manipulate the system",
                    "Using automated tools or bots without authorization",
                    "Posting spam, advertisements, or promotional content",
                    "Attempting to hack, damage, or disrupt the platform",
                    "Impersonating other users or administrators",
                    "Violating intellectual property rights",
                    "Trading prohibited items (weapons, drugs, etc.)"
                ),
                isWarning = true
            )

            // Content Guidelines
            TermsSection(
                title = "6. User Content",
                items = listOf(
                    "You retain ownership of content you post on TradeFlow",
                    "You grant TradeFlow a license to use, display, and distribute your content",
                    "You are responsible for the content you post",
                    "Content must not violate any laws or third-party rights",
                    "TradeFlow reserves the right to remove any content at its discretion",
                    "You must not post offensive, obscene, or inappropriate content",
                    "Images must accurately represent the items being traded"
                )
            )

            // Privacy and Data
            TermsSection(
                title = "7. Privacy and Data Collection",
                items = listOf(
                    "TradeFlow collects and processes personal data as described in our Privacy Policy",
                    "We use cookies and similar technologies to enhance user experience",
                    "Your data may be shared with third-party service providers",
                    "We implement security measures to protect your information",
                    "You have rights regarding your personal data under applicable laws",
                    "We may use your data for analytics and platform improvement"
                )
            )

            // Liability and Disclaimers
            TermsSection(
                title = "8. Limitation of Liability",
                content = "TradeFlow is provided \"as is\" without warranties of any kind. We do not guarantee uninterrupted or error-free service. TradeFlow is not liable for any direct, indirect, incidental, or consequential damages arising from your use of the service. This includes but is not limited to loss of data, loss of profits, or damage to items traded on the platform."
            )

            // Intellectual Property
            TermsSection(
                title = "9. Intellectual Property Rights",
                items = listOf(
                    "TradeFlow and its original content are protected by copyright and trademark laws",
                    "You may not copy, modify, or distribute our content without permission",
                    "The TradeFlow logo and brand are registered trademarks",
                    "User-generated content remains the property of respective users",
                    "You must not use TradeFlow's intellectual property for commercial purposes"
                )
            )

            // Termination
            TermsSection(
                title = "10. Account Termination",
                items = listOf(
                    "You may terminate your account at any time",
                    "TradeFlow may suspend or terminate accounts that violate these terms",
                    "Termination may occur without prior notice for serious violations",
                    "Upon termination, your right to use the service ceases immediately",
                    "Unused credits will be forfeited upon account termination",
                    "TradeFlow reserves the right to delete inactive accounts"
                )
            )

            // Modifications
            TermsSection(
                title = "11. Changes to Terms",
                content = "TradeFlow reserves the right to modify these Terms and Conditions at any time. We will notify users of significant changes via email or platform notification. Continued use of the service after changes constitutes acceptance of the new terms. It is your responsibility to review these terms periodically."
            )

            // Dispute Resolution
            TermsSection(
                title = "12. Dispute Resolution",
                items = listOf(
                    "Any disputes shall be resolved through binding arbitration",
                    "You waive the right to participate in class-action lawsuits",
                    "Disputes must be filed within one year of occurrence",
                    "Arbitration will be conducted under applicable arbitration rules",
                    "The arbitration location shall be determined by TradeFlow",
                    "Each party bears their own legal costs unless otherwise determined"
                )
            )

            // Governing Law
            TermsSection(
                title = "13. Governing Law",
                content = "These Terms and Conditions are governed by and construed in accordance with the laws of the jurisdiction in which TradeFlow operates, without regard to its conflict of law provisions. Our failure to enforce any right or provision of these terms will not be considered a waiver of those rights."
            )

            // Contact Information
            TermsSection(
                title = "14. Contact Information",
                content = "If you have any questions about these Terms and Conditions, please contact us at:\n\nEmail: support@tradeflow.com\nPhone: +1 (555) 123-4567\nAddress: TradeFlow Inc., 123 Trade Street, Commerce City, CC 12345"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Acknowledgment
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Greenish.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Acceptance of Terms",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "By creating an account and using TradeFlow, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions. If you do not agree to these terms, you must not use our service.",
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Text(
                text = "© 2024 TradeFlow. All rights reserved.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun TermsSection(
    title: String,
    content: String? = null,
    items: List<String>? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (content != null) {
            Text(
                text = content,
                fontSize = 15.sp,
                color = Color(0xFF333333),
                lineHeight = 22.sp
            )
        }

        if (items != null) {
            items.forEach { item ->
                Text(
                    text = "• $item",
                    fontSize = 15.sp,
                    color = Color(0xFF333333),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TermsCard(
    title: String,
    items: List<String>,
    isWarning: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) Color(0xFFE65100) else DarkGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            items.forEach { item ->
                Text(
                    text = "• $item",
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 8.dp)
                )
            }
        }
    }
}
