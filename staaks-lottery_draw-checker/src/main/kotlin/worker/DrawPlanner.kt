package worker

import com.example.staaks_campsite_app.data.LotteryDraw
import com.example.staaks_campsite_app.data.model.WaitlistEntry

/**
 * describes the changes that one transaction will save
 */
data class DrawPlan(
    val entryChanges: Map<String, Map<String, Any>>,
    val lotteryChanges: Map<String, Any>,
    val message: String
)

/**
 * prepares results without reading or writing firestore
 */
class DrawPlanner {
    private val draw = LotteryDraw()

    fun initial(
        lotteryId: String,
        entries: List<WaitlistEntry>,
        existingIds: Set<String>,
        winnerCount: Int,
        seed: Long,
        now: Long
    ): DrawPlan {
        // prevents overwriting invitations from an earlier draw
        check(entries.none { wasSelected(it) }) {
            "The initial draw already has invitation records but drawCompleted is false"
        }

        val candidates = entries.filter {
            it.status == "Waiting" &&
                it.selectedAt == 0L &&
                it.entrantId in existingIds
        }

        val winners = draw.selectWinners(
            lotteryId,
            candidates,
            existingIds,
            winnerCount,
            seed
        )

        val winnerIds = winners.map { it.entrantId }
        val changes = winners.associate {
            it.entryId to selectedFields(now)
        }

        return DrawPlan(
            entryChanges = changes,
            lotteryChanges = mapOf(
                "status" to "Drawn",
                "drawCompleted" to true,
                "drawnAt" to now,
                "drawSeed" to seed,
                "drawAlgorithm" to "kotlin-fisher-yates-v1",
                "eligibleEntrantIds" to candidates
                    .map { it.entrantId }
                    .distinct()
                    .sorted(),
                "winnerEntrantIds" to winnerIds,
                "winnerIds" to winnerIds,
                "previouslySelectedEntrantIds" to winnerIds
            ),
            message = "initial draw saved with ${winners.size} winners"
        )
    }

    fun replacements(
        lotteryId: String,
        entries: List<WaitlistEntry>,
        existingIds: Set<String>,
        originalPool: Set<String>,
        savedHistory: Set<String>,
        winnerCount: Int,
        seed: Long,
        now: Long
    ): DrawPlan? {
        val history = savedHistory.toMutableSet()

        entries.filter { wasSelected(it) }.forEach {
            history.add(it.entrantId)
        }

        // accepted reservations remain under the reservation system
        val active = entries.filter {
            it.status == "Accepted" ||
                (it.status == "Selected" && it.entrantId in existingIds)
        }.map { it.entrantId }.toSet()

        // groups duplicates so one entrant releases only one place
        val released = entries.filter {
            !it.replacementProcessed &&
                it.entrantId !in active &&
                (
                    (it.status == "Declined" && it.entrantId in history) ||
                    (it.status == "Selected" && it.entrantId !in existingIds)
                )
        }.groupBy { it.entrantId }.toSortedMap()

        if (released.isEmpty()) return null

        val count = minOf(
            released.size,
            (winnerCount - active.size).coerceAtLeast(0)
        )

        val candidates = entries.filter {
            it.entrantId in originalPool &&
                it.entrantId !in history &&
                it.selectedAt == 0L
        }

        val replacements = if (count == 0) {
            emptyList()
        } else {
            draw.selectWinners(
                lotteryId,
                candidates,
                existingIds,
                count,
                seed
            )
        }

        val changes = mutableMapOf<String, Map<String, Any>>()

        for (entry in replacements) {
            changes[entry.entryId] = selectedFields(now)
            history.add(entry.entrantId)
        }

        for ((index, releasedEntries) in released.values.withIndex()) {
            val replacement = replacements.getOrNull(index)

            for (entry in releasedEntries) {
                changes[entry.entryId] = mapOf(
                    "status" to if (entry.status == "Declined") {
                        "Declined"
                    } else {
                        "Removed"
                    },
                    "replacementProcessed" to true,
                    "replacementProcessedAt" to now,
                    "replacementEntrantId" to
                        (replacement?.entrantId ?: ""),
                    "replacementEntryId" to
                        (replacement?.entryId ?: "")
                )
            }
        }

        return DrawPlan(
            entryChanges = changes,
            lotteryChanges = mapOf(
                "previouslySelectedEntrantIds" to history.sorted(),
                "winnerIds" to (
                    active + replacements.map { it.entrantId }
                ).sorted(),
                "lastReplacementAt" to now,
                "lastReplacementSeed" to seed
            ),
            message =
                "processed ${released.size} released invitations " +
                "and selected ${replacements.size} replacements"
        )
    }

    private fun selectedFields(now: Long): Map<String, Any> = mapOf(
        "status" to "Selected",
        "selectedAt" to now,
        "replacementProcessed" to false
    )

    private fun wasSelected(entry: WaitlistEntry): Boolean =
        entry.selectedAt > 0L ||
            entry.status in setOf("Selected", "Accepted", "Declined")
}