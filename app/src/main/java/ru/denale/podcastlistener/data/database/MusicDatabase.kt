package ru.denale.podcastlistener.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MusicBdEntity::class
    ],
    version = 2
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Perform the necessary SQL operations to update the schema
        database.execSQL("ALTER TABLE $MUSIC_TYPES_DATABASE_NAME ADD COLUMN authorIds TEXT")
        database.execSQL("ALTER TABLE $MUSIC_TYPES_DATABASE_NAME ADD COLUMN genreIds TEXT")
    }
}