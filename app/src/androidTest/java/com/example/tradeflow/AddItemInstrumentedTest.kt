package com.example.tradeflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tradeflow.view.UserAddItemScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddItemInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testAddItemScreen_UIElementsExist() {
        // Set the content
        composeRule.setContent {
            UserAddItemScreen()
        }

        // Check if all fields exist
        composeRule.onNodeWithTag("itemNameInput").assertIsDisplayed()
        composeRule.onNodeWithTag("itemPriceInput").assertIsDisplayed()
        composeRule.onNodeWithTag("itemCategoryInput").assertIsDisplayed()
        composeRule.onNodeWithTag("itemLocationInput").assertIsDisplayed()
        
        // Description might be off screen, so we scroll to it
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("itemDescriptionInput"))
        composeRule.onNodeWithTag("itemDescriptionInput").assertIsDisplayed()
        
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("termsCheckbox"))
        composeRule.onNodeWithTag("termsCheckbox").assertIsDisplayed()
        
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("submitButton"))
        composeRule.onNodeWithTag("submitButton").assertIsDisplayed()
    }

    @Test
    fun testAddItemScreen_Validation() {
        composeRule.setContent {
            UserAddItemScreen()
        }

        // Scroll to button and click without filling fields
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("submitButton"))
        composeRule.onNodeWithTag("submitButton").performClick()

        // Expect error dialog
        composeRule.onNodeWithText("Error").assertIsDisplayed()
        composeRule.onNodeWithText("Please fill all fields and agree to terms").assertIsDisplayed()
    }

    @Test
    fun testAddItemScreen_FormFill_ValidationPassed() {
        composeRule.setContent {
            UserAddItemScreen()
        }

        // Fill Name
        composeRule.onNodeWithTag("itemNameInput").performTextInput("Test Item")
        
        // Fill Price
        composeRule.onNodeWithTag("itemPriceInput").performTextInput("100")

        // Fill Category
        composeRule.onNodeWithTag("itemCategoryInput").performTextInput("Test Category")

        // Fill Location
        composeRule.onNodeWithTag("itemLocationInput").performTextInput("Test Location")

        // Fill Description
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("itemDescriptionInput"))
        composeRule.onNodeWithTag("itemDescriptionInput").performTextInput("This is a test description")

        // Select Purpose (Rent)
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("itemPurposeDropdown"))
        composeRule.onNodeWithTag("itemPurposeDropdown").performClick()
        composeRule.onNodeWithText("Rent").performClick()

        // Check Checkbox
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("termsCheckbox"))
        composeRule.onNodeWithTag("termsCheckbox").performClick()

        // Click Submit
        composeRule.onNodeWithTag("addItemList").performScrollToNode(hasTestTag("submitButton"))
        composeRule.onNodeWithTag("submitButton").performClick()

        // We expect the form validation to pass, so the "Please fill all fields" error should NOT be visible.
        composeRule.onNodeWithText("Please fill all fields and agree to terms").assertDoesNotExist()
        
        // We expect SOME error (Login or Image) or Success (unlikely without image)
        // verifying that the dialog title is displayed is enough to confirm interaction occurred
        composeRule.onNodeWithText("Error").assertIsDisplayed()
    }
}
