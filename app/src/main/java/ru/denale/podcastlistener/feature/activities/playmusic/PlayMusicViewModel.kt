package ru.denale.podcastlistener.feature.activities.playmusic

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import ru.denale.podcastlistener.LastSessionData
import ru.denale.podcastlistener.common.*
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse
import ru.denale.podcastlistener.data.repo.MusicRepository
import ru.denale.podcastlistener.feature.home.ENTERANCE_COUNT
import ru.denale.podcastlistener.feature.home.FIRST_ADD_ENTERANCE_COUNT
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.denale.podcastlistener.data.repo.AdvertisementRepository
import java.util.*

private const val HOURS_IN_DAY = 24
private const val HOUR_OF_UPDATE = 4
private const val SINGLE_PODCAST_TITLE = "Подкаст"

class PlayMusicViewModel(
    bundle: Bundle?,
    private val musicRepository: MusicRepository,
    private val advertisementRepository: AdvertisementRepository,
    val sharedPreferences: SharedPreferences
) :
    MusicPlayerOnlineViewModel() {

    fun setTrackListened(genreId: String, authorId: String) {
        compositeDisposable.add(
            musicRepository.setTrackListened(genreId, authorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete().subscribe({
                    Log.d("listenre", "setTrackListened: success")
                }, {
                    Log.d("listenre", "setTrackListened: failure")
                })
        )
    }

    fun markTrackSeen(podcastId: String) {
        compositeDisposable.add(
            musicRepository.markTrackSeen(podcastId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete().subscribe({}, {})
        )
    }

//    val musicLiveData = MutableLiveData<List<Music>>()
//
//    init {
//        val podcast = bundle.getParcelable<Music>(EXTRA_KEY_DATA)!!
//        musicLiveData.value = listOf(podcast)
//    }

    val musicLiveData = MutableLiveData<List<Music>>()
    val sessionLiveData = MutableLiveData<LastSessionData>()
    val typeLiveData = MutableLiveData<String>()
    val titleLiveData = MutableLiveData<String>()
    val isAdvertisementAvailableData = MutableLiveData<Boolean>()

    init {
        // progressLiveData.value = true

        val type = bundle?.getString(EXTRA_MUSIC_TYPE)
        val music = bundle?.getParcelable<Music>(EXTRA_MUSIC)

        var enterCount = sharedPreferences.getInt(ENTERANCE_COUNT, 0)
        val isAdvAllowed = isAdvertisementAllowed()
        if (enterCount > FIRST_ADD_ENTERANCE_COUNT && isAdvAllowed) {
            isAdvertisementAvailableData.value = true
        } else {
            enterCount++
            sharedPreferences.edit().putInt(ENTERANCE_COUNT, enterCount).apply()
            isAdvertisementAvailableData.value = false
        }

        music?.let {
            musicLiveData.value = listOf(it)
            titleLiveData.value = SINGLE_PODCAST_TITLE
        } ?: type?.let { getTypeData(type) }
    }

    fun isAdvertisementAllowed(): Boolean {
        return advertisementRepository.isAdvertisementAllowed()
    }

    fun getTypeData(id: String) {
        musicRepository.getPreviousMusics(id).flatMap {
            if (shouldUpdateWave(it.time)) {
                musicRepository.clearSessionData(id)
                musicRepository.clearType(id)
                musicRepository.getMusics(id, 0)
                    .doOnSuccess {
                        musicRepository.saveMusicList(
                            it,
                            Calendar.getInstance().timeInMillis
                        )
                    }
            } else {
                Single.just(WaveResponse(podcasts = it.list, title = it.title, type = it.type, warning = null))
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                // progressLiveData.value = false
            }
            .subscribe(object : MusicPlayerSignleObserver<WaveResponse>(compositeDisposable) {
                override fun onSuccess(t: WaveResponse) {
                    sessionLiveData.value = musicRepository.getLastSessionData(id)
                    musicLiveData.value = t.podcasts
                    titleLiveData.value = t.title ?: "Волна"
//                    sharedPreferences.edit().putLong(type, Calendar.getInstance().timeInMillis)
//                        .apply()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })
    }

    private fun shouldUpdateWave(millis: Long): Boolean {
        val now = Calendar.getInstance()
        val savedDate = Calendar.getInstance()
        savedDate.timeInMillis = millis

        val differenceMillis = Math.abs(now.timeInMillis - savedDate.timeInMillis)
        val differenceHours = differenceMillis / (60 * 60 * 1000)

        // сейчас 3 часа ночи, волна была создана в 2 ночи
        // сейчас 5 утра, волна создана в 3 ночи
        return if (now.get(Calendar.DAY_OF_YEAR) == savedDate.get(Calendar.DAY_OF_YEAR)) {
            savedDate.get(Calendar.HOUR_OF_DAY) < HOUR_OF_UPDATE && now.get(Calendar.HOUR_OF_DAY) >= HOUR_OF_UPDATE
            // сейчас 3 ночи, волна создана в 15
            // сейчас 5 утра, волна создана в 15
            // сейчас 2 ночи, волна в 3 ночи прошлого дня
        } else if (differenceHours < HOURS_IN_DAY) {
            now.get(Calendar.HOUR_OF_DAY) >= HOUR_OF_UPDATE || savedDate.get(Calendar.HOUR_OF_DAY) < HOUR_OF_UPDATE
        } else {
            true
        }
    }

    fun loadPodcastId(podcastId: String) {
        musicRepository.getMusic(podcastId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                // progressLiveData.value = false
            }
            .subscribe(object : MusicPlayerSignleObserver<Music>(compositeDisposable) {
                override fun onSuccess(t: Music) {
                    titleLiveData.value = SINGLE_PODCAST_TITLE
                    musicLiveData.value = listOf(t)
                }
            })
    }
}