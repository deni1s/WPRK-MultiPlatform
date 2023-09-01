package ru.denale.podcastlistener.data.repo

import io.reactivex.Single
import ru.denale.podcastlistener.data.BannerResponse

interface BannerRepository {
    fun getBanners(gravity: String): Single<BannerResponse>
}