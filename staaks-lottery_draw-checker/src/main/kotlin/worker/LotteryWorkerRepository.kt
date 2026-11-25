package worker

import com.example.staaks_campsite_app.data.model.WaitlistEntry
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import java.security.SecureRandom
import java.time.Instant
import java.time.ZoneId

// reads lottery data and saves the draw results
class LotteryWorkerRepository(private val db: Firestore) {
    private val planner = DrawPlanner()
    private val random = SecureRandom()

    // shows the lottery settings and the number of waitlist entries
    fun check(lotteryId: String) {
        val lottery = db.collection("lotteries")
            .document(lotteryId)
            .get()
            .get()

        check(lottery.exists()) {
            "Lottery $lotteryId was not found"
        }

        println("Lottery: ${lottery.id}")
        println("status: ${lottery.getString("status")}")
        println("drawCompleted: ${lottery.getBoolean("drawCompleted")}")
        println("registrationClosesAt: ${lottery.getLong("registrationClosesAt")}")
        println("numberOfWinners: ${lottery.getLong("numberOfWinners")}")

        val entries = db.collection("waitlist")
            .whereEqualTo("lotteryId", lotteryId)
            .get()
            .get()

        println("waitlist records: ${entries.size()}")
        println("Check complete - no documents were changed")
    }

    // runs the first draw or fills spots that became available
    fun process(lotteryId: String): String? {
        // keeps the same seed if firestore retries the transaction
        val seed = random.nextLong()

        return db.runTransaction<String?> { transaction ->
            val lotteryRef = db.collection("lotteries").document(lotteryId)
            val lottery = transaction.get(lotteryRef).get()

            if (!lottery.exists()) {
                return@runTransaction null
            }

            val status = lottery.getString("status")
            val drawCompleted = lottery.getBoolean("drawCompleted") == true
            val needsFirstDraw = status == "Open" && !drawCompleted
            val canCheckReplacements =
                drawCompleted && (status == "Drawn" || status == "Ongoing")

            if (!needsFirstDraw && !canCheckReplacements) {
                return@runTransaction null
            }

            // skips lotteries whose booking period has ended
            val now = System.currentTimeMillis()
            val today = Instant.ofEpochMilli(now)
                .atZone(ZoneId.of("America/Edmonton"))
                .toLocalDate()
                .toString()

            val bookingEnd = lottery.getString("bookingEndDate") ?: ""
            if (bookingEnd.isNotBlank() && today > bookingEnd) {
                return@runTransaction null
            }

            // waits for the exact registration deadline before the first draw
            if (needsFirstDraw) {
                val closesAt = lottery.getLong("registrationClosesAt") ?: 0L

                check(closesAt > 0L) {
                    "Set registrationClosesAt to epoch milliseconds first"
                }

                if (now < closesAt) {
                    return@runTransaction null
                }
            }

            val winnerCount = lottery.getLong("numberOfWinners")
            require(winnerCount != null && winnerCount in 1L..Int.MAX_VALUE.toLong()) {
                "numberOfWinners must be a positive integer"
            }

            // checks that every campsite in the lottery still exists
            val campsiteId = lottery.getString("campsiteId") ?: ""
            require(isValidId(campsiteId)) {
                "Set campsiteId to a valid campsite document id"
            }

            val campsiteIds = readIds(lottery, "campsiteIds") + campsiteId
            check(campsiteIds.size <= 50) {
                "This version supports up to 50 campsites per lottery"
            }

            for (id in campsiteIds) {
                check(isValidId(id)) {
                    "Invalid campsite id"
                }

                val campsiteRef = db.collection("campsites").document(id)
                val campsite = transaction.get(campsiteRef).get()

                check(campsite.exists()) {
                    "A campsite no longer exists"
                }
            }

            // loads only the waitlist entries for this lottery
            val waitlistQuery = db.collection("waitlist")
                .whereEqualTo("lotteryId", lotteryId)

            val documents = transaction.get(waitlistQuery).get()
                .documents
                .sortedBy { it.id }

            check(documents.size <= 200) {
                "This worker supports up to 200 waitlist records per lottery"
            }

            // reads the entry data and collects each entrant id once
            val entries = mutableListOf<WaitlistEntry>()
            val entrantIds = mutableSetOf<String>()

            for (document in documents) {
                val selectedAt = document.getLong("selectedAt") ?: 0L
                require(selectedAt >= 0L) {
                    "selectedAt cannot be negative"
                }

                val entrantId = document.getString("entrantId") ?: ""
                entrantIds.add(entrantId)

                entries.add(
                    WaitlistEntry(
                        entryId = document.id,
                        lotteryId = lotteryId,
                        entrantId = entrantId,
                        joinedDate = document.getString("joinedDate") ?: "",
                        status = document.getString("status") ?: "Waiting",
                        selectedAt = selectedAt,
                        replacementProcessed =
                            document.getBoolean("replacementProcessed") == true
                    )
                )
            }

            // checks which entrants still have a user document
            val existingIds = mutableSetOf<String>()
            for (entrantId in entrantIds) {
                if (!isValidId(entrantId)) continue

                val userRef = db.collection("users").document(entrantId)
                val user = transaction.get(userRef).get()

                if (user.exists()) {
                    existingIds.add(entrantId)
                }
            }

            // asks the planner which fields need to change
            val plan = if (needsFirstDraw) {
                planner.initial(
                    lotteryId = lotteryId,
                    entries = entries,
                    existingIds = existingIds,
                    winnerCount = winnerCount.toInt(),
                    seed = seed,
                    now = now
                )
            } else {
                check(lottery.contains("eligibleEntrantIds")) {
                    "This lottery has no saved original pool and cannot be replaced safely"
                }

                val originalPool = readIds(lottery, "eligibleEntrantIds")
                val history = readIds(lottery, "previouslySelectedEntrantIds") +
                        readIds(lottery, "winnerEntrantIds")

                planner.replacements(
                    lotteryId = lotteryId,
                    entries = entries,
                    existingIds = existingIds,
                    originalPool = originalPool,
                    savedHistory = history,
                    winnerCount = winnerCount.toInt(),
                    seed = seed,
                    now = now
                )
            }

            if (plan == null) {
                return@runTransaction null
            }

            // applies the plan after all firestore reads are done
            for (entryChange in plan.entryChanges) {
                val document = documents.first { it.id == entryChange.entryId }
                saveChanges(transaction, document.reference, entryChange.changes)
            }

            saveChanges(transaction, lotteryRef, plan.lotteryChanges)

            plan.message
        }.get()
    }

