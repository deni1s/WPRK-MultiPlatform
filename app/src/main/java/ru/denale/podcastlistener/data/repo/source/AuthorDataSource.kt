package ru.denale.podcastlistener.data.repo.source

import io.reactivex.Single
import ru.denale.podcastlistener.data.AuthorResponse

interface AuthorDataSource {
    fun getAuthors(offset: Int, limit: Int): Single<AuthorResponse>
    fun getAuthors(): Single<AuthorResponse>
}