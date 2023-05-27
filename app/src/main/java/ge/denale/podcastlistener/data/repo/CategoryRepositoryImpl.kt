package ge.denale.podcastlistener.data.repo

import ge.denale.podcastlistener.data.Genre
import ge.denale.podcastlistener.data.repo.source.CategoryDataSource
import io.reactivex.Single

class CategoryRepositoryImpl(val categoryDataSource: CategoryDataSource) : CategoryRepository {
    override fun getCategories(offset: Int, limit: Int): Single<List<Genre>> =
        categoryDataSource.getCategories(offset, limit)

    override fun getCategories(): Single<List<Genre>> = categoryDataSource.getCategories()
}