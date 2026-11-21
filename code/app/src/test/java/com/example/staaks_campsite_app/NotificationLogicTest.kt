package com.example.staaks_campsite_app

import com.example.staaks_campsite_app.data.confirmationMessage
import com.example.staaks_campsite_app.data.inboxNotifications
import com.example.staaks_campsite_app.data.notSelectedMessage
import com.example.staaks_campsite_app.data.ownerTitle
import com.example.staaks_campsite_app.data.winMessage
import com.example.staaks_campsite_app.data.model.Notification
import com.example.staaks_campsite_app.data.model.NotificationType
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the notification text and inbox logic.
 * Covers US 01.03.01, US 01.03.02, US 01.05.01 and US 02.10.01.
 */
class NotificationLogicTest {

    private fun notification(id: String, seconds: Long, cleared: Boolean = false) = Notification(
        notificationId = id,
        recipientId = "e1",
        type = NotificationType.WIN,
        sentAt = Timestamp(seconds, 0),
        cleared = cleared
    )

    @Test
    fun inboxIsNewestFirst() {
        val list = listOf(notification("old", 100), notification("new", 200), notification("mid", 150))
        assertEquals(listOf("new", "mid", "old"), inboxNotifications(list).map { it.notificationId })
    }

    @Test
    fun inboxHidesClearedNotifications() {
        val list = listOf(notification("kept", 100), notification("cleared", 200, cleared = true))
        assertEquals(listOf("kept"), inboxNotifications(list).map { it.notificationId })
    }

    @Test
    fun newNotificationIsUnreadUntilMarked() {
        val n = notification("n1", 0)
        assertFalse(n.isRead)
        n.markAsRead()
        assertTrue(n.isRead)
    }

    @Test
    fun winMessageNamesCampsite() {
        assertTrue(winMessage("Two Jack Lake").contains("Two Jack Lake"))
    }

    @Test
    fun notSelectedMessageSaysTheyMayStillBeSelected() {
        val message = notSelectedMessage("Two Jack Lake")
        assertTrue(message.contains("Two Jack Lake"))
        assertTrue(message.contains("may still be selected"))
    }

    @Test
    fun confirmationMessageHasCampsiteAndDates() {
        val message = confirmationMessage("Two Jack Lake", "2027-05-21", "2027-05-23")
        assertTrue(message.contains("Two Jack Lake"))
        assertTrue(message.contains("2027-05-21"))
        assertTrue(message.contains("2027-05-23"))
    }

    @Test
    fun blankOwnerTitleUsesCampsiteName() {
        assertEquals("Message from Two Jack Lake", ownerTitle("  ", "Two Jack Lake"))
        assertEquals("Check-in", ownerTitle(" Check-in ", "Two Jack Lake"))
    }
}
