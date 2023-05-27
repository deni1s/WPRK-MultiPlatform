package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.Genre
import io.reactivex.Single

interface CategoryDataSource {
    fun getCategories(offset: Int, limit: Int): Single<List<Genre>>
    fun getCategories(): Single<List<Genre>>
}