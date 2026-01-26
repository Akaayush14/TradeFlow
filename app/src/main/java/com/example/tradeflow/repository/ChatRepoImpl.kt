package com.example.tradeflow.repository

import com.example.tradeflow.model.MessageModel
import com.example.tradeflow.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class ChatRepoImpl : ChatRepo {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val messagesRef = database.getReference("messages")
    private val usersRef = database.getReference("Users")

    private val messageListeners = mutableMapOf<String, ValueEventListener>()
    private val messageListenerRefs = mutableMapOf<String, DatabaseReference>()

    override fun sendMessage(
        receiverId: String,
        message: MessageModel,
        callback: (Boolean, String) -> Unit
    ) {
        val sender = getCurrentUser() ?: return callback(false, "Not logged in")

        val chatId = generateChatId(sender.uid, receiverId)
        val messageId = messagesRef.child(chatId).push().key ?: UUID.randomUUID().toString()

        val msg = message.copy(
            messageId = messageId,
            senderId = sender.uid,
            receiverId = receiverId,
            timestamp = System.currentTimeMillis()
        )

        messagesRef.child(chatId).child(messageId)
            .setValue(msg)
            .addOnSuccessListener {
                callback(true, "Message sent")
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Failed to send message")
            }
    }

    override fun getMessages(
        receiverId: String,
        onNewMessage: (MessageModel) -> Unit,
        callback: (Boolean, String) -> Unit
    ) {
        val user = getCurrentUser() ?: return callback(false, "Not logged in")

        val chatId = generateChatId(user.uid, receiverId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<MessageModel>()

                snapshot.children.forEach { childSnapshot ->
                    val message = childSnapshot.getValue(MessageModel::class.java)
                    message?.let {
                        messages.add(it)
                    }
                }

                // Sort messages by timestamp and trigger onNewMessage for each
                messages.sortedBy { it.timestamp }.forEach { message ->
                    onNewMessage(message)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        }

        val chatRef = messagesRef.child(chatId)
        chatRef.addValueEventListener(listener)
        messageListeners[receiverId] = listener
        messageListenerRefs[receiverId] = chatRef
        callback(true, "Listening to messages")
    }

    override fun getChatPartners(
        callback: (Boolean, String, List<String>?) -> Unit
    ) {
        val user = getCurrentUser() ?: return callback(false, "Not logged in", null)
        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val partners = mutableSetOf<String>()
                snapshot.children.forEach { chatSnapshot ->
                    val chatId = chatSnapshot.key ?: return@forEach
                    val parts = chatId.split("_")
                    if (parts.size == 2) {
                        if (parts[0] == user.uid) {
                            partners.add(parts[1])
                        } else if (parts[1] == user.uid) {
                            partners.add(parts[0])
                        }
                    }
                }
                callback(true, "Chat partners fetched", partners.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun removeListeners() {
        messageListeners.forEach { (receiverId, listener) ->
            val ref = messageListenerRefs[receiverId] ?: messagesRef
            ref.removeEventListener(listener)
        }
        messageListeners.clear()
        messageListenerRefs.clear()
    }

    private fun generateChatId(u1: String, u2: String): String =
        listOf(u1, u2).sorted().joinToString("_")
}
