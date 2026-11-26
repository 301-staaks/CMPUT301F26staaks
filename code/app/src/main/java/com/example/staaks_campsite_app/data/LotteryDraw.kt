package com.example.staaks_campsite_app.data

import com.example.staaks_campsite_app.data.model.WaitlistEntry
import kotlin.random.Random

/**
 * selects initial winners and replacement entrants.
 *
 * this class does not read or write firestore and does not change
 * the supplied entries. returned entries reference the original objects.
 */
class LotteryDraw {

    /**
     * selects eligible entrants without giving duplicate users extra chances.
     *
     * selects everyone eligible when fewer entrants exist than requested.
     * an empty eligible list produces an empty result.
     *
     * repeating a draw requires the same seed, participant data and
     * kotlin random implementation to reproduce the result.
     *
     * @param lotteryId the lottery being drawn
     * @param entries the waitlist entries to examine
     * @param existingEntrantIds the ids of profiles that still exist
     * @param numberOfWinners the positive number of winners requested
     * @param seed the seed used to shuffle eligible participants
     * @return the selected entries without duplicate entrant ids
     * @throws IllegalArgumentException if the winner count is not positive
     */
    fun selectWinners(
        lotteryId: String,
        entries: List<WaitlistEntry>,
        existingEntrantIds: Set<String>,
        numberOfWinners: Int,
        seed: Long
    ): List<WaitlistEntry> {

        require(numberOfWinners > 0) {
            "The number of winners must be greater than zero."
        }

        val eligibleEntries = mutableListOf<WaitlistEntry>()
        val addedEntrants = mutableSetOf<String>()

        // starts from a stable order when using saved, unique entry ids
        val orderedEntries = entries.sortedBy { it.entryId }

        for (entry in orderedEntries) {

            // ignores entries belonging to other lotteries
            if (entry.lotteryId != lotteryId) {
                continue
            }

            if (entry.entrantId.isBlank()) {
                continue
            }

            // excludes entrants whose profiles no longer exist
            if (!existingEntrantIds.contains(entry.entrantId)) {
                continue
            }

            if (entry.status != "Waiting") {
                continue
            }

            // excludes entries that already received an invitation
            if (entry.selectedAt > 0L) {
                continue
            }

            // add returns false when the entrant is already in the set
            if (addedEntrants.add(entry.entrantId)) {
                eligibleEntries.add(entry)
            }
        }

        val random = Random(seed)

        // fisher-yates shuffles the list by swapping each position
        // with a randomly chosen position from the remaining prefix
        for (index in eligibleEntries.lastIndex downTo 1) {
            val randomIndex = random.nextInt(index + 1)

            val temporary = eligibleEntries[index]
            eligibleEntries[index] = eligibleEntries[randomIndex]
            eligibleEntries[randomIndex] = temporary
        }

        val selectedEntries = mutableListOf<WaitlistEntry>()
        var winnerCount = numberOfWinners

        // cannot select more entrants than the eligible list contains
        if (winnerCount > eligibleEntries.size) {
            winnerCount = eligibleEntries.size
        }

        for (index in 0 until winnerCount) {
            selectedEntries.add(eligibleEntries[index])
        }

        return selectedEntries
    }

    /**
     * selects one replacement from previously unselected entrants.
     *
     * checks all supplied entries for this lottery to prevent a duplicate
     * waiting entry from giving a previously selected user another chance.
     *
     * this method only selects a candidate. the caller must verify that
     * an invitation needs replacement and save the result separately.
     *
     * @param lotteryId the lottery needing a replacement
     * @param entries the lottery participation records to examine
     * @param existingEntrantIds the ids of profiles that still exist
     * @param seed the seed used for replacement selection
     * @return the selected entry, or null if nobody is eligible
     */
    fun selectReplacement(
        lotteryId: String,
        entries: List<WaitlistEntry>,
        existingEntrantIds: Set<String>,
        seed: Long
    ): WaitlistEntry? {

        val previouslySelected = mutableSetOf<String>()

        for (entry in entries) {
            if (entry.lotteryId != lotteryId) {
                continue
            }

            // records selected users across all their participation entries
            if (
                entry.selectedAt > 0L ||
                entry.status == "Selected" ||
                entry.status == "Accepted" ||
                entry.status == "Declined"
            ) {
                previouslySelected.add(entry.entrantId)
            }
        }

        val remainingEntries = mutableListOf<WaitlistEntry>()

        for (entry in entries) {
            if (!previouslySelected.contains(entry.entrantId)) {
                remainingEntries.add(entry)
            }
        }

        // reuses the eligibility checks and random selection
        val selected = selectWinners(
            lotteryId = lotteryId,
            entries = remainingEntries,
            existingEntrantIds = existingEntrantIds,
            numberOfWinners = 1,
            seed = seed
        )

        if (selected.isEmpty()) {
            return null
        }

        return selected[0]
    }
}