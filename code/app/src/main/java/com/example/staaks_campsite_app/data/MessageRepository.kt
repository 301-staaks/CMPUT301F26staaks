package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Message
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * saves messages in firestore
 *
 * still needed: show messages in time order and update
 * the conversation when a message is sent
 *
 * @property messages the messages loaded from firestore
 */

class MessageRepository {
    private val db = Firebase.firestore
    private val messagesRef = db.collection("messages")

    private val _messages = mutableStateListOf<Message>()

    val messages: List<Message>
        get() = _messages

    /**
     * saves a message in firestore
     *
     * creates an id if the message does not have one
     * @param message the message we want to save
     */

    fun sendMessage(message: Message) {
        if (message.messageId.isBlank()) {
            message.messageId = messagesRef.document().id
        }

        messagesRef.document(message.messageId).set(message)
    }

    /**
     * finds the loaded messages for a conversation
     *
     * does not put the messages in time order
     *
     * @param conversationId the id of the conversation
     * @return its messages or an empty list if none are loaded
     */
    fun getMessages(conversationId: String): List<Message> {
        val result = mutableListOf<Message>()

        for (message in _messages) {
            if (message.conversationId == conversationId) {
                result.add(message)
            }
        }

        return result
    }

    /**
     * gets all loaded messages for the admin screen
     *
     * does not check whether the user is an admin
     *
     * @return the list of loaded messages
     */
    fun getAllMessages(): List<Message> {
        return messages
    }

    /**
     * marks a loaded message as read and saves it in firestore
     *
     * @param messageId the id of the message
     */

    fun markMessageAsRead(messageId: String) {
        for (message in _messages) {
            if (message.messageId == messageId) {
                val updatedMessage = Message(
                    messageId = message.messageId,
                    conversationId = message.conversationId,
                    senderId = message.senderId,
                    text = message.text,
                    sentDate = message.sentDate,
                    isRead = true
                )

                messagesRef.document(messageId).set(updatedMessage)
                return
            }
        }
    }

    init {
        messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _messages.clear()

            snapshot?.documents?.forEach { document ->
                val message = document.toObject(Message::class.java)

                if (message != null) {
                    message.messageId = document.id
                    _messages.add(message)
                }
            }
        }
    }
}