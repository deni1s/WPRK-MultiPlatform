package ru.denale.podcastlistener.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = MUSIC_TYPES_DATABASE_NAME)
data class MusicBdEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val author: String,
    val authorId: String,
    val genreId: String,
    val durationString: String?,
    val mediaUrl: String,
    val imageUrl: String?,
    @ColumnInfo(name = MUSIC_LIST_TYPE)
    val type: String,
    val screenTitle: String,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val timestamp: Long
)