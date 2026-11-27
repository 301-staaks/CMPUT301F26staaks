package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Lottery
import com.example.staaks_campsite_app.data.model.WaitlistEntry
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.staaks_campsite_app.data.model.LotteryTime

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

    var lotteriesLoading by mutableStateOf(true)
        private set

    var lotteryError by mutableStateOf<String?>(null)
        private set


    /**
     * saves a lottery in firestore
     *
     * @param lottery the lottery we want to save
     */

    /**
     * creates a lottery or updates its editable settings before closing
     */
    fun saveLottery(lottery: Lottery) = run {
        if (lottery.lotteryId.isBlank()) {
            lottery.lotteryId = lotteriesRef.document().id
        }

        val reference = lotteriesRef.document(lottery.lotteryId)

        db.runTransaction { transaction ->
            val existing = transaction.get(reference)
            val currentTime = System.currentTimeMillis()

            require(lottery.ownerId.isNotBlank()) {
                "Owner ID is missing"
            }

            require(lottery.numberOfWinners > 0) {
                "The number of winners must be greater than zero"
            }

            require(lottery.registrationClosesAt > currentTime) {
                "Registration must close in the future"
            }

            require(
                lottery.registrationStartDate.isNotBlank() &&
                        lottery.registrationStartDate <=
                        lottery.registrationEndDate &&
                        lottery.registrationEndDate <
                        lottery.bookingStartDate &&
                        lottery.bookingStartDate <=
                        lottery.bookingEndDate
            ) {
                "Check the registration and booking dates"
            }

            val capacity = lottery.waitlistCapacity

            require(capacity == null || capacity > 0) {
                "Waitlist capacity must be greater than zero"
            }

            // prevents reducing capacity below the current participant count
            if (
                existing.exists() &&
                existing.getLong("membershipVersion") == 1L
            ) {
                val count = existing.getLong("waitlistCount")
                    ?: error("The participant count is missing")

                require(capacity == null || capacity.toLong() >= count) {
                    "Capacity cannot be lower than the current participant count"
                }
            }

            val campsiteIds = (
                    lottery.campsiteIds + lottery.campsiteId
                    ).filter { it.isNotBlank() }.distinct()

            require(campsiteIds.isNotEmpty() && campsiteIds.size <= 50) {
                "Select between 1 and 50 campsites"
            }

            // verifies the selected campsites before writing
            for (id in campsiteIds) {
                val campsite = transaction.get(
                    db.collection("campsites").document(id)
                )

                check(campsite.exists()) {
                    "A selected campsite no longer exists"
                }

                check(campsite.getString("ownerId") == lottery.ownerId) {
                    "A selected campsite belongs to another owner"
                }
            }

            if (existing.exists()) {
                check(existing.getString("ownerId") == lottery.ownerId) {
                    "This lottery belongs to another owner"
                }

                check(
                    existing.getString("status") == "Open" &&
                            existing.getBoolean("drawCompleted") != true
                ) {
                    "A drawn or closed lottery cannot be edited"
                }

                val previousDeadline =
                    existing.getLong("registrationClosesAt") ?: 0L

                check(previousDeadline > currentTime) {
                    "The saved registration deadline is missing or has passed"
                }
            }

            val fields = mapOf<String, Any?>(
                "lotteryId" to lottery.lotteryId,
                "ownerId" to lottery.ownerId,
                "campsiteId" to campsiteIds.first(),
                "campsiteIds" to campsiteIds,
                "registrationStartDate" to lottery.registrationStartDate,
                "registrationEndDate" to lottery.registrationEndDate,
                "bookingStartDate" to lottery.bookingStartDate,
                "bookingEndDate" to lottery.bookingEndDate,
                "numberOfWinners" to lottery.numberOfWinners,
                "waitlistCapacity" to capacity,
                "registrationClosesAt" to lottery.registrationClosesAt
            )

            if (existing.exists()) {
                // updates settings while preserving draw results and history
                transaction.update(reference, fields)
            } else {
                // initializes fields only when creating a lottery
                transaction.set(
                    reference,
                    fields + mapOf(
                        "status" to "Open",
                        "drawCompleted" to false,
                        "drawnAt" to 0L,
                        "winnerIds" to emptyList<String>(),
                        "waitlistCount" to 0L,
                        "membershipVersion" to 1L
                    )
                )
            }

            null
        }
    }

    /**
     * finds a lottery in the loaded list
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
            if (lottery.includesCampsite(campsiteId)) {
                result.add(lottery)
            }
        }

        return result
    }

    fun hasEntrantAlreadyJoined(lotteryId: String, entrantId: String): Boolean {
        for (entry in _waitlistEntries) {
            if (entry.lotteryId == lotteryId &&
                entry.entrantId == entrantId
            ) {
                return true
            }
        }
        return false
    }

    /**
     * adds an entrant to a lottery waitlist. Checks that the entrant is eligible to join the lottery
     * waitlist (i.e. checks lottery exists, valid entrant id, registration is open, has already
     * joined the waitlist, and lottery is not full).
     *
     * @param lotteryId the id of the lottery
     * @param entrantId the id of the entrant joining
     * @param joinedDate the date they joined written as YYYY-MM-DD
     */

    fun addEntrantToWaitlist(
        lotteryId: String,
        entrantId: String,
        joinedDate: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (
            lotteryId.isBlank() ||
            entrantId.isBlank() ||
            "/" in lotteryId ||
            "/" in entrantId ||
            lotteryId in setOf(".", "..") ||
            entrantId in setOf(".", "..")
        ) {
            onFailure("The lottery or entrant ID is invalid")
            return
        }

        val lotteryReference = lotteriesRef.document(lotteryId)

        // uses a consistent entry ID for each lottery and entrant pair
        val entryReference =
            waitlistRef.document("${lotteryId}_${entrantId}")

        db.runTransaction { transaction ->
            val lottery = transaction.get(lotteryReference)
            val existingEntry = transaction.get(entryReference)
            val entrant = transaction.get(
                db.collection("users").document(entrantId)
            )

            check(lottery.exists()) {
                "Lottery not found"
            }

            check(
                entrant.exists() &&
                        entrant.getString("type") == "ENTRANT"
            ) {
                "An entrant profile is required"
            }

            check(
                lottery.getString("status") == "Open" &&
                        lottery.getBoolean("drawCompleted") != true
            ) {
                "Registration is closed"
            }

            val now = System.currentTimeMillis()
            val today = LotteryTime.today(now)

            val deadline =
                lottery.getLong("registrationClosesAt") ?: 0L

            check(deadline > now) {
                "The registration deadline has passed or is missing"
            }

            val starts =
                lottery.getString("registrationStartDate") ?: ""

            val ends =
                lottery.getString("registrationEndDate") ?: ""

            check(
                starts.isNotBlank() &&
                        ends.isNotBlank() &&
                        today >= starts &&
                        today <= ends
            ) {
                "Registration is not open today"
            }

            check(!existingEntry.exists()) {
                "You have already joined this lottery"
            }

            // older lotteries need their existing entries counted before migration
            check(lottery.getLong("membershipVersion") == 1L) {
                "Registration is unavailable for this older lottery"
            }

            val count = lottery.getLong("waitlistCount")

            check(count != null && count >= 0L) {
                "The participant count is invalid"
            }

            val capacity = lottery.getLong("waitlistCapacity")

            check(capacity == null || count < capacity) {
                "This lottery is full"
            }

            val additionalIds =
                (lottery.get("campsiteIds") as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()

            val campsiteIds = (
                    additionalIds +
                            listOfNotNull(lottery.getString("campsiteId"))
                    ).filter { it.isNotBlank() }.distinct()

            check(campsiteIds.isNotEmpty() && campsiteIds.size <= 50) {
                "The lottery has invalid campsite information"
            }

            for (id in campsiteIds) {
                val campsite = transaction.get(
                    db.collection("campsites").document(id)
                )

                check(campsite.exists()) {
                    "A campsite is no longer available"
                }
            }

            // all reads finish before these writes
            val entry = WaitlistEntry(
                entryId = entryReference.id,
                lotteryId = lotteryId,
                entrantId = entrantId,
                joinedDate = today,
                status = "Waiting",
                selectedAt = 0L,
                replacementProcessed = false
            )

            transaction.set(entryReference, entry)

            transaction.update(
                lotteryReference,
                "waitlistCount",
                count + 1L
            )
            null
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure(
                    task.exception?.cause?.localizedMessage
                        ?: task.exception?.localizedMessage
                        ?: "Could not join the lottery"
                )
            }
        }
    }

    /**
     * removes an unselected entry before registration closes
     */
    fun removeEntrantFromWaitlist(
        lotteryId: String,
        entrantId: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) = db.runTransaction { transaction ->
        val lotteryReference = lotteriesRef.document(lotteryId)

        val entryReference =
            waitlistRef.document("${lotteryId}_${entrantId}")

        val lottery = transaction.get(lotteryReference)
        val entry = transaction.get(entryReference)

        check(lottery.exists()) {
            "Lottery not found"
        }

        if (!entry.exists()) {
            return@runTransaction null
        }

        check(entry.getString("entrantId") == entrantId) {
            "This entry belongs to another entrant"
        }

        check(
            lottery.getString("status") == "Open" &&
                    lottery.getBoolean("drawCompleted") != true &&
                    (lottery.getLong("registrationClosesAt") ?: 0L) >
                    System.currentTimeMillis()
        ) {
            "Registration is closed"
        }

        check(
            entry.getString("status") == "Waiting" &&
                    (entry.getLong("selectedAt") ?: 0L) == 0L
        ) {
            "A selected invitation cannot be removed as a waitlist entry"
        }

        check(lottery.getLong("membershipVersion") == 1L) {
            "This older lottery needs participant migration"
        }
        val count = lottery.getLong("waitlistCount")

        check(count != null && count > 0L) {
            "The participant count is invalid"
        }

        transaction.delete(entryReference)

        transaction.update(
            lotteryReference,
            "waitlistCount",
            count - 1L
        )

        null
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onSuccess()
        } else {
            onFailure(
                task.exception?.cause?.localizedMessage
                    ?: task.exception?.localizedMessage
                    ?: "Could not join the lottery"
            )
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
                lotteryError = error.localizedMessage
                    ?: "Could not load lotteries"
                lotteriesLoading = false
                return@addSnapshotListener
            }

            if (snapshot == null) {
                lotteryError = "Could not load lotteries"
                lotteriesLoading = false
                return@addSnapshotListener
            }

            lotteryError = null

            _lotteries.clear()

            snapshot?.documents?.forEach { document ->
                val lottery = document.toObject(Lottery::class.java)

                if (lottery != null) {
                    lottery.lotteryId = document.id
                    _lotteries.add(lottery)
                }
            }
            lotteriesLoading = false
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