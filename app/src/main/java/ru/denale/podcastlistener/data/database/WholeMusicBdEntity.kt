package ru.denale.podcastlistener.data.database

import ru.denale.podcastlistener.data.Music

const val MUSIC_TYPES_DATABASE_NAME = "musicTypesHistoryTable"
const val MUSIC_LIST_TYPE = "type"

data class WholeMusicBdEntity(
    val type: String,
    val title: String,
    val time: Long,
    val list: List<Music>
)