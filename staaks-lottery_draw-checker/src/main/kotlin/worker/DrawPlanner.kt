package worker

import com.example.staaks_campsite_app.data.LotteryDraw
import com.example.staaks_campsite_app.data.model.WaitlistEntry

// holds the name of a field and the value to save
data class Change(
    val fieldName: String,
    val newValue: Any
)

// holds all the changes for one waitlist entry
data class WaitlistChange(
    val entryId: String,
    val changes: List<Change>
)

// holds the changes the repository will save and the message it will show
data class DrawPlan(
    val entryChanges: List<WaitlistChange>,
    val lotteryChanges: List<Change>,
    val message: String
)

// prepares the changes for the first draw and for replacements
class DrawPlanner {
    private val draw = LotteryDraw()

    // chooses winners for the first draw
    fun initial(
        lotteryId: String,
        entries: List<WaitlistEntry>,
        existingIds: Set<String>,
        winnerCount: Int,
        seed: Long,
        now: Long
    ): DrawPlan {
        // stops the first draw if anyone was already selected
        check(entries.none { wasSelected(it) }) {
            "The initial draw already has invitation records but drawCompleted is false"
        }

        // keeps waiting entrants who still have an account and have never been selected
        val candidates = entries.filter { entry ->
            entry.status == "Waiting" &&
                    entry.selectedAt == 0L &&
                    entry.entrantId in existingIds
        }

        // uses the seed to make the random selection repeatable
        val winners = draw.selectWinners(
            lotteryId,
            candidates,
            existingIds,
            winnerCount,
            seed
        )

        val winnerIds = mutableListOf<String>()
        val entryChanges = mutableListOf<WaitlistChange>()

        // records each winner and prepares the change to their waitlist entry
        for (winner in winners) {
            winnerIds.add(winner.entrantId)
            entryChanges.add(selectEntry(winner, now))
        }

        // saves each eligible entrant once for future replacements
        val eligibleIds = mutableSetOf<String>()
        for (candidate in candidates) {
            eligibleIds.add(candidate.entrantId)
        }

        // marks the lottery as drawn and records the results
        return DrawPlan(
            entryChanges = entryChanges,
            lotteryChanges = listOf(
                Change("status", "Drawn"),
                Change("drawCompleted", true),
                Change("drawnAt", now),
                Change("drawSeed", seed),
                Change("drawAlgorithm", "kotlin-fisher-yates-v1"),
                Change("eligibleEntrantIds", eligibleIds.sorted()),
                Change("winnerEntrantIds", winnerIds),
                Change("winnerIds", winnerIds),
                Change("previouslySelectedEntrantIds", winnerIds)
            ),
            message = "INITIAL DRAW SAVED WITH ${winners.size} WINNERS"
        )
    }

    // fills spots that became available after the first draw
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
        val activeIds = mutableSetOf<String>()

        for (entry in entries) {
            // adds anyone who already received an invitation to the history
            if (wasSelected(entry)) {
                history.add(entry.entrantId)
            }

            // accepted entrants and selected entrants with an account still hold a spot
            if (
                entry.status == "Accepted" ||
                (entry.status == "Selected" && entry.entrantId in existingIds)
            ) {
                activeIds.add(entry.entrantId)
            }
        }

        // finds declined invitations and selected entrants whose accounts are gone
        val releasedEntries = entries.filter { entry ->
            val declined = entry.status == "Declined" && entry.entrantId in history
            val entrantRemoved = entry.status == "Selected" &&
                    entry.entrantId !in existingIds

            // skips entries that were already handled or still hold a spot
            !entry.replacementProcessed &&
                    entry.entrantId !in activeIds &&
                    (declined || entrantRemoved)
        }

        // returns early when there are no released invitations to handle
        if (releasedEntries.isEmpty()) return null

        // counts one released spot per entrant even if they have multiple entries
        val releasedIds = mutableSetOf<String>()
        for (entry in releasedEntries) {
            releasedIds.add(entry.entrantId)
        }

        // limits replacements to the number of spots that are actually free
        val remaining = winnerCount - activeIds.size
        val availableSpots = if (remaining < 0) 0 else remaining
        val count = minOf(releasedIds.size, availableSpots)

        // only considers entrants from the first draw who have never been selected
        val candidates = entries.filter { entry ->
            entry.entrantId in originalPool &&
                    entry.entrantId !in history &&
                    entry.selectedAt == 0L
        }

        // chooses replacements only when there are spots to fill
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

        // prepares the new invitations and records who now holds a spot
        val entryChanges = mutableListOf<WaitlistChange>()
        for (replacement in replacements) {
            entryChanges.add(selectEntry(replacement, now))
            history.add(replacement.entrantId)
            activeIds.add(replacement.entrantId)
        }

        // pairs each released spot with a replacement if one is available
        for ((index, entrantId) in releasedIds.sorted().withIndex()) {
            val replacement = if (index < replacements.size) {
                replacements[index]
            } else {
                null
            }

            // records the same result on every released entry for this entrant
            for (entry in releasedEntries) {
                if (entry.entrantId != entrantId) continue

                val status = if (entry.status == "Declined") "Declined" else "Removed"

                entryChanges.add(
                    WaitlistChange(
                        entryId = entry.entryId,
                        changes = listOf(
                            Change("status", status),
                            Change("replacementProcessed", true),
                            Change("replacementProcessedAt", now),
                            Change("replacementEntrantId", replacement?.entrantId ?: ""),
                            Change("replacementEntryId", replacement?.entryId ?: "")
                        )
                    )
                )
            }
        }

        // saves the updated history and the entrants who now hold a spot
        return DrawPlan(
            entryChanges = entryChanges,
            lotteryChanges = listOf(
                Change("previouslySelectedEntrantIds", history.sorted()),
                Change("winnerIds", activeIds.sorted()),
                Change("lastReplacementAt", now),
                Change("lastReplacementSeed", seed)
            ),
            message = "processed ${releasedIds.size} released invitations " +
                    "and selected ${replacements.size} replacements"
        )
    }

    // prepares the fields for a new invitation
    private fun selectEntry(entry: WaitlistEntry, now: Long): WaitlistChange {
        return WaitlistChange(
            entryId = entry.entryId,
            changes = listOf(
                Change("status", "Selected"),
                Change("selectedAt", now),
                Change("replacementProcessed", false)
            )
        )
    }

    // checks the selection time and status for signs of a past invitation
    private fun wasSelected(entry: WaitlistEntry): Boolean {
        return entry.selectedAt > 0L ||
                entry.status == "Selected" ||
                entry.status == "Accepted" ||
                entry.status == "Declined"
    }
}