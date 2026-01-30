package com.example.tradeflow

import android.app.Activity
import android.app.Instrumentation
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tradeflow.view.LoginActivity
import com.example.tradeflow.view.UserDashboard
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
        // Stub the intent to prevent the actual activity from starting
        // This isolates the test and prevents crashes if the destination activity fails to load
        intending(hasComponent(UserDashboard::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Enter email
        composeRule.onNodeWithTag("email")
            .performTextInput("test@tradeflow.com")

        // Enter password
        composeRule.onNodeWithTag("password")
            .performTextInput("password")

        // Click Login
        composeRule.onNodeWithTag("loginButton")
            .performClick()

        // Poll for the intent to handle async Firebase login delay
        // This loop waits up to 10 seconds for the navigation intent to be fired
        val startTime = System.currentTimeMillis()
        val timeout = 10000L // 10 seconds allowance for network/auth
        var intended = false
        
        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                Intents.intended(hasComponent(UserDashboard::class.java.name))
                intended = true
                break
            } catch (e: AssertionError) {
                try {
                    Thread.sleep(200)
                } catch (ignored: InterruptedException) {}
            }
        }
        
        // If not found after timeout, perform the check again to throw the failure
        if (!intended) {
            Intents.intended(hasComponent(UserDashboard::class.java.name))
        }
    }
}
