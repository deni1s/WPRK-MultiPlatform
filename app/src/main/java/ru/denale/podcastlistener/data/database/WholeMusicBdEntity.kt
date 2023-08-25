package ru.denale.podcastlistener.data.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.denale.podcastlistener.data.Music

const val MUSIC_TYPES_DATABASE_NAME = "musicTypesHistoryTable"
const val MUSIC_LIST_TYPE = "type"

data class WholeMusicBdEntity(
    val type: String,
    val title: String,
    val time: Long,
    val list: List<Music>
)