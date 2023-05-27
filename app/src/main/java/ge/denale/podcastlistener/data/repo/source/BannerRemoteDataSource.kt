package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.Banner
import ge.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class BannerRemoteDataSource(val apiService: ApiService):BannerDataSource {
    override fun getBanners(): Single<List<Banner>> = apiService.getBanners()
}