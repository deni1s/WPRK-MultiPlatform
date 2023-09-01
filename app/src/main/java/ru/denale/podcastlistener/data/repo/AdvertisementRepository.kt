package ru.denale.podcastlistener.data.repo

interface AdvertisementRepository {
    fun isAdvertisementAllowed(): Boolean
    fun setAdvertisementAllowed(isAllowed: Boolean)
    fun increaseEnterance()
}