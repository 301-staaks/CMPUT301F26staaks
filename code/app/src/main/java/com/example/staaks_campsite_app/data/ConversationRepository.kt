package com.example.staaks_campsite_app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Admin
import com.example.staaks_campsite_app.data.model.Conversation
import com.example.staaks_campsite_app.data.model.Entrant
import com.example.staaks_campsite_app.data.model.Message
import com.example.staaks_campsite_app.data.model.Owner
import com.example.staaks_campsite_app.data.model.User
import com.example.staaks_campsite_app.data.model.UserType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
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
    fun createConversation2(conversation: Conversation) {
        if (conversation.conversationId.isBlank()) {
            conversation.conversationId = conversationsRef.document().id
        }

        conversationsRef.document(conversation.conversationId)
            .set(conversation)
    }

    // new ver of create conversation. kept old version just in case.
    fun createConversation(fromId: String, toId: String) {
        conversationsRef
            .whereArrayContains("participantIds", fromId)
            .get()
            .addOnSuccessListener { snapshot ->
                val exists = snapshot.documents.any {
                    (it.get("participantIds") as? List<*>)?.contains(toId) == true
                }
                if (exists) return@addOnSuccessListener

                val conversation = Conversation()
                conversation.participantIds = listOf(fromId, toId)
                conversation.conversationId = conversationsRef.document().id
                conversation.messages = emptyList()

                conversationsRef.document(conversation.conversationId).set(conversation)
            }
    }

    /**
     * looks for a conversation in the loaded list
     *
     * @param conversationId the id of the conversation
     * @return the conversation or null if it is not in the list
     */
    fun getConversation(conversationId: String): Conversation? {
        return _conversations.find { it.conversationId == conversationId }
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


    fun sendMessage(message: Message) {
        val messages = db.collection("conversations").document(message.conversationId).collection("messages")
        message.messageId = messages.document().id
        messages.document(message.messageId).set(message)
    }

    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val rawDate = doc.get("sentDate")
                    val timestamp: Timestamp? = when (rawDate) {
                        is Timestamp -> rawDate
                        is Long -> Timestamp(rawDate / 1000, 0)
                        is String -> {
                            try {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                sdf.parse(rawDate)?.let { Timestamp(it) }
                            } catch (e: Exception) {null}
                        }
                        else -> null
                    }


                    Message(
                        messageId = doc.id,
                        conversationId = doc.getString("conversationId") ?: conversationId,
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        sentDate = timestamp,
                        isRead = doc.getBoolean("isRead") ?: false
                    )

                } ?: emptyList()

                val sortedMessages = messages.sortedWith( compareBy(nullsFirst()) {it.sentDate} )

                trySend(sortedMessages)
            }

        awaitClose { listener.remove() }
    }

    fun observeConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val reg = conversationsRef
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Conversation::class.java)?.apply {conversationId = doc.id}
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