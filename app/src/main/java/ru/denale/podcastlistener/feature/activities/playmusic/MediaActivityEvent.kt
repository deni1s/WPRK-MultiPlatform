package ru.denale.podcastlistener.feature.activities.playmusic

import ru.denale.podcastlistener.data.Music

sealed class MediaActivityEvent  {

    object onPlayRequired : MediaActivityEvent()

    data class onNewInitializationRequired(
        val list: List<Music>,
        val isAdvertisementAvailable: Boolean,
        val type: InitializationType?,
        val podcastId: String?,
        val progress: Int?
    ) : MediaActivityEvent()

    object onPauseRequeired : MediaActivityEvent()

    object onNextRequeired : MediaActivityEvent()

    object onPreviousRequeired : MediaActivityEvent()

    data class onMusicRequeired(val music: Music) : MediaActivityEvent()

//    @Parcelize
//    data class onAdvertisementInfoAvailable(val isAdvertisementAvailable: Boolean): MediaActivityEvent()

    data class onMusicSeeked(val progress: Int) : MediaActivityEvent()

//    @Parcelize
//    data class onMusicFetched(val list: List<Music>): MediaActivityEvent()

    object onNewDataRequired : MediaActivityEvent()
}