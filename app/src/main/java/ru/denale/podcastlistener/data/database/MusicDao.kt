package ru.denale.podcastlistener.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Single

@Dao
interface MusicDao {

    @Insert
    fun insert(musicList: List<MusicBdEntity>)

    @Query("SELECT * FROM $MUSIC_TYPES_DATABASE_NAME WHERE $MUSIC_LIST_TYPE = :type")
    fun getMusic(type: String): Single<List<MusicBdEntity>>

    @Query("DELETE FROM $MUSIC_TYPES_DATABASE_NAME WHERE $MUSIC_LIST_TYPE = :type")
    fun clearWave(type: String)
}