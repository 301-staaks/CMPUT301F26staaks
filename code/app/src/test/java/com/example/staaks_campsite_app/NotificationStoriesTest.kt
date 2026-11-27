package com.example.staaks_campsite_app

import com.example.staaks_campsite_app.data.CANCELLATION_CSV_HEADER
import com.example.staaks_campsite_app.data.ENTRANT_RESERVATIONS_ROUTE
import com.example.staaks_campsite_app.data.RESERVATION_CSV_HEADER
import com.example.staaks_campsite_app.data.cancellationCsv
import com.example.staaks_campsite_app.data.cancellationMessage
import com.example.staaks_campsite_app.data.chosenRecipients
import com.example.staaks_campsite_app.data.csvField
import com.example.staaks_campsite_app.data.csvFileName
import com.example.staaks_campsite_app.data.datesBetween
import com.example.staaks_campsite_app.data.winChoiceErrors
import com.example.staaks_campsite_app.data.entrantLotteryStatus
import com.example.staaks_campsite_app.data.inboxRoute
import com.example.staaks_campsite_app.data.lotteryReservationId
import com.example.staaks_campsite_app.data.opensDetail
import com.example.staaks_campsite_app.data.notifiableEntrants
import com.example.staaks_campsite_app.data.ownerNotificationErrors
import com.example.staaks_campsite_app.data.reservationCsv
import com.example.staaks_campsite_app.data.sentConfirmation
import com.example.staaks_campsite_app.data.model.Notification
import com.example.staaks_campsite_app.data.model.NotificationType
import com.example.staaks_campsite_app.data.model.WaitlistEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the cancellation text, inbox routing, CSV files and the
 * owner Send Notification rules.
 * Covers US 01.03.01, US 01.03.02, US 01.05.02, US 01.07.02 and US 02.10.01.
 */
class NotificationStoriesTest {

    // ---- US 01.07.02 cancellation notification ----

    @Test
    fun cancellationMessageHasCampsiteDatesAndCancellationDate() {
        val text = cancellationMessage("Pine Lake", "2027-06-01", "2027-06-03", "2027-05-20")
        assertTrue(text.contains("Pine Lake"))
        assertTrue(text.contains("2027-06-01"))
        assertTrue(text.contains("2027-06-03"))
        assertTrue(text.contains("2027-05-20"))
    }

    // ---- inbox taps ----

    @Test
    fun notSelectedOpensReservationsTab() {
        val n = Notification(type = NotificationType.NOT_SELECTED)
        assertEquals(ENTRANT_RESERVATIONS_ROUTE, inboxRoute(n))
    }

    @Test
    fun winConfirmationAndCancellationOpenTheDialog() {
        assertTrue(opensDetail(Notification(type = NotificationType.WIN)))
        assertTrue(opensDetail(Notification(type = NotificationType.CONFIRMATION)))
        assertTrue(opensDetail(Notification(type = NotificationType.CANCELLATION)))
        assertFalse(opensDetail(Notification(type = NotificationType.NOT_SELECTED)))
        assertFalse(opensDetail(Notification(type = NotificationType.OWNER)))
    }

    @Test
    fun otherNotificationsOpenNothing() {
        assertNull(inboxRoute(Notification(type = NotificationType.OWNER)))
        assertNull(inboxRoute(Notification(type = NotificationType.WIN)))
    }

    @Test
    fun lotteryReservationIdIsStable() {
        assertEquals("L1_e1", lotteryReservationId("L1", "e1"))
        assertEquals(lotteryReservationId("L1", "e1"), lotteryReservationId("L1", "e1"))
    }

    @Test
    fun lotteryStatusesForTheEntrant() {
        assertEquals("Pending", entrantLotteryStatus("Waiting", false))
        assertEquals("Lost", entrantLotteryStatus("Waiting", true))
        assertEquals("Won", entrantLotteryStatus("Selected", true))
        assertEquals("Confirmed", entrantLotteryStatus("Accepted", true))
        assertEquals("Declined", entrantLotteryStatus("Declined", true))
    }

    // ---- US 01.05.01 winner picks campsite and dates ----

    @Test
    fun datesBetweenIncludesBothEnds() {
        assertEquals(listOf("2027-02-27", "2027-02-28", "2027-03-01"), datesBetween("2027-02-27", "2027-03-01"))
        assertEquals(listOf("2027-06-01"), datesBetween("2027-06-01", "2027-06-01"))
        assertTrue(datesBetween("2027-06-03", "2027-06-01").isEmpty())
        assertTrue(datesBetween("2027-02-30", "2027-03-01").isEmpty())
        assertTrue(datesBetween("", "2027-03-01").isEmpty())
    }

