package ge.denale.podcastlistener.data.repo

import ge.denale.podcastlistener.data.Banner
import io.reactivex.Single

interface BannerRepository {
    fun getBanners(): Single<List<Banner>>
}