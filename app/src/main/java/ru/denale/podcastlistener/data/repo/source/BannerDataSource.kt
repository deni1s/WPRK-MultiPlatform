package ru.denale.podcastlistener.data.repo.source

import io.reactivex.Single
import ru.denale.podcastlistener.data.BannerResponse

interface BannerDataSource {
    fun getBanners(gravity: String):Single<BannerResponse>
}