package ru.denale.podcastlistener

data class LastSessionData(
    val waveId: String,
    val podcastId: String?,
    val progress: Int?
)
