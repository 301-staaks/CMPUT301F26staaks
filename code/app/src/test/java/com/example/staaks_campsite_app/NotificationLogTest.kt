package com.example.staaks_campsite_app

import com.example.staaks_campsite_app.data.groupIntoSends
import com.example.staaks_campsite_app.data.logName
import com.example.staaks_campsite_app.data.notificationTypeLabel
import com.example.staaks_campsite_app.data.recipientSummary
import com.example.staaks_campsite_app.data.model.Notification
import com.example.staaks_campsite_app.data.model.NotificationType
import com.example.staaks_campsite_app.data.model.SYSTEM_SENDER
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the admin notification log (US 03.08.01).
 */
class NotificationLogTest {

    private fun notification(
        id: String,
        recipientId: String,
        batchId: String = "",
        seconds: Long = 0,
        senderId: String = "o1",
        cleared: Boolean = false
    ) = Notification(
        notificationId = id,
        senderId = senderId,
        recipientId = recipientId,
        type = NotificationType.OWNER,
        title = "Title $id",
        message = "Message $id",
        batchId = batchId,
        sentAt = Timestamp(seconds, 0),
        cleared = cleared
    )

    @Test
    fun oneSendToManyRecipientsIsOneEntry() {
        val list = listOf(
            notification("n1", "e1", batchId = "b1"),
            notification("n2", "e2", batchId = "b1"),
            notification("n3", "e3", batchId = "b1")
        )
        val sends = groupIntoSends(list)

        assertEquals(1, sends.size)
        assertEquals(listOf("e1", "e2", "e3"), sends[0].recipientIds)
    }

    @Test
    fun logIsNewestFirst() {
        val list = listOf(
            notification("old", "e1", batchId = "b1", seconds = 100),
            notification("new", "e1", batchId = "b2", seconds = 300),
            notification("mid", "e1", batchId = "b3", seconds = 200)
        )
        assertEquals(listOf("b2", "b3", "b1"), groupIntoSends(list).map { it.batchId })
    }

    @Test
    fun notificationsWithoutBatchIdAreSeparateEntries() {
        val list = listOf(notification("n1", "e1"), notification("n2", "e2"))
        assertEquals(2, groupIntoSends(list).size)
    }

    @Test
    fun clearedNotificationsStayInTheLog() {
        val list = listOf(notification("n1", "e1", batchId = "b1", cleared = true))
        assertEquals(1, groupIntoSends(list).size)
    }

    @Test
    fun emptyLogHasNoEntries() {
        assertTrue(groupIntoSends(emptyList()).isEmpty())
    }

    @Test
    fun systemSenderShowsAsSystem() {
        assertEquals("System", logName(SYSTEM_SENDER, emptyMap()))
    }

    @Test
    fun knownUserShowsName() {
        assertEquals("Ana", logName("u1", mapOf("u1" to "Ana")))
    }

    @Test
    fun deletedOrUnnamedUserShowsId() {
        assertEquals("u2", logName("u2", mapOf("u1" to "Ana")))
        assertEquals("u3", logName("u3", mapOf("u3" to "")))
    }

    @Test
    fun longRecipientListIsShortened() {
        assertEquals("A, B, C +2 more", recipientSummary(listOf("A", "B", "C", "D", "E")))
        assertEquals("A, B", recipientSummary(listOf("A", "B")))
    }

    @Test
    fun typesHaveReadableLabels() {
        assertEquals("Lottery win", notificationTypeLabel(NotificationType.WIN))
        assertEquals("Owner message", notificationTypeLabel(NotificationType.OWNER))
    }
}
