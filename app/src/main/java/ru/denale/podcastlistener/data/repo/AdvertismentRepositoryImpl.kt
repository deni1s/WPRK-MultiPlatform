package ru.denale.podcastlistener.data.repo

import android.content.SharedPreferences

const val IS_ADVERTISEMENT_ALLOWED_KEY = "is_advertisement_allowed"
const val ENTRANCE_COUNT_KEY = "enterance_count"
const val AD_FREE_ENTRANCES_COUNT = 4

class AdvertismentRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : AdvertisementRepository {

    private var enteranceCount: Int? = null

    override fun isAdvertisementAllowed(): Boolean {
        val isTooMuchFree = (enteranceCount ?: getEntrancesCount()) > AD_FREE_ENTRANCES_COUNT
        return sharedPreferences.getBoolean(IS_ADVERTISEMENT_ALLOWED_KEY, true) && isTooMuchFree
    }

    override fun setAdvertisementAllowed(isAllowed: Boolean) {
        sharedPreferences.edit().putBoolean(IS_ADVERTISEMENT_ALLOWED_KEY, isAllowed).apply()
    }

    override fun increaseEnterance() {
        getEntrancesCount().let {
            sharedPreferences.edit().putInt(ENTRANCE_COUNT_KEY, it + 1).apply()
        }
    }

    private fun getEntrancesCount(): Int {
        return sharedPreferences.getInt(ENTRANCE_COUNT_KEY, 0)
    }
}