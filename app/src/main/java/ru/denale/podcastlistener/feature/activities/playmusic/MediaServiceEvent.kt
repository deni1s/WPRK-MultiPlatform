package ru.denale.podcastlistener.feature.activities.playmusic

import android.os.Parcelable
import ru.denale.podcastlistener.data.Music
import kotlinx.android.parcel.Parcelize

sealed class MediaServiceEvent {
    data class onMusicStarted(val duration: Long) : MediaServiceEvent()

    data class onMusicLoading(
        val music: Music,
        val isLast: Boolean,
        val isFirst: Boolean
    ) : MediaServiceEvent()

    data class onMusicDisplay(
        val music: Music,
        val isLast: Boolean,
        val isFirst: Boolean
    ) : MediaServiceEvent()

    object onMusicFinished : MediaServiceEvent()

    object onMusicPaused : MediaServiceEvent()

    data class onMusicContinued(
        val duration: Int,
        val position: Int,
    ) : MediaServiceEvent()

    object onMusicFailed : MediaServiceEvent()

    data class onDataUpdate(
        val duration: Int,
        val position: Int,
        val music: Music,
        val isLast: Boolean,
        val isFirst: Boolean,
        val isMusicInProgress: Boolean,
        val type: InitializationType?
    ) : MediaServiceEvent()
}