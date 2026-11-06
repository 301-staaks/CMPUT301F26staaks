package com.example.staaks_campsite_app.data.model

/**
 * stores the users and latest message details of a conversation
 * the messages themselves are stored separately
 *
 * @property conversationId the ID of this conversation
 * @property participantIds the IDs of the users in the conversation
 * @property campsiteId the ID of the campsite the conversation is about
 * @property lastMessageText the latest message shown in the conversation
 * @property updatedDate the date the conversation was last updated
 */
class Conversation(
    var conversationId: String = "",
    var participantIds: List<String> = emptyList(),
    var campsiteId: String = "",
    var lastMessageText: String = "",
    var updatedDate: String = ""
) {
    /**
     * checks if a user is part of this conversation
     *
     * @param userId the ID of the user we are looking for
     * @return true if the user is in the participant list
     */
    fun hasParticipant(userId: String): Boolean {
        for (participantId in participantIds) {
            if (participantId == userId) {
                return true
            }
        }

        return false
    }
}