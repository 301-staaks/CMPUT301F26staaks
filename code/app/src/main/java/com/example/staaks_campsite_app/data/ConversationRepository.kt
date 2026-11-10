package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Admin
import com.example.staaks_campsite_app.data.model.Conversation
import com.example.staaks_campsite_app.data.model.Entrant
import com.example.staaks_campsite_app.data.model.Message
import com.example.staaks_campsite_app.data.model.Owner
import com.example.staaks_campsite_app.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.emptyList

/**
 * @property conversations the conversations loaded from firestore
 */
class ConversationRepository {
    private val db = Firebase.firestore
    private val conversationsRef = db.collection("conversations")

    private val _conversations = mutableStateListOf<Conversation>()

    val conversations: List<Conversation>
        get() = _conversations

    /**
     * saves a conversation in firestore
     *
     * @param conversation the conversation we want to save
     */
    fun createConversation(conversation: Conversation) {
        if (conversation.conversationId.isBlank()) {
            conversation.conversationId = conversationsRef.document().id
        }

        conversationsRef.document(conversation.conversationId)
            .set(conversation)
    }

    /**
     * looks for a conversation in the loaded list
     *
     * @param conversationId the id of the conversation
     * @return the conversation or null if it is not in the list
     */
    fun getConversation(conversationId: String): Conversation? {
        for (conversation in _conversations) {
            if (conversation.conversationId == conversationId) {
                return conversation
            }
        }

        return null
    }

    /**
     * finds the loaded conversations that a user is part of
     *
     * adds each conversation to the result only once
     *
     * @param userId the id of the user
     * @return their conversations or an empty list if none are loaded
     */
    fun getConversationsForUser(userId: String): List<Conversation> {
        val result = mutableListOf<Conversation>()

        for (conversation in _conversations) {
            for (participantId in conversation.participantIds) {
                if (participantId == userId) {
                    result.add(conversation)
                    break
                }
            }
        }

        return result
    }


    fun sendMessage(conversationId: String, message: Message) {
        val messages = db.collection("conversations").document(conversationId).collection("messages")
        message.messageId = messages.document().id
        messages.document(message.messageId).set(message)
    }

    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow() {
        val listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("sentDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    fun observeConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val reg = conversationsRef
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Conversation::class.java)
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun deleteConversation(conversation: Conversation) {
        conversationsRef.document(conversation.conversationId).delete()
    }

    init {
        conversationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _conversations.clear()

            snapshot?.documents?.forEach { document ->
                val conversation = document.toObject(
                    Conversation::class.java
                )

                if (conversation != null) {
                    conversation.conversationId = document.id
                    _conversations.add(conversation)
                }
            }
        }
    }
}