package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Banner
import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class BannerRemoteDataSource(val apiService: ApiService):BannerDataSource {
    override fun getBanners(gravity: String): Single<List<Banner>> = apiService.getBanners(gravity)
}