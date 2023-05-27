package ge.denale.podcastlistener.data.repo

import ge.denale.podcastlistener.data.Banner
import ge.denale.podcastlistener.data.repo.source.BannerDataSource
import io.reactivex.Single

class BannerRepositoryImpl(val bannerDataSource: BannerDataSource) : BannerRepository {
    override fun getBanners(): Single<List<Banner>> = bannerDataSource.getBanners()
}