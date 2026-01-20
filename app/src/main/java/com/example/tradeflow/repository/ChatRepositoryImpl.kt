package com.example.tradeflow.repository

import com.example.tradeflow.model.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.ai.client.generativeai.GenerativeModel
import android.util.Log
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class ChatRepositoryImpl(private val context: Context) : ChatRepository {

    private val database = FirebaseDatabase.getInstance()


    private fun getRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun sendMessage(senderId: String, receiverId: String, message: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val roomId = getRoomId(senderId, receiverId)
        val ref = database.getReference("chats").child(roomId).push()
        val chatMessage = ChatMessage(
            id = ref.key ?: "",
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        ref.setValue(chatMessage)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    override suspend fun generateBotResponse(senderId: String, prompt: String) {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            Log.e("ChatBot", "No network connectivity available")
            sendMessage("chat_bot", senderId, "No internet connection. Please check your network and try again.", {}, {})
            return
        }
        
        try {
            // Verify API key format first
            val apiKey = "AIzaSyA8SDbTu6ewxq2QCoHb_cRUV270bIc5gsc"
            if (apiKey.isBlank() || !apiKey.startsWith("AIza")) {
                throw IllegalArgumentException("Invalid API key format")
            }
            
            // Use gemini-2.5-flash - the current stable model
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = apiKey
            )

            Log.d("ChatBot", "Sending request to Gemini API with model: gemini-2.5-flash")
            Log.d("ChatBot", "API key: ${apiKey.take(10)}...")
            
            val response = generativeModel.generateContent(prompt)
            val botReply = response.text ?: "I'm sorry, I couldn't understand that."

            Log.d("ChatBot", "Received response: $botReply")
            sendMessage("chat_bot", senderId, botReply, {}, {})

        } catch (e: Exception) {
            Log.e("ChatBot", "Primary model error - Type: ${e.javaClass.simpleName}, Message: ${e.message}", e)
            e.printStackTrace()
            
            // Log the full exception details for debugging
            Log.e("ChatBot", "Full exception: $e")

            // Try fallback to flash-lite if flash fails
            tryFallbackModel(senderId, prompt, e)
        }
    }

    private suspend fun tryFallbackModel(senderId: String, prompt: String, originalError: Exception) {
        try {
            Log.d("ChatBot", "Trying fallback model: gemini-2.0-flash-lite")
            val apiKey = "AIzaSyA8SDbTu6ewxq2QCoHb_cRUV270bIc5gsc"
            
            val fallbackModel = GenerativeModel(
                modelName = "gemini-2.0-flash-lite",
                apiKey = apiKey
            )

            Log.d("ChatBot", "Fallback API key: ${apiKey.take(10)}...")
            val response = fallbackModel.generateContent(prompt)
            val botReply = response.text ?: "I'm sorry, I couldn't understand that."

            Log.d("ChatBot", "Fallback successful: $botReply")
            sendMessage("chat_bot", senderId, botReply, {}, {})

        } catch (e: Exception) {
            Log.e("ChatBot", "Fallback model error - Type: ${e.javaClass.simpleName}, Message: ${e.message}", e)
            Log.e("ChatBot", "Original error - Type: ${originalError.javaClass.simpleName}, Message: ${originalError.message}")

            val errorMessage = when {
                originalError.message?.contains("API key", ignoreCase = true) == true ->
                    "API configuration error. Please verify your API key."
                originalError.message?.contains("quota", ignoreCase = true) == true ||
                        originalError.message?.contains("429", ignoreCase = true) == true ->
                    "Daily limit reached. Try again later."
                originalError.message?.contains("404", ignoreCase = true) == true ||
                        originalError.message?.contains("not found", ignoreCase = true) == true ->
                    "Service temporarily unavailable. Please try again."
                originalError.message?.contains("service unavailable", ignoreCase = true) == true ||
                        originalError.message?.contains("503", ignoreCase = true) == true ->
                    "Service temporarily unavailable. Please try again in a few moments."
                e.message?.contains("service unavailable", ignoreCase = true) == true ||
                        e.message?.contains("503", ignoreCase = true) == true ->
                    "Service temporarily unavailable. Please try again in a few moments."
                else ->
                    "Sorry, I'm having trouble responding. Error: ${originalError.message?.take(50) ?: e.message?.take(50)}"
            }
            sendMessage("chat_bot", senderId, errorMessage, {}, {})
        }
    }

    override fun getMessages(senderId: String, receiverId: String): Flow<List<ChatMessage>> = callbackFlow {
        val roomId = getRoomId(senderId, receiverId)
        val ref = database.getReference("chats").child(roomId).orderByChild("timestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val message = child.getValue(ChatMessage::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                trySend(messages.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}