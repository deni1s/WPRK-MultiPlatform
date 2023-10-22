package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse
import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class MusicRemoteDataSource(private val apiService: ApiService) : MusicDataSource {

    override fun getMusics(podcastId: String): Single<Music> =
        apiService.getMusicsInfo(podcastId)

    override fun getMusics(category_id: String?, offset: Int): Single<WaveResponse> =
        apiService.getMusics(category_id, offset)

    override fun getMusicsByAuthor(author_id: String?, offset: Int): Single<WaveResponse> =
        apiService.getMusicsByAuthor(author_id, offset)

    override fun setTrackListened(genreIds: List<String>?, authorIds: List<String>?) =
        apiService.markTrackListened(genreIds, authorIds)

    override fun markTrackSeen(podcastId: String) =
        apiService.markTrackSeen(podcastId)
}