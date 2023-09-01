package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.repo.source.AuthorDataSource
import io.reactivex.Single
import ru.denale.podcastlistener.data.AuthorResponse

class AuthorRepositoryImpl(val dataSource: AuthorDataSource) : AuthorRepository {
    override fun getAuthors(offset: Int, limit: Int): Single<AuthorResponse> =
        dataSource.getAuthors(offset, limit)

    override fun getAuthors(): Single<AuthorResponse> = dataSource.getAuthors()
}