    // checks every lottery that may need a draw or a replacement
    fun processAll() {
        val lotteries = db.collection("lotteries")
            .whereIn("status", listOf("Open", "Drawn", "Ongoing"))
            .get()
            .get()
            .documents

        for (lottery in lotteries) {
            try {
                println("Lottery: ${lottery.id}")
                println("status: ${lottery.getString("status")}")
                println("drawCompleted: ${lottery.getBoolean("drawCompleted")}")
                println("registrationClosesAt: ${lottery.getLong("registrationClosesAt")}")
                println("numberOfWinners: ${lottery.getLong("numberOfWinners")}")

                val message = process(lottery.id)
                if (message != null) {
                    println("\u001B[32m${lottery.id}: $message\u001B[0m")
                }
            } catch (error: Exception) {
                System.err.println("Lottery ${lottery.id} failed: ${error.message}")
            }
        }
    }

    // sends all the field changes for one document in one update
    private fun saveChanges(
        transaction: Transaction,
        reference: DocumentReference,
        changes: List<Change>
    ) {
        if (changes.isEmpty()) return

        val first = changes[0]
        val otherFieldsAndValues = mutableListOf<Any>()

        for (index in 1 until changes.size) {
            val change = changes[index]
            otherFieldsAndValues.add(change.fieldName)
            otherFieldsAndValues.add(change.newValue)
        }

        transaction.update(
            reference,
            first.fieldName,
            first.newValue,
            *otherFieldsAndValues.toTypedArray()
        )
    }

    // Reads IDs from a Firestore field and keeps each string only once.
    private fun readIds(document: DocumentSnapshot, field: String): Set<String> {
        // document.get(field) reads the value stored under the given field name
        // as? List<*> checks whether that value is a list of any element type
        //ff the field is missing or the value is not a list return an empty set
        val values = document.get(field) as? List<*> ?: return emptySet()

        // keep only the values that are strings, ignoring numbers, nulls, and other types
        val ids = values.filterIsInstance<String>()

        // convert the list to a set so each ID appears only once
        return ids.toSet()
    }

    // checks that the id can be used as a document name
    private fun isValidId(id: String): Boolean {
        return id.isNotBlank() && "/" !in id && id != "." && id != ".."
    }
}