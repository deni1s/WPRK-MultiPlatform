package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.Banner
import io.reactivex.Single

interface BannerDataSource {
    fun getBanners():Single<List<Banner>>
}