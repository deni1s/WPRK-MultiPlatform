package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.Banner
import ru.denale.podcastlistener.data.repo.source.BannerDataSource
import io.reactivex.Single

class BannerRepositoryImpl(val bannerDataSource: BannerDataSource) : BannerRepository {
    override fun getBanners(gravity: String): Single<List<Banner>> = bannerDataSource.getBanners(gravity)
}