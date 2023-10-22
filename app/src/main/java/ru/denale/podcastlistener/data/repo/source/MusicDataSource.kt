package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse
import io.reactivex.Completable
import io.reactivex.Single

interface MusicDataSource {
    fun getMusics(podcastId: String): Single<Music>
    fun getMusics(category_id: String?, offset: Int): Single<WaveResponse>
    fun getMusicsByAuthor(author_id: String?, offset: Int): Single<WaveResponse>
    fun setTrackListened(genreIds: List<String>?, authorIds: List<String>?): Completable
    fun markTrackSeen(podcastId: String): Completable
}