package ru.denale.podcastlistener.data

data class WaveResponse(
    val podcasts: List<Music>,
    val title: String?,
    val type: String
)