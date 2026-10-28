package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Lottery
import com.example.staaks_campsite_app.data.model.WaitlistEntry
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * saves lotteries and waiting list entries in firestore
 *
 * still needed: stop users from joining the same waiting list twice
 *
 * @property lotteries the lotteries loaded from firestore
 * @property waitlistEntries the waiting list entries loaded from firestore
 */

class LotteryRepository {
    private val db = Firebase.firestore
    private val lotteriesRef = db.collection("lotteries")
    private val waitlistRef = db.collection("waitlist")

    private val _lotteries = mutableStateListOf<Lottery>()
    private val _waitlistEntries = mutableStateListOf<WaitlistEntry>()

    val lotteries: List<Lottery>
        get() = _lotteries

    val waitlistEntries: List<WaitlistEntry>
        get() = _waitlistEntries

    /**
     * saves a lottery in firestore
     *
     * @param lottery the lottery we want to save
     */

    fun saveLottery(lottery: Lottery) {
        if (lottery.lotteryId.isBlank()) {
            lottery.lotteryId = lotteriesRef.document().id
        }

        lotteriesRef.document(lottery.lotteryId).set(lottery)
    }

    /**
     * looks for a lottery in the loaded list
     *
     * @param lotteryId the id of the lottery we want to find
     * @return the lottery or null if it is not in the list
     */
    fun getLottery(lotteryId: String): Lottery? {
        for (lottery in _lotteries) {
            if (lottery.lotteryId == lotteryId) {
                return lottery
            }
        }

        return null
    }

    /**
     * finds the loaded lotteries for a campsite
     *
     * @param campsiteId the id of the campsite
     * @return its lotteries or an empty list if none are loaded
     */
    fun getLotteriesForCampsite(campsiteId: String): List<Lottery> {
        val result = mutableListOf<Lottery>()

        for (lottery in _lotteries) {
            if (lottery.campsiteId == campsiteId) {
                result.add(lottery)
            }
        }

        return result
    }

    /**
     * adds an entrant to a lottery waitlist
     *
     * @param lotteryId the id of the lottery
     * @param entrantId the id of the entrant joining
     * @param joinedDate the date they joined written as YYYY-MM-DD
     */

    fun addEntrantToWaitlist(
        lotteryId: String,
        entrantId: String,
        joinedDate: String
    ) {
        val entryReference = waitlistRef.document()

        val entry = WaitlistEntry(
            entryId = entryReference.id,
            lotteryId = lotteryId,
            entrantId = entrantId,
            joinedDate = joinedDate
        )

        entryReference.set(entry)
    }

    /**
     * removes the entrant loaded entries from a lottery waitlist
     *
     * @param lotteryId the id of the lottery
     * @param entrantId the id of the entrant leaving
     */

    fun removeEntrantFromWaitlist(
        lotteryId: String,
        entrantId: String
    ) {
        for (entry in _waitlistEntries) {
            if (entry.lotteryId == lotteryId &&
                entry.entrantId == entrantId
            ) {
                waitlistRef.document(entry.entryId).delete()
            }
        }
    }

    /**
     * finds the loaded waitlist entries for a lottery
     *
     * @param lotteryId the id of the lottery
     * @return its waiting list entries or an empty list if none are loaded
     */

    fun getWaitlistEntries(lotteryId: String): List<WaitlistEntry> {
        val result = mutableListOf<WaitlistEntry>()

        for (entry in _waitlistEntries) {
            if (entry.lotteryId == lotteryId) {
                result.add(entry)
            }
        }

        return result
    }

    init {
        lotteriesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _lotteries.clear()

            snapshot?.documents?.forEach { document ->
                val lottery = document.toObject(Lottery::class.java)

                if (lottery != null) {
                    lottery.lotteryId = document.id
                    _lotteries.add(lottery)
                }
            }
        }

        waitlistRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _waitlistEntries.clear()

            snapshot?.documents?.forEach { document ->
                val entry = document.toObject(WaitlistEntry::class.java)

                if (entry != null) {
                    entry.entryId = document.id
                    _waitlistEntries.add(entry)
                }
            }
        }
    }
}