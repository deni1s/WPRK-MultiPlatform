package ge.denale.podcastlistener.data.repo

import ge.denale.podcastlistener.data.Author
import io.reactivex.Single

interface AuthorRepository {
    fun getAuthors(offset: Int, limit: Int):Single<List<Author>>
    fun getAuthors():Single<List<Author>>
}