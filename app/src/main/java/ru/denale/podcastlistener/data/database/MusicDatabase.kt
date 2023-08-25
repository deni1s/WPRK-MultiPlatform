package ru.denale.podcastlistener.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MusicBdEntity::class
    ],
    version = 1
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao
}