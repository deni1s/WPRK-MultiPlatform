package ru.denale.podcastlistener.data.repo

import io.reactivex.Single
import ru.denale.podcastlistener.data.GenreResponse

interface CategoryRepository {
    fun getCategories(offset: Int, limit: Int): Single<GenreResponse>
    fun getCategories(): Single<GenreResponse>
}