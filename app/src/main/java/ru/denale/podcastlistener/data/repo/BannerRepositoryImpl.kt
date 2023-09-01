package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.repo.source.BannerDataSource
import io.reactivex.Single
import ru.denale.podcastlistener.data.BannerResponse

class BannerRepositoryImpl(val bannerDataSource: BannerDataSource) : BannerRepository {
    override fun getBanners(gravity: String): Single<BannerResponse> = bannerDataSource.getBanners(gravity)
}