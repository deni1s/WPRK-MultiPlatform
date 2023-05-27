package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.Music
import ge.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class MusicRemoteDataSource(private val apiService: ApiService) : MusicDataSource {

    override fun getMusics(podcastId: String): Single<Music> =
        apiService.getMusicsInfo(podcastId)

    override fun getMusics(category_id: String?, offset: Int): Single<List<Music>> =
        apiService.getMusics(category_id, offset)

    override fun getMusicsByAuthor(author_id: String?, offset: Int): Single<List<Music>> =
        apiService.getMusicsByAuthor(author_id, offset)

    override fun setTrackListened(genreId: String, authorId: String) =
        apiService.markTrackListened(genreId, authorId)

    override fun markTrackSeen(podcastId: String) =
        apiService.markTrackSeen(podcastId)
}