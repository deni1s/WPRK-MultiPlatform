package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Author
import io.reactivex.Single

interface AuthorDataSource {
    fun getAuthors(offset: Int, limit: Int): Single<List<Author>>
    fun getAuthors(): Single<List<Author>>
}