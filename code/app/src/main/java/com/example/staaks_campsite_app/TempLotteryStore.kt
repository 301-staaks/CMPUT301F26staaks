package com.example.staaks_campsite_app

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.Lottery

/**
 * TEMPORARY in-memory lottery storage so the owner screens can be tested before the
 * back end is ready.
 */
object TempLotteryStore {
    private val _lotteries = mutableStateListOf<Lottery>()
    private var nextId = 1

    val lotteries: List<Lottery>
        get() = _lotteries

    /**
     * creates the lottery if it is new (blank id), otherwise replaces the saved one
     * with the same id
     */
    fun save(lottery: Lottery) {
        if (lottery.lotteryId.isBlank()) {
            lottery.lotteryId = "temp_lottery_${nextId++}"
            _lotteries.add(lottery)
            return
        }

        val index = _lotteries.indexOfFirst { it.lotteryId == lottery.lotteryId }
        if (index >= 0) {
            _lotteries[index] = lottery
        } else {
            _lotteries.add(lottery)
        }
    }

    /** the lottery with this id, or null if there is none */
    fun get(lotteryId: String): Lottery? =
        _lotteries.find { it.lotteryId == lotteryId }

    /** the lotteries created by this owner */
    fun getForOwner(ownerId: String): List<Lottery> =
        _lotteries.filter { it.ownerId == ownerId }
}
