package com.example.staaks_campsite_app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp

/**
 * stores a message sent in a conversation between two users
 *
 * @property messageId the ID of this message
 * @property conversationId the ID of the conversation it belongs to
 * @property senderId the ID of the user who sent it
 * @property text what the user wrote
 * @property sentDate the date the message was sent
 * @property isRead whether the other user has read it
 */
class Message(
    var messageId: String = "",
    var conversationId: String = "",
    var senderId: String = "",
    var text: String = "",
    @ServerTimestamp var sentDate: Timestamp? = null,
    var isRead: Boolean = false
) {
    /**
     * marks this message as rea
     */
    fun markAsRead() {
        isRead = true
    }
}