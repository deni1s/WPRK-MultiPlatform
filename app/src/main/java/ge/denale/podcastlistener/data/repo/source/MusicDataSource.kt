package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.Music
import io.reactivex.Completable
import io.reactivex.Single

interface MusicDataSource {
    fun getMusics(podcastId: String): Single<Music>
    fun getMusics(category_id: String?, offset: Int): Single<List<Music>>
    fun getMusicsByAuthor(author_id: String?, offset: Int): Single<List<Music>>
    fun setTrackListened(genreId: String, authorId: String): Completable
    fun markTrackSeen(podcastId: String): Completable
}