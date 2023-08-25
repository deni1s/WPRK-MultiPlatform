package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.Genre
import io.reactivex.Single

interface CategoryRepository {
    fun getCategories(offset: Int, limit: Int): Single<List<Genre>>
    fun getCategories(): Single<List<Genre>>
}