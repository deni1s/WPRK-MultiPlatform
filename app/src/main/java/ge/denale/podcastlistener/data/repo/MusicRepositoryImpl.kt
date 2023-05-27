package ge.denale.podcastlistener.data.repo

import ge.denale.podcastlistener.data.Music
import ge.denale.podcastlistener.data.repo.source.MusicDataSource
import io.reactivex.Completable
import io.reactivex.Single

class MusicRepositoryImpl(val musicDataSource: MusicDataSource) : MusicRepository {
    override fun getMusic(podcastId: String): Single<Music> =
        musicDataSource.getMusics(podcastId)

    override fun getMusics(category_id: String?, offset: Int): Single<List<Music>> =
        musicDataSource.getMusics(category_id, offset)

    override fun getMusicsByAuthor(author_id: String?, offset: Int): Single<List<Music>> =
        musicDataSource.getMusicsByAuthor(author_id, offset)

    override fun setTrackListened(genreId: String, authorId: String) =
        musicDataSource.setTrackListened(genreId, authorId)

    override fun markTrackSeen(podcastId: String) = musicDataSource.markTrackSeen(podcastId)
}