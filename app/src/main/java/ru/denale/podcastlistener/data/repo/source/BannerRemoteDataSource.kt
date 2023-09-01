package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single
import ru.denale.podcastlistener.data.BannerResponse

class BannerRemoteDataSource(val apiService: ApiService):BannerDataSource {
    override fun getBanners(gravity: String): Single<BannerResponse> = apiService.getBanners(gravity)
}