package ru.denale.podcastlistener.data.repo

import io.reactivex.Single
import ru.denale.podcastlistener.data.AuthorResponse

interface AuthorRepository {
    fun getAuthors(offset: Int, limit: Int): Single<AuthorResponse>
    fun getAuthors(): Single<AuthorResponse>
}