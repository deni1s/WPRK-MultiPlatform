package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.Banner
import io.reactivex.Single

interface BannerRepository {
    fun getBanners(gravity: String): Single<List<Banner>>
}