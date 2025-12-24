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

class AdminPrivacyPolicy : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminPrivacyPolicyScreen(
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
fun AdminPrivacyPolicyScreen(onBackClick: () -> Unit = {}) {
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
                            text = "Privacy & Security",
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
                text = "Admin Guidelines & Policies",
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
            PolicySection(
                title = "Administrator Responsibilities",
                content = "As an administrator of TradeFlow, you have special privileges and responsibilities. This document outlines the rules and guidelines you must follow to maintain the integrity, safety, and fairness of our platform."
            )

            // Core Principles
            PolicySection(
                title = "Core Principles",
                items = listOf(
                    "Act with integrity and transparency in all administrative actions",
                    "Protect user privacy and handle personal data responsibly",
                    "Ensure fair and unbiased treatment of all users",
                    "Maintain the security and stability of the platform",
                    "Uphold community standards and enforce policies consistently"
                )
            )

            // User Data Protection
            PolicySection(
                title = "User Data Protection",
                items = listOf(
                    "Never access user data without legitimate administrative need",
                    "Do not share, sell, or distribute user information to third parties",
                    "Use encryption and secure methods when handling sensitive data",
                    "Report any data breaches immediately to technical team",
                    "Comply with all applicable data protection regulations (GDPR, etc.)",
                    "Maintain confidentiality of user transactions and communications"
                )
            )

            // Content Moderation
            PolicySection(
                title = "Content Moderation Rules",
                items = listOf(
                    "Review reported content within 24 hours",
                    "Remove illegal, harmful, or prohibited content immediately",
                    "Document all moderation actions with clear reasoning",
                    "Provide users with explanation when content is removed",
                    "Apply content policies consistently across all users",
                    "Escalate complex cases to senior administrators"
                )
            )

            // User Management
            PolicySection(
                title = "User Management Guidelines",
                items = listOf(
                    "Issue warnings before taking restrictive actions when appropriate",
                    "Provide clear reasons for account restrictions or bans",
                    "Allow users to appeal moderation decisions",
                    "Document all user management actions in system logs",
                    "Never use admin privileges for personal gain or favoritism",
                    "Treat all users with respect regardless of their status"
                )
            )

            // Prohibited Actions
            PolicyCard(
                title = "⚠️ Strictly Prohibited Actions",
                items = listOf(
                    "Accepting bribes or incentives from users",
                    "Creating fake accounts or manipulating the credit system",
                    "Using your position to gain unfair trading advantages",
                    "Sharing admin credentials with unauthorized persons",
                    "Deleting or modifying records to hide mistakes",
                    "Harassing or discriminating against any user",
                    "Accessing the platform while under influence of substances",
                    "Using admin tools for personal transactions or benefits"
                ),
                isWarning = true
            )

            // Conflict of Interest
            PolicySection(
                title = "Conflict of Interest",
                items = listOf(
                    "Disclose any personal relationships with users you moderate",
                    "Recuse yourself from decisions involving friends or family",
                    "Report potential conflicts of interest to management",
                    "Do not moderate content where you have financial interest",
                    "Maintain professional boundaries with all users"
                )
            )

            // Security Practices
            PolicySection(
                title = "Security Practices",
                items = listOf(
                    "Use strong, unique passwords and enable 2FA",
                    "Never share your admin credentials with anyone",
                    "Log out from admin sessions when finished",
                    "Report suspicious activities immediately",
                    "Keep admin devices secure and updated",
                    "Use VPN when accessing admin panel from public networks",
                    "Review access logs regularly for unauthorized access"
                )
            )

            // Communication Standards
            PolicySection(
                title = "Communication Standards",
                items = listOf(
                    "Maintain professional tone in all user communications",
                    "Respond to user inquiries within 48 hours",
                    "Be clear and concise in your explanations",
                    "Never engage in arguments with users",
                    "Escalate abusive situations to senior staff",
                    "Document important conversations for records"
                )
            )

            // Compliance & Reporting
            PolicySection(
                title = "Compliance & Reporting",
                items = listOf(
                    "Submit weekly activity reports to management",
                    "Report policy violations by other admins immediately",
                    "Participate in mandatory training and policy updates",
                    "Keep accurate records of all administrative actions",
                    "Cooperate fully with internal or external audits",
                    "Stay informed about platform policy changes"
                )
            )

            // Consequences
            PolicyCard(
                title = "Consequences of Policy Violations",
                items = listOf(
                    "Minor violations: Written warning and retraining",
                    "Moderate violations: Temporary suspension of admin privileges",
                    "Serious violations: Permanent removal from admin role",
                    "Illegal activities: Report to law enforcement and legal action",
                    "All violations may result in account termination"
                ),
                isWarning = true
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
                        text = "Administrator Acknowledgment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "By continuing to serve as an administrator, you acknowledge that you have read, understood, and agree to comply with all policies outlined in this document. Failure to adhere to these guidelines may result in disciplinary action up to and including termination.",
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
fun PolicySection(
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
fun PolicyCard(
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