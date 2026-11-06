package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Conversation
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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