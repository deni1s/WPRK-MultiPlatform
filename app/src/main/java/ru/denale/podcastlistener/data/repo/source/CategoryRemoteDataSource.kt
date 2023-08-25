package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class CategoryRemoteDataSource(val apiService: ApiService):CategoryDataSource {
    override fun getCategories(offset: Int, limit: Int): Single<List<Genre>> = apiService.getCategories(offset, limit)
    override fun getCategories(): Single<List<Genre>> = apiService.getCategories()
}