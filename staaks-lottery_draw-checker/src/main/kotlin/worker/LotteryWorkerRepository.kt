package worker

import com.example.staaks_campsite_app.data.model.WaitlistEntry
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import java.security.SecureRandom
import java.time.Instant
import java.time.ZoneId

/**
 * reads fresh data and saves each draw in one transaction
 */
class LotteryWorkerRepository(private val db: Firestore) {
    private val planner = DrawPlanner()
    private val random = SecureRandom()

    /**
     * displays the configuration without changing documents
     */
    fun check(lotteryId: String) {
        val document = db.collection("lotteries")
            .document(lotteryId)
            .get()
            .get()

        check(document.exists()) {
            "Lottery $lotteryId was not found"
        }

        println("Lottery: ${document.id}")
        println("status: ${document.getString("status")}")
        println("drawCompleted: ${document.getBoolean("drawCompleted")}")
        println("registrationClosesAt: ${document.getLong("registrationClosesAt")}")
        println("numberOfWinners: ${document.getLong("numberOfWinners")}")

        val entries = db.collection("waitlist")
            .whereEqualTo("lotteryId", lotteryId)
            .get()
            .get()

        println("waitlist records: ${entries.size()}")
        println("Check complete - no documents were changed")
    }

    /**
     * processes an initial draw or pending replacements
     */
    fun process(lotteryId: String): String? {
        // keeps the same seed if firestore retries the transaction
        val seed = random.nextLong()

        return db.runTransaction<String?> { transaction ->
            val reference = db.collection("lotteries")
                .document(lotteryId)

            val lottery = transaction.get(reference).get()

            if (!lottery.exists()) {
                return@runTransaction null
            }

            val completed = lottery.getBoolean("drawCompleted") == true
            val status = lottery.getString("status")

            val initial = status == "Open" && !completed
            val replacement =
                status in setOf("Drawn", "Ongoing") && completed

            if (!initial && !replacement) {
                return@runTransaction null
            }

            val now = System.currentTimeMillis()
            val today = Instant.ofEpochMilli(now)
                .atZone(ZoneId.of("America/Edmonton"))
                .toLocalDate()
                .toString()

            val bookingEnd =
                lottery.getString("bookingEndDate") ?: ""

            if (bookingEnd.isNotBlank() && today > bookingEnd) {
                return@runTransaction null
            }

            if (initial) {
                val closesAt =
                    lottery.getLong("registrationClosesAt") ?: 0L

                check(closesAt > 0L) {
                    "Set registrationClosesAt to epoch milliseconds first"
                }

                if (now < closesAt) {
                    return@runTransaction null
                }
            }

            val count = lottery.getLong("numberOfWinners")

            require(
                count != null &&
                    count in 1L..Int.MAX_VALUE.toLong()
            ) {
                "numberOfWinners must be a positive integer"
            }

            val campsiteId =
                lottery.getString("campsiteId") ?: ""

            require(
                campsiteId.isNotBlank() &&
                    "/" !in campsiteId &&
                    campsiteId !in setOf(".", "..")
            ) {
                "Set campsiteId to a valid campsite document id"
            }

            val campsiteIds =
                readIds(lottery, "campsiteIds") + campsiteId

            check(campsiteIds.size <= 50) {
                "This version supports up to 50 campsites per lottery"
            }

            for (id in campsiteIds) {
                check(
                    id.isNotBlank() &&
                        "/" !in id &&
                        id !in setOf(".", "..")
                ) {
                    "Invalid campsite id"
                }

                val campsite = transaction.get(
                    db.collection("campsites").document(id)
                ).get()

                check(campsite.exists()) {
                    "A campsite no longer exists"
                }
            }

            val query = db.collection("waitlist")
                .whereEqualTo("lotteryId", lotteryId)

            val documents = transaction.get(query)
                .get()
                .documents
                .sortedBy { it.id }

            // keeps this first version within small project limits
            check(documents.size <= 200) {
                "This worker supports up to 200 waitlist records per lottery"
            }

            val entries = documents.map { document ->
                val selectedAt =
                    document.getLong("selectedAt") ?: 0L

                require(selectedAt >= 0L) {
                    "selectedAt cannot be negative"
                }

                WaitlistEntry(
                    entryId = document.id,
                    lotteryId = lotteryId,
                    entrantId =
                        document.getString("entrantId") ?: "",
                    joinedDate =
                        document.getString("joinedDate") ?: "",
                    status =
                        document.getString("status") ?: "Waiting",
                    selectedAt = selectedAt,
                    replacementProcessed =
                        document.getBoolean("replacementProcessed") == true
                )
            }

            val existing = mutableSetOf<String>()

            for (id in entries.map { it.entrantId }.distinct()) {
                if (
                    id.isBlank() ||
                    "/" in id ||
                    id in setOf(".", "..")
                ) {
                    continue
                }

                val user = transaction.get(
                    db.collection("users").document(id)
                ).get()

                if (user.exists()) {
                    existing.add(id)
                }
            }

            val plan = if (initial) {
                planner.initial(
                    lotteryId,
                    entries,
                    existing,
                    count.toInt(),
                    seed,
                    now
                )
            } else {
                check(lottery.contains("eligibleEntrantIds")) {
                    "This lottery has no saved original pool and cannot be replaced safely"
                }

                val history =
                    readIds(lottery, "previouslySelectedEntrantIds") +
                        readIds(lottery, "winnerEntrantIds")

                planner.replacements(
                    lotteryId,
                    entries,
                    existing,
                    readIds(lottery, "eligibleEntrantIds"),
                    history,
                    count.toInt(),
                    seed,
                    now
                )
            }

            if (plan == null) {
                return@runTransaction null
            }

            // all reads finish before the first write
            val references = documents.associate {
                it.id to it.reference
            }

            for ((id, fields) in plan.entryChanges) {
                transaction.update(
                    references.getValue(id),
                    fields
                )
            }

            transaction.update(reference, plan.lotteryChanges)

            plan.message
        }.get()
    }

    /**
     * checks open lotteries and lotteries with possible replacements
     */
    fun processAll() {
        val lotteries = db.collection("lotteries")
            .whereIn("status", listOf("Open", "Drawn", "Ongoing"))
            .get()
            .get()
            .documents

        for (document in lotteries) {
            try {
                val message = process(document.id)

                if (message != null) {
                    println("${document.id}: $message")
                }
            } catch (error: Exception) {
                System.err.println(
                    "Lottery ${document.id} failed: ${error.message}"
                )
            }
        }
    }

    private fun readIds(
        document: DocumentSnapshot,
        field: String
    ): Set<String> {
        val values =
            document.get(field) as? List<*> ?: return emptySet()

        return values.filterIsInstance<String>().toSet()
    }
}