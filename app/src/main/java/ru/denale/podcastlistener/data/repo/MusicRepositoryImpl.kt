package ru.denale.podcastlistener.data.repo

import android.content.SharedPreferences
import ru.denale.podcastlistener.LastSessionData
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse
import ru.denale.podcastlistener.data.database.MusicDao
import ru.denale.podcastlistener.data.database.WholeMusicBdEntity
import ru.denale.podcastlistener.data.database.toDomainModel
import ru.denale.podcastlistener.data.database.toStorageModel
import ru.denale.podcastlistener.data.repo.source.MusicDataSource
import io.reactivex.Completable
import io.reactivex.Single

class MusicRepositoryImpl(
    private val musicDataSource: MusicDataSource,
    private val dataBase: MusicDao,
    private val sharedPreferences: SharedPreferences
) : MusicRepository {
    override fun getMusic(podcastId: String): Single<Music> =
        musicDataSource.getMusics(podcastId)

    override fun getMusics(category_id: String?, offset: Int): Single<WaveResponse> =
        musicDataSource.getMusics(category_id, offset)

    override fun getPreviousMusics(type: String): Single<WholeMusicBdEntity?> {
        return dataBase.getMusic(type).map {
            if (it.isNotEmpty()) {
                WholeMusicBdEntity(
                    type = it.first().type,
                    time = it.first().timestamp,
                    list = it.toDomainModel(),
                    title = it.firstOrNull()?.screenTitle.orEmpty()
                )
            } else {
                WholeMusicBdEntity(
                    type = type,
                    list = it.toDomainModel(),
                    time = 0,
                    title = it.firstOrNull()?.screenTitle.orEmpty()
                )
            }
        }
    }

    override fun clearType(type: String) {
        dataBase.clearWave(type)
    }

    override fun saveMusicList(response: WaveResponse, timestamp: Long) {
        dataBase.insert(response.toStorageModel(response.type, timestamp))
    }

    override fun getMusicsByAuthor(author_id: String?, offset: Int): Single<WaveResponse> =
        musicDataSource.getMusicsByAuthor(author_id, offset)

    override fun setTrackListened(genreId: String, authorId: String) =
        musicDataSource.setTrackListened(genreId, authorId)

    override fun markTrackSeen(podcastId: String) = musicDataSource.markTrackSeen(podcastId)

    override fun saveLastSessionData(type: String, id: String, currentPosition: Int?) {
        sharedPreferences.edit().putString("podcastId$type", id).apply()
        if (currentPosition != null) {
            sharedPreferences.edit().putInt("podcastTime$type", currentPosition).apply()
        } else {
            sharedPreferences.edit().remove("podcastTime$type").apply()
        }
    }

    override fun getLastSessionData(type: String): LastSessionData {
        return LastSessionData(
            type = type,
            podcastId = sharedPreferences.getString("podcastId$type", null),
            sharedPreferences.getInt("podcastTime$type", 0)
        )
    }

    override fun clearSessionData(type: String) {
        sharedPreferences.edit().remove("podcastId$type").apply()
        sharedPreferences.edit().remove("podcastTime$type").apply()
    }
}