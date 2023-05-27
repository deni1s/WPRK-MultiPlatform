package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.Author
import ge.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class AuthorRemoteDataSource(val apiService: ApiService):AuthorDataSource {
    override fun getAuthors(offset: Int, limit: Int): Single<List<Author>> = apiService.getAuthors(offset, limit)
    override fun getAuthors(): Single<List<Author>> = apiService.getAuthors()
}