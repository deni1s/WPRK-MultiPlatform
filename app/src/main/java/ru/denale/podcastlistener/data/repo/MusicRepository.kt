package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.LastSessionData
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse
import ru.denale.podcastlistener.data.database.WholeMusicBdEntity
import io.reactivex.Completable
import io.reactivex.Single

interface MusicRepository {
    fun getMusic(podcastId: String): Single<Music>
    fun getMusics(category_id: String?, offset: Int): Single<WaveResponse>
    fun getPreviousMusics(type: String): Single<WholeMusicBdEntity?>
    fun clearType(type: String)
    fun saveMusicList(response: WaveResponse, timestamp: Long)
    fun getMusicsByAuthor(author_id: String?, offset: Int): Single<WaveResponse>
    fun setTrackListened(genreId: List<String>?, authorId: List<String>?): Completable
    fun markTrackSeen(podcastId: String): Completable

    fun saveLastSessionData(type: String, id: String, currentPosition: Int?)
    fun getLastSessionData(type: String): LastSessionData
    fun clearSessionData(type: String)
}