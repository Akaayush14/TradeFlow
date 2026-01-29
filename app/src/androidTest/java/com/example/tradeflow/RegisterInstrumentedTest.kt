package com.example.tradeflow

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tradeflow.RegisterActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<RegisterActivity>()

    @Test
    fun testSuccessfulRegister_finishesActivity() {
        // Enter Name
        composeRule.onNodeWithTag("name")
            .performTextInput("Test User")

        // Enter Phone
        composeRule.onNodeWithTag("phone")
            .performTextInput("1234567890")

        // Enter Email
        composeRule.onNodeWithTag("email")
            .performTextInput("test@tradeflow.com")

        // Enter Password
        composeRule.onNodeWithTag("password")
            .performTextInput("password")

        // Enter Confirm Password
        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("password")

        // Accept Terms (performScrollTo ensures it's visible on smaller screens)
        composeRule.onNodeWithTag("terms")
            .performScrollTo()
            .performClick()

        // Click Register
        composeRule.onNodeWithTag("registerButton")
            .performScrollTo()
            .performClick()

        // Wait for UI events to propagate
        composeRule.waitForIdle()
        
        // Check if activity is finishing
        // Note: checking isFinishing is reliable for verifying finish() was called
        assertTrue("Activity should be finishing after successful registration", 
            composeRule.activity.isFinishing || composeRule.activity.isDestroyed)
    }
}
