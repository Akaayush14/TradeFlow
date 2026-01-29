package com.example.tradeflow

import com.example.tradeflow.repository.UserRepo
import com.example.tradeflow.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify


class RegisterUnitTest {

    @Test
    fun register_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(3)
            callback(true, "Registration success", "user123")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), eq("1234567890"), any())

        var successResult = false
        var messageResult = ""
        var userIdResult = ""

        viewModel.register("test@gmail.com", "123456", "1234567890") { success, msg, userId ->
            successResult = success
            messageResult = msg
            userIdResult = userId
        }

        assertTrue(successResult)
        assertEquals("Registration success", messageResult)
        assertEquals("user123", userIdResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), eq("1234567890"), any())
    }

}
