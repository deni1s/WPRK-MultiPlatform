package ru.denale.podcastlistener.feature.activities.playmusic

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import io.appmetrica.analytics.AppMetrica
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.repo.MusicRepository
import java.util.Timer
import java.util.TimerTask

class MusicPlayerService2 : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val musicRepository: MusicRepository by inject()

    private var timer: Timer? = null
    private var currentDuration: Long? = null
    private val compositeDisposable = CompositeDisposable()
    private var currentMusic: Music? = null
    private var currentType: String? = null
    private var markListenedSent: Boolean = false
    private var listenedDuration: Long = 0

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
        player.addListener(object : Player.Listener {

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                val music = mediaItem?.mediaMetadata?.extras?.getParcelable<Music>("mediaItem")
                currentType = mediaItem?.mediaMetadata?.extras?.getString("type")
                currentMusic = music
                markListenedSent = false
                listenedDuration = 0L
                markTrackSeen(music?.id)
                AppMetrica.reportEvent(
                    "PlayerScreen",
                    mapOf(
                        "result" to "success",
                        "type" to currentType,
                        "authorId" to music?.authorIds?.first().orEmpty(),
                        "podcastId" to music?.id.orEmpty()
                    )
                )
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    if (player.duration != currentDuration && player.duration > 0 && currentMusic != null) {
                        currentDuration = player.duration
                        setTimer(player.duration, currentMusic)
                    } else {
                        setTimer(player.duration, currentMusic)
                    }
                } else {
                    timer?.cancel()
                    if (timer != null && currentType?.contains("wave") == true) {
                        musicRepository.saveLastSessionData(
                            currentType!!,
                            currentMusic?.id.orEmpty(),
                            player.currentPosition.toInt()
                        )
                    }
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    if (player.duration != currentDuration && player.duration > 0 && currentMusic != null) {
                        currentDuration = player.duration
                    }
                }

                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) || events.contains(Player.EVENT_TRACKS_CHANGED)) {
                    if (player.duration != currentDuration && player.duration > 0 && currentMusic != null) {
                        currentDuration = player.duration
                    }
                }
            }
        })
    }

    private fun setTimer(duration: Long, music: Music?) {
        if (!markListenedSent) {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    listenedDuration += 1000
                    if (listenedDuration > duration * 0.7) {
                        markListenedSent = true
                        setTrackListened(music?.genreIds, music?.authorIds)
                        timer?.cancel()
                    }
                }

            }, 1000, 1000)
        }
    }

    private fun setTrackListened(genreIds: List<String>?, authorIds: List<String>?) {
        compositeDisposable.add(
            musicRepository.setTrackListened(genreIds, authorIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete().subscribe({}, {})
        )
    }

    private fun markTrackSeen(podcastId: String?) {
        podcastId?.let {
            compositeDisposable.add(
                musicRepository.markTrackSeen(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorComplete().subscribe({}, {})
            )
        }
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        compositeDisposable.dispose()
        timer?.cancel()
        timer = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession
}