package ru.denale.podcastlistener.data.repo.source

import io.reactivex.Single
import ru.denale.podcastlistener.data.GenreResponse

interface CategoryDataSource {
    fun getCategories(offset: Int, limit: Int): Single<GenreResponse>
    fun getCategories(): Single<GenreResponse>
}