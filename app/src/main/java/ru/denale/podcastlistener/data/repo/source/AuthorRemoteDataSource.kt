package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class AuthorRemoteDataSource(val apiService: ApiService):AuthorDataSource {
    override fun getAuthors(offset: Int, limit: Int): Single<List<Author>> = apiService.getAuthors(offset, limit)
    override fun getAuthors(): Single<List<Author>> = apiService.getAuthors()
}