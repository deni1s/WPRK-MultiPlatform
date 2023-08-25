package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Banner
import io.reactivex.Single

interface BannerDataSource {
    fun getBanners(gravity: String):Single<List<Banner>>
}