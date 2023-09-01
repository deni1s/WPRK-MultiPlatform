package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single
import ru.denale.podcastlistener.data.GenreResponse

class CategoryRemoteDataSource(val apiService: ApiService):CategoryDataSource {
    override fun getCategories(offset: Int, limit: Int): Single<GenreResponse> = apiService.getCategories(offset, limit)
    override fun getCategories(): Single<GenreResponse> = apiService.getCategories()
}