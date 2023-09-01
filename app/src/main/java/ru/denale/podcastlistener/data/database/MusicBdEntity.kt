package ru.denale.podcastlistener.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = MUSIC_TYPES_DATABASE_NAME)
data class MusicBdEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "createdAt")
    val createdAt: String,
    @ColumnInfo(name = "author")
    val author: String,
    @ColumnInfo(name = "authorId")
    val authorId: String,
    @ColumnInfo(name = "genreId")
    val genreId: String,
    @ColumnInfo(name = "durationString")
    val durationString: String?,
    @ColumnInfo(name = "mediaUrl")
    val mediaUrl: String,
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String?,
    @ColumnInfo(name = MUSIC_LIST_TYPE)
    val type: String,
    @ColumnInfo(name = "screenTitle")
    val screenTitle: String,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER, name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "warning")
    val warning: String?,
)