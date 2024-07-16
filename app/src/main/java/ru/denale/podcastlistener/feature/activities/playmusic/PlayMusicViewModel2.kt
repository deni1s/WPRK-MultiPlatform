package ru.denale.podcastlistener.feature.activities.playmusic

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import io.appmetrica.analytics.AppMetrica
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.denale.podcastlistener.LastSessionData
import ru.denale.podcastlistener.common.EXTRA_MUSIC
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ru.denale.podcastlistener.common.MusicPlayerOnlineViewModel
import ru.denale.podcastlistener.common.MusicPlayerSignleObserver
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.WaveResponse
import ru.denale.podcastlistener.data.repo.AdvertisementRepository
import ru.denale.podcastlistener.data.repo.MusicRepository
import ru.denale.podcastlistener.services.PODCAST_ID_KEY
import java.util.Calendar

private const val HOURS_IN_DAY = 24
private const val HOUR_OF_UPDATE = 4
private const val SINGLE_PODCAST_TITLE = "Подкаст"
class PlayMusicViewModel2(
    bundle: Bundle?,
    private val musicRepository: MusicRepository,
    private val advertisementRepository: AdvertisementRepository
) :
    MusicPlayerOnlineViewModel() {

    val musicLiveData = MutableLiveData<MusicState>()
    val titleLiveData = MutableLiveData<String>()
    val isAdvertisementAvailableData = MutableLiveData<Boolean>()

    init {
        val type = bundle?.getString(EXTRA_MUSIC_TYPE)
        val music = bundle?.getParcelable<Music>(EXTRA_MUSIC)
        val podcastId = bundle?.getString(PODCAST_ID_KEY)

        isAdvertisementAvailableData.value = isAdvertisementAllowed()

        music?.let {
            musicLiveData.value = MusicState(listOf(it), it.id, null)
            titleLiveData.value = SINGLE_PODCAST_TITLE
        } ?: type?.let { getTypeData(type) } ?: podcastId?.let {
            loadPodcastId(it)
        }
    }

    fun isAdvertisementAllowed(): Boolean {
        return advertisementRepository.isAdvertisementAllowed()
    }

    private fun getTypeData(id: String) {
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
                Single.just(
                    WaveResponse(
                        podcasts = it.list,
                        title = it.title,
                        type = it.type,
                        warning = null
                    )
                )
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                // progressLiveData.value = false
            }
            .subscribe(object : MusicPlayerSignleObserver<WaveResponse>(compositeDisposable) {
                override fun onSuccess(t: WaveResponse) {
                    val session = musicRepository.getLastSessionData(id)
                    musicLiveData.value = MusicState(t.podcasts, null, session)
                    titleLiveData.value = t.title ?: "Волна"
                    AppMetrica.reportEvent("PlayerScreen", t.type)
//                    sharedPreferences.edit().putLong(type, Calendar.getInstance().timeInMillis)
//                        .apply()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    AppMetrica.reportEvent("PlayerScreen", "error: ${e.message}")
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
                    musicLiveData.value = MusicState(listOf(t), t.id, null)
                }
            })
    }
}

data class MusicState(
    val list: List<Music>,
    val singlePodcastId: String?,
    val session: LastSessionData?
)