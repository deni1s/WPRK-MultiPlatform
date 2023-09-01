package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.repo.source.CategoryDataSource
import io.reactivex.Single
import ru.denale.podcastlistener.data.GenreResponse

class CategoryRepositoryImpl(val categoryDataSource: CategoryDataSource) : CategoryRepository {
    override fun getCategories(offset: Int, limit: Int): Single<GenreResponse> =
        categoryDataSource.getCategories(offset, limit)

    override fun getCategories(): Single<GenreResponse> = categoryDataSource.getCategories()
}