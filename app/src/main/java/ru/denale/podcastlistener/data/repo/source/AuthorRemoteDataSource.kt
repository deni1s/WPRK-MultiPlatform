package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single
import ru.denale.podcastlistener.data.AuthorResponse

class AuthorRemoteDataSource(val apiService: ApiService):AuthorDataSource {
    override fun getAuthors(offset: Int, limit: Int): Single<AuthorResponse> = apiService.getAuthors(offset, limit)
    override fun getAuthors(): Single<AuthorResponse> = apiService.getAuthors()
}