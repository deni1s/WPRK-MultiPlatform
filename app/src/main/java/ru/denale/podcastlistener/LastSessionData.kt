package ru.denale.podcastlistener

data class LastSessionData(
    val type: String,
    val podcastId: String?,
    val progress: Int?
)
