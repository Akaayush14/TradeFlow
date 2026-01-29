package com.example.tradeflow

import com.example.tradeflow.model.ProductModel
import com.example.tradeflow.repository.ProductRepo
import com.example.tradeflow.viewmodel.ProductViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AddItemUnitTest {

    @Test
    fun add_item_success_test() {
        val repo = mock<ProductRepo>()
        val viewModel = ProductViewModel(repo)
        val product = ProductModel(
            productId = "p1",
            name = "Test Product",
            price = 100.0,
            ownerId = "user1"
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Product added successfully")
            null
        }.`when`(repo).addProduct(any(), any())

        var successResult = false
        var messageResult = ""

        viewModel.addProduct(product) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Product added successfully", messageResult)

        verify(repo).addProduct(eq(product), any())
    }
}