    @Test
    fun winChoiceNeedsEverything() {
        assertEquals(
            listOf("Pick a campsite.", "Pick an arrival date.", "Pick a departure date."),
            winChoiceErrors("", "", "", "2027-06-01", "2027-06-30", emptyList())
        )
    }

    @Test
    fun winChoiceChecksOrderPeriodAndBookedDays() {
        val start = "2027-06-01"
        val end = "2027-06-30"
        assertEquals(1, winChoiceErrors("c1", "2027-06-05", "2027-06-04", start, end, emptyList()).size)
        assertEquals(listOf("Pick dates from 2027-06-01 to 2027-06-30."),
            winChoiceErrors("c1", "2027-05-31", "2027-06-02", start, end, emptyList()))
        assertEquals(listOf("Already booked: 2027-06-03."),
            winChoiceErrors("c1", "2027-06-02", "2027-06-04", start, end, listOf("2027-06-03", "2027-06-10")))
        assertTrue(winChoiceErrors("c1", "2027-06-04", "2027-06-09", start, end, listOf("2027-06-03", "2027-06-10")).isEmpty())
    }

    // ---- US 01.05.02 / US 01.07.02 CSV ----

    @Test
    fun reservationCsvHasHeaderAndValues() {
        val csv = reservationCsv("Ana", "Pine Lake", "Jasper, AB", "2027-06-01", "2027-06-03")
        val lines = csv.trimEnd().split("\r\n")
        assertEquals(2, lines.size)
        assertEquals(RESERVATION_CSV_HEADER.joinToString(","), lines[0])
        assertEquals("Ana,Pine Lake,\"Jasper, AB\",2027-06-01,2027-06-03", lines[1])
    }

    @Test
    fun cancellationCsvAddsCancellationDate() {
        val csv = cancellationCsv("Ana", "Pine Lake", "Jasper", "2027-06-01", "2027-06-03", "2027-05-20")
        val lines = csv.trimEnd().split("\r\n")
        assertEquals(CANCELLATION_CSV_HEADER.joinToString(","), lines[0])
        assertEquals("Ana,Pine Lake,Jasper,2027-06-01,2027-06-03,2027-05-20", lines[1])
    }

    @Test
    fun csvFieldEscapesQuotesAndFormulas() {
        assertEquals("\"say \"\"hi\"\"\"", csvField("say \"hi\""))
        assertEquals("'=SUM(A1)", csvField("=SUM(A1)"))
        assertEquals("plain", csvField("plain"))
        assertEquals("", csvField(""))
    }

    @Test
    fun csvFileNameIsSafe() {
        assertEquals("reservation_Pine_Lake_2027-06-01.csv", csvFileName("reservation", "Pine Lake!", "2027-06-01"))
        assertEquals("cancellation_campsite.csv", csvFileName("cancellation", "  ", ""))
    }

    // ---- US 02.10.01 owner Send Notification ----

    private fun entry(entrant: String, joined: String, status: String = "Waiting") =
        WaitlistEntry(entrantId = entrant, joinedDate = joined, status = status)

    @Test
    fun notifiableEntrantsOnlyActiveAccounts() {
        val entries = listOf(
            entry("b", "2027-01-02"),
            entry("a", "2027-01-01"),
            entry("c", "2027-01-03", status = "Removed"),
            entry("a", "2027-01-04"),
            entry("deleted", "2027-01-05"),
            entry("", "2027-01-06")
        )
        assertEquals(listOf("a", "b"), notifiableEntrants(entries, setOf("a", "b", "c", "")))
    }

    @Test
    fun chosenRecipientsAllOrSelected() {
        val waitlist = listOf("a", "b", "c")
        assertEquals(waitlist, chosenRecipients(waitlist, true, setOf("a")))
        assertEquals(listOf("b"), chosenRecipients(waitlist, false, setOf("b", "gone")))
    }

    @Test
    fun errorsListWhatIsMissing() {
        assertEquals(listOf("Select a lottery.", "Write a message."), ownerNotificationErrors("", emptyList(), " "))
        assertEquals(listOf("Choose at least one entrant."), ownerNotificationErrors("L1", emptyList(), "hi"))
        assertTrue(ownerNotificationErrors("L1", listOf("a"), "hi").isEmpty())
    }

    @Test
    fun sentConfirmationCountsEntrants() {
        assertEquals("Notification sent to 1 entrant.", sentConfirmation(1))
        assertEquals("Notification sent to 4 entrants.", sentConfirmation(4))
    }
}